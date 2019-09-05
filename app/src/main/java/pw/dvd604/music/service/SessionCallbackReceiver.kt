package pw.dvd604.music.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.ResultReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import com.android.volley.Response
import org.json.JSONObject
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.SearchHandler
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.network.HTTP

class SessionCallbackReceiver(private val service: MediaService) :
    MediaSessionCompat.Callback(),
    Response.Listener<String> {

    override fun onResponse(response: String?) {
        service.mediaSession.setMetadata(Util.addMetadata(JSONObject(response).getInt("duration")))
    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        when ((mediaButtonEvent?.extras?.get(Intent.EXTRA_KEY_EVENT) as KeyEvent).keyCode) {

            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (service.player.isPlaying) {
                    service.player.pause()
                } else {
                    service.player.start()
                }
                service.buildNotification()
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                service.nextSong()
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                service.prevSong()
            }
        }
        return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
        super.onCommand(command, extras, cb)
        when (command) {
            "shuffle" -> {
                if (extras?.getBoolean("shuffle")!!) {
                    service.mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)
                } else {
                    service.mediaSession.setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
                }
            }
            "likesong" -> {
                service.http.putReq(HTTP.like(service.currentMedia?.id), JSONObject("{}"))
            }
            "setQueue" -> {
                service.hasQueue = true
                service.mediaQueue = Util.mediaQueue
            }
        }
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
        Util.log(this, "Got search $query")

        val songs = SearchHandler.search(query)

        if (songs.isEmpty()) {
            service.nextSong()
            return
        }

        Util.log(this, "${songs.size} entry 0: ${songs[0].generateText()}")

        Util.log(this, "preparing")

        onPrepareFromUri(Uri.parse(Util.songToUrl(songs[0])), null)
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        onPlayFromSearch(query, extras)
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        super.onPrepareFromUri(uri, extras)

        if (extras?.getSerializable("media") != null) {
            service.currentMedia = extras.getSerializable("media") as Media
        } else {
            val splitURL = uri.toString().split('/')

            service.currentMedia = try {
                SongList.songList.filter {
                    it.id == splitURL[splitURL.size - 1]
                }[0]
            } catch (e: Exception) {
                SongList.songList.random()
            }
        }

        Util.addSongToStack(service.currentMedia)

        service.mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_BUFFERING,
                0,
                1f
            ).build()
        )

        service.http.getReq(HTTP.songInfo(service.currentMedia!!.id), this)

        if (service.player.isPlaying) {
            service.player.stop()
        }

        service.player.reset()

        service.player.setAudioAttributes(
            AudioAttributes
                .Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )

        service.player.apply {
            setDataSource(uri.toString())
            prepareAsync()
        }


        service.currentMedia?.let {
            service.mediaSession.setMetadata(Util.songToMetadata(it))
        }
    }

    override fun onPlay() {
        super.onPlay()
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Request audio focus for playback, this registers the afChangeListener

        service.mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_PLAYING,
                0,
                1f
            ).build()
        )

        service.registerReceivers()

        service.audioFocusRequest =
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(service.afChangeListener)
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }

        val result = am.requestAudioFocus(service.audioFocusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            service.mediaSession.isActive = true
            // start the player (custom call)
            service.player.start()

            // Register BECOME_NOISY BroadcastReceiver
            //registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
            // Put the service in the foreground, post notification
            service.buildNotification()
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post(
            SeekRunnable(
                service,
                handler
            )
        )

    }

    class SeekRunnable(private val service: MediaService, private val handler: Handler) :
        Runnable {
        override fun run() {
            service.mediaSession.setMetadata(Util.addMetadataProgress(service.player.currentPosition))
            handler.postDelayed(this, 1000)
        }
    }

    override fun onSeekTo(pos: Long) {
        super.onSeekTo(pos)
        service.player.pause()
        service.player.seekTo(pos.toInt())
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        service.prevSong()
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        service.nextSong()
    }

    override fun onStop() {
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Abandon audio focus
        if (service.isAudioFocusRequestInitialised())
            am.abandonAudioFocusRequest(service.audioFocusRequest)

        service.unregisterReceivers()

        // Stop the service
        service.stopSelf()
        // Set the session inactive  (and update metadata and state)
        service.mediaSession.isActive = false
        // stop the player (custom call)
        service.player.stop()
        // Take the service out of the foreground
        service.stopForeground(true)

        service.mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_STOPPED,
                0,
                1f
            ).build()
        )
    }

    override fun onPause() {
        //val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Update metadata and state
        // pause the player (custom call)
        service.player.pause()

        service.mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_PAUSED,
                service.player.currentPosition.toLong(),
                1f
            ).build()
        )
        // unregister BECOME_NOISY BroadcastReceiver
        service.unregisterReceivers()
        // Take the service out of the foreground, retain the notification
        service.stopForeground(false)
    }
}

class AudioFocusListener(private val mediaService: MediaService) :
    AudioManager.OnAudioFocusChangeListener {
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaService.player.isPlaying) {
                    mediaService.mediaSession.controller.transportControls.play()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (mediaService.player.isPlaying) {
                    mediaService.mediaSession.controller.transportControls.pause()
                }
            }
        }
    }
}