package pw.dvd604.music

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.service.media.MediaBrowserService
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.SongListRequest
import pw.dvd604.music.util.Util

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
                )

            setPlaybackState(stateBuilder.build())

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
        mediaSession?.setCallback(SessionCallbackReceiver(this))
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

    override fun onCompletion(mp: MediaPlayer?) {

    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {
        SessionCallbackReceiver(this).onPlay()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_MIN
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

        override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
            super.onPrepareFromUri(uri, extras)

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
        }

        override fun onPlay() {
            super.onPlay()
            val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Request audio focus for playback, this registers the afChangeListener

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
        }

        override fun onPause() {
            val am = service.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            // Update metadata and state
            // pause the player (custom call)
            service.player.pause()
            // unregister BECOME_NOISY BroadcastReceiver
            //unregisterReceiver(myNoisyAudioStreamReceiver)
            // Take the service out of the foreground, retain the notification
            service.stopForeground(false)
        }

        private fun buildNotification() {
            val controller = service.mediaSession?.controller
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

                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_pause_white_18,
                        service.getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            service,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                // Take advantage of MediaStyle features
                setStyle(
                    android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(service.mediaSession?.sessionToken)
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
