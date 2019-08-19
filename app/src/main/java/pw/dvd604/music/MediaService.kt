package pw.dvd604.music

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.PlaybackState
import android.net.Uri
import android.os.*
import android.service.media.MediaBrowserService
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.KeyEvent
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.SongListRequest
import pw.dvd604.music.util.Util
import kotlin.random.Random

class MediaService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    private val id: Int = 696969
    private val channelId: String = "petifyNot"
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var http: HTTP
    private var songList = ArrayList<Song>(0)

    private lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    //private val myNoisyAudioStreamReceiver = BecomingNoisyReceiver()
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: MediaPlayer

    private lateinit var audioFocusRequest: AudioFocusRequest

    private var currentSong: Song? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        http = HTTP(this)
        http.getReq(HTTP.getSong(), SongListRequest(::setSongs))

        afChangeListener = AudioFocusListener(this)
        player = MediaPlayer()
        player.setOnPreparedListener(this)
        player.setOnErrorListener(this)
        player.setOnCompletionListener(this)

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
                )

            setPlaybackState(stateBuilder.build())


            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
        mediaSession.setCallback(SessionCallbackReceiver(this))
    }

    private fun setSongs(songs: ArrayList<Song>) {
        songList = songs
    }

    override fun onGetRoot(p0: String, p1: Int, p2: Bundle?): BrowserRoot? {
        return BrowserRoot("root", null)
    }

    override fun onLoadChildren(parentMediaId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val mediaList = ArrayList<MediaBrowserCompat.MediaItem>(0)
        for (song in songList) {
            mediaList.add(Util.songToMediaItem(song))
        }

        result.sendResult(mediaList)
    }

    private fun nextSong() {
        val currentSongIndex: Int = songList.indexOf(currentSong)
        val nextSongIndex: Int =
            if (mediaSession.controller.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
                Random.nextInt(songList.size)
            } else {
                currentSongIndex + 1
            }

        val nextSong: Song = songList[nextSongIndex]
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
        mediaSession.setMetadata(Util.addMetadata(mp?.duration))
        mediaSession.controller.transportControls.play()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    class SessionCallbackReceiver(private val service: MediaService) : MediaSessionCompat.Callback() {

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
            }
        }

        override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
            super.onPrepareFromUri(uri, extras)

            service.mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    PlaybackStateCompat.STATE_BUFFERING,
                    0,
                    1f
                ).build()
            )
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

            service.currentSong = extras?.getSerializable("song") as Song
            Util.songToMetadata(extras.getSerializable("song") as Song, true)
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
                service.audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
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
            }

        }

        override fun onSkipToPrevious() {
            super.onSkipToPrevious()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()
            service.nextSong()
        }

        override fun onStop() {
            val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Abandon audio focus
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

            service.mediaSession.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    PlaybackStateCompat.STATE_STOPPED,
                    0,
                    1f
                ).build()
            )
        }

        override fun onPause() {
            val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
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
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        service,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                setSmallIcon(R.mipmap.ic_launcher)
                color = ContextCompat.getColor(service, R.color.colorPrimary)

                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_skip_previous_white_24,
                        "Previous",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
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
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_skip_next_white_24,
                        "Next",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service,
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        )
                    )
                )

                // Take advantage of MediaStyle features
                setStyle(
                    android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
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

    class AudioFocusListener(mediaService: MediaService) : AudioManager.OnAudioFocusChangeListener {
        override fun onAudioFocusChange(focusChange: Int) {

        }
    }
}
