package pw.dvd604.music.service

import android.content.Context
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.storage.DatabaseContract
import java.io.File

class MediaContainer(private val service: MediaPlaybackService) : MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {
    companion object {
        var songID: String = ""
        lateinit var player: MediaPlayer
        lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
        lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
        lateinit var request: AudioFocusRequest
        var playlistID = ""
        val songsPlayed = ArrayList<String>(0)
    }

    init {
        player = MediaPlayer().apply {
            setOnPreparedListener(this@MediaContainer)
            setOnErrorListener(this@MediaContainer)
            setOnCompletionListener(this@MediaContainer)
            setOnSeekCompleteListener(this@MediaContainer)
        }

        becomingNoisyReceiver = BecomingNoisyReceiver(service)
        afChangeListener = AudioFocusListener(service)
    }

    fun play(id: String?, extras: Bundle? = null) {
        if (id == null)
            return

        player.reset()

        val file = File(
            "${Environment.getExternalStorageDirectory()}/petify/${id}"
        )

        if (!file.exists()) {
            player.setDataSource("https://unacceptableuse.com/petify/song/$id")
        } else {
            player.setDataSource(file.absolutePath)
        }

        if (extras?.getInt("do_not_add") != 1)
            songsPlayed.add(id)

        player.prepare()

        play()
    }

    fun play(uri: Uri?) {
        player.reset()
        player.setDataSource(uri.toString())
        player.prepare()

        play()
    }

    fun prepare(id: String?) {

    }

    fun prepare(uri: Uri?) {

    }

    fun play() {
        val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val filter = IntentFilter()
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)


        request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setOnAudioFocusChangeListener(afChangeListener)
            setAudioAttributes(AudioAttributes.Builder().run {
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            build()
        }

        val result = am.requestAudioFocus(request)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            service.mediaSession?.isActive = true
            player.start()

            becomingNoisyReceiver.register(service, filter)
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {

    }

    override fun onCompletion(mp: MediaPlayer?) {
        skip(1)
    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }

    fun pause() {
        player.pause()
        service.stopForeground(false)
        becomingNoisyReceiver.unregister(service)
    }

    fun stop() {
        player.stop()
        player.reset()

        try {
            val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocusRequest(request)
        } catch (e: Exception) {
        }

        becomingNoisyReceiver.unregister(service)
        service.mediaSession?.isActive = false
        service.stopForeground(true)
    }

    fun skip(i: Int) {
        GlobalScope.launch {
            if (i > 0) {
                val normalSQL =
                    "SELECT ${DatabaseContract.Song.TABLE_NAME}.id FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Opinion.TABLE_NAME} ON ${DatabaseContract.Opinion.TABLE_NAME}.id =  ${DatabaseContract.Song.TABLE_NAME}.id WHERE ${DatabaseContract.Opinion.COLUMN_NAME_OPINION} <> -1 AND ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_TITLE} <> 'Unknown' ORDER BY RANDOM() LIMIT 1"
                val playlistSQL =
                    "SELECT ${DatabaseContract.Song.TABLE_NAME}.id FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Opinion.TABLE_NAME} ON ${DatabaseContract.Opinion.TABLE_NAME}.id =  ${DatabaseContract.Song.TABLE_NAME}.id INNER JOIN ${DatabaseContract.PlaylistSongs.TABLE_NAME} ON ${DatabaseContract.PlaylistSongs.TABLE_NAME}.${DatabaseContract.PlaylistSongs.COLUMN_NAME_SONG_ID} = ${DatabaseContract.Song.TABLE_NAME}.id WHERE ${DatabaseContract.Opinion.COLUMN_NAME_OPINION} <> -1 AND ${DatabaseContract.PlaylistSongs.COLUMN_NAME_PLAYLIST_ID} = ? ORDER BY RANDOM() LIMIT 1"

                val cursor =
                    (service.applicationContext as MusicApplication).readableDatabase.rawQuery(
                        if (playlistID != "") {
                            playlistSQL
                        } else {
                            normalSQL
                        },
                        if (playlistID != "") {
                            arrayOf(playlistID)
                        } else {
                            null
                        },
                        null
                    )

                with(cursor) {
                    while (moveToNext()) {

                        val id = getString(
                            getColumnIndexOrThrow("id")
                        )

                        service.mediaSession?.controller?.transportControls?.playFromMediaId(
                            id, null
                        )

                        service.mNotificationBuilder.build(id)
                    }
                    cursor.close()
                }
            } else if (i < 0) {
                if (songsPlayed.size != 0) {

                    val currentIndex = songsPlayed.size - 1
                    var newIndex = currentIndex + i

                    if (newIndex < 0) newIndex = 0

                    val bundle = Bundle()
                    bundle.putInt("do_not_add", 1)

                    songsPlayed.removeAt(currentIndex)

                    service.mediaSession?.controller?.transportControls?.playFromMediaId(
                        songsPlayed[newIndex], bundle
                    )
                }
            }
        }
    }

    fun currentPosition(): Long {
        return player.currentPosition.toLong()
    }

    class AudioFocusListener(private val mediaService: MediaPlaybackService) :
        AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!player.isPlaying) {
                        mediaService.mediaSession?.controller?.transportControls?.play()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (player.isPlaying) {
                        mediaService.mediaSession?.controller?.transportControls?.pause()
                    }
                }
            }
        }
    }
}