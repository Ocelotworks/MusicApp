package pw.dvd604.music

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.android.volley.Response
import org.json.JSONObject
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.SongList.Companion.downloadedSongs
import pw.dvd604.music.util.Util
import kotlin.random.Random

class MediaService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    private val id: Int = 696969
    private val channelId: String = "petifyNot"
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var http: HTTP
    private var songList = SongList.songList
    private var hasQueue: Boolean = false
    private var songQueue: ArrayList<Song>? = null
    private var queuePosition: Int = 0

    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    //private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: MediaPlayer

    private lateinit var audioFocusRequest: AudioFocusRequest

    private var currentSong: Song? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        androidx.media.session.MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        Util.createNotificationChannel(
            this,
            channelId,
            getString(R.string.channel_name),
            getString(R.string.channel_description)
        )

        http = HTTP(this)

        afChangeListener = AudioFocusListener(this)
        player = MediaPlayer()
        player.setOnPreparedListener(this)
        player.setOnErrorListener(this)
        player.setOnCompletionListener(this)
        player.setOnSeekCompleteListener(this)

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "petify").apply {

            // Enable callbacks from MediaButtons and TransportControls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                            or PlaybackStateCompat.ACTION_SEEK_TO
                )

            setPlaybackState(stateBuilder.build())


            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
        mediaSession.setCallback(SessionCallbackReceiver(this))
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val mediaList = ArrayList<MediaBrowserCompat.MediaItem>(0)
        for (song in songList) {
            mediaList.add(Util.songToMediaItem(song))
        }

        result.sendResult(mediaList)
    }

    var attempts = 0

    private fun nextSong() {
        if (!hasQueue) {
            val list: ArrayList<Song> = if (Settings.getBoolean(Settings.shuffleOffline)) {
                downloadedSongs
            } else {
                songList
            }

            val currentSongIndex: Int = if (list.indexOf(currentSong) == -1) {
                0
            } else {
                list.indexOf(currentSong)
            }

            val nextSongIndex: Int =
                if (mediaSession.controller.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                    Random.nextInt(list.size)
                } else {
                    (currentSongIndex + 1) % list.size
                }

            val nextSong: Song = list[nextSongIndex]
            val url: String = Util.songToUrl(nextSong)

            val bundle = Bundle()
            bundle.putSerializable("song", nextSong)

            mediaSession.controller.transportControls.prepareFromUri(Uri.parse(url), bundle)
        } else {
            //If there is a song queue loaded
            currentSong?.let { song ->
                songQueue?.let { queue ->

                    queuePosition = queue.indexOf(song) + 1 + attempts

                    if (queuePosition > queue.size) {
                        hasQueue = false
                        songQueue = null
                        nextSong()
                        return
                    }

                    val nextSong: Song = queue[queuePosition]

                    if (Settings.getBoolean(Settings.shuffleOffline) && downloadedSongs.indexOf(
                            nextSong
                        ) == -1
                    ) {
                        attempts++
                        nextSong()
                        return
                    }

                    val url: String = Util.songToUrl(nextSong)

                    val bundle = Bundle()
                    bundle.putSerializable("song", nextSong)

                    mediaSession.controller.transportControls.prepareFromUri(Uri.parse(url), bundle)
                }
            }
        }
    }

    private fun prevSong() {
        val nextSong: Song = Util.popSongStack()
        val url: String = Util.songToUrl(nextSong)

        val bundle = Bundle()
        bundle.putSerializable("song", nextSong)

        mediaSession.controller.transportControls.prepareFromUri(Uri.parse(url), bundle)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        nextSong()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaSession.controller.transportControls.play()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        mp?.start()
    }

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
                    buildNotification()
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
                    service.http.putReq(HTTP.like(service.currentSong?.id), JSONObject("{}"))
                }
                "setQueue" -> {
                    service.hasQueue = true
                    service.songQueue = Util.songQueue
                }
            }
        }

        override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
            super.onPrepareFromUri(uri, extras)
            service.currentSong = extras?.getSerializable("song") as Song

            Util.addSongToStack(service.currentSong)

            service.mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    PlaybackStateCompat.STATE_BUFFERING,
                    0,
                    1f
                ).build()
            )

            service.http.getReq(HTTP.songInfo(service.currentSong!!.id), this)

            service.player.stop()
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


            service.mediaSession.setMetadata(Util.songToMetadata(extras.getSerializable("song") as Song))
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                    buildNotification()
                }
                val handler = Handler(Looper.getMainLooper())
                handler.post(SeekRunnable(service, handler))
            }

        }

        class SeekRunnable(private val service: MediaService, val handler: Handler) : Runnable {
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (service::audioFocusRequest.isInitialized) {
                    am.abandonAudioFocusRequest(service.audioFocusRequest)
                    //unregisterReceiver(myNoisyAudioStreamReceiver)
                    // Stop the service
                    service.stopSelf()
                    // Set the session inactive  (and update metadata and state)
                    service.mediaSession.isActive = false
                    // stop the player (custom call)
                    service.player.stop()
                    // Take the service out of the foreground
                    service.stopForeground(false)
                }
            }

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
            //unregisterReceiver(myNoisyAudioStreamReceiver)
            // Take the service out of the foreground, retain the notification
            service.stopForeground(false)
        }

        private fun buildNotification() {
            val controller = service.mediaSession.controller
            val mediaMetadata = controller?.metadata
            val description = mediaMetadata?.description

            val builder = NotificationCompat.Builder(service, service.channelId).apply {
                // Add the metadata for the currently playing track
                setContentTitle(description?.title)
                setContentText(description?.subtitle)
                setSubText(description?.description)
                setLargeIcon(description?.iconBitmap)

                // Enable launching the player by clicking the notification
                setContentIntent(controller?.sessionActivity)

                // Stop the service when the notification is swiped away
                setDeleteIntent(
                    androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                        service,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                setSmallIcon(R.drawable.ic_notification)
                color = ContextCompat.getColor(service, R.color.colorPrimary)

                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_skip_previous_white_24,
                        "Previous",
                        androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    )
                )

                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        if (service.player.isPlaying) {
                            R.drawable.baseline_pause_white_24
                        } else {
                            R.drawable.baseline_play_arrow_white_24
                        },
                        service.getString(R.string.pause),
                        androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_skip_next_white_24,
                        "Next",
                        androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service,
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                    )
                )

                // Take advantage of MediaStyle features
                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                                service,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                )
            }

            // Display the notification and place the service in the foreground
            service.startForeground(service.id, builder.build())
        }
    }

    class AudioFocusListener(private val mediaService: MediaService) :
        AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    if (!mediaService.player.isPlaying) {
                        mediaService.player.start()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    if (!mediaService.player.isPlaying) {
                        mediaService.player.pause()
                    }
                }
            }
        }
    }
}
