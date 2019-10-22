package pw.dvd604.music.service

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.SearchHandler
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.SongList.Companion.downloadedSongs
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.download.Downloader
import pw.dvd604.music.util.network.HTTP
import pw.dvd604.music.util.network.SongListRequest
import kotlin.random.Random

class MediaService : MediaBrowserServiceCompat(), MediaPlayer.OnPreparedListener,
    MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {

    private val id: Int = 696969
    private lateinit var channelId: String
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var http: HTTP
    private var songList = SongList.songList

    lateinit var afChangeListener: AudioManager.OnAudioFocusChangeListener
    private val noisyAudioStreamReceiver =
        BecomingNoisyReceiver(this)
    private val pwIntentReceiver = MusicIntentController(this)
    lateinit var mediaSession: MediaSessionCompat
    lateinit var player: MediaPlayer

    lateinit var audioFocusRequest: AudioFocusRequest
    fun isAudioFocusRequestInitialised() = ::audioFocusRequest.isInitialized

    var currentMedia: Media? = null

    private val intentFilter = IntentFilter()
    private val pwIntentFilter = IntentFilter()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        androidx.media.session.MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        channelId = getString(R.string.petify_music_channel)
        Util.createNotificationChannel(
            this,
            channelId,
            getString(R.string.channel_name),
            getString(R.string.channel_description)
        )

        Util.log(this, "Started Service")

        http = HTTP(this)

        if (SongList.songList.isEmpty() && !SongList.isBuilding) {
            Settings.init(this)
            Util.downloader = Downloader(this.applicationContext)
            HTTP.setup(Settings.getSetting(Settings.server))
            populateSongList()
        }

        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        pwIntentFilter.addAction(MusicIntentController.PLAY)
        pwIntentFilter.addAction(MusicIntentController.SKIP)
        pwIntentFilter.addAction(MusicIntentController.PREV)

        afChangeListener = AudioFocusListener(this)

        player = MediaPlayer().apply {
            setOnPreparedListener(this@MediaService)
            setOnErrorListener(this@MediaService)
            setOnCompletionListener(this@MediaService)
            setOnSeekCompleteListener(this@MediaService)
        }

        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, "petify").apply {
            setSessionActivity(sessionActivityPendingIntent)
            setRatingType(RatingCompat.RATING_NONE)

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SET_SHUFFLE_MODE
                            or PlaybackStateCompat.ACTION_SEEK_TO
                            or PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                            or PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH
                            or PlaybackStateCompat.ACTION_PREPARE
                            or PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_STOP
                    //or PlaybackStateCompat.ACTION_SET_REPEAT_MODE
                )

            stateBuilder.addCustomAction("likesong", "Like song", R.drawable.ic_notification)
            stateBuilder.addCustomAction("setQueue", "Set Queue", R.drawable.ic_notification)

            setPlaybackState(stateBuilder.build())
            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
        mediaSession.setCallback(SessionCallbackReceiver(this))
    }

    fun buildNotification() {
        val controller = mediaSession.controller
        val mediaMetadata = controller?.metadata
        val description = mediaMetadata?.description

        val builder = NotificationCompat.Builder(this@MediaService, channelId).apply {
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
                    this@MediaService,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.drawable.ic_notification)
            color = ContextCompat.getColor(this@MediaService, R.color.colorPrimary)

            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_previous_white_24,
                    "Previous",
                    androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            // Add a pause button
            addAction(
                NotificationCompat.Action(
                    if (this@MediaService.player.isPlaying) {
                        R.drawable.baseline_pause_white_24
                    } else {
                        R.drawable.baseline_play_arrow_white_24
                    },
                    this@MediaService.getString(R.string.pause),
                    androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaService,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_next_white_24,
                    "Next",
                    androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this@MediaService,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        androidx.media.session.MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this@MediaService,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }

        // Display the notification and place the service in the foreground
        startForeground(id, builder.build())
    }

    private fun populateSongList() {
        SongList.isBuilding = true
        val fileContents = Util.readFromFile(this, "mediaList")

        if (fileContents != null) {
            SongListRequest(::setSongs).onResponse(fileContents)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceivers()
    }

    private fun setSongs(arrayList: ArrayList<Media>) {
        SongList.setSongsAndNotify(arrayList)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val rootExtras = Bundle().apply {
            putBoolean(MEDIA_SEARCH_SUPPORTED, true)
        }
        return BrowserRoot("root", rootExtras)
    }

    override fun onLoadChildren(
        parentMediaId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentMediaId) {
            "root" -> {
                val mediaList = ArrayList<MediaBrowserCompat.MediaItem>(0)
                for (song in SongList.songList) {
                    mediaList.add(song.toMediaItem())
                }
                result.sendResult(mediaList.subList(0, 50))
            }
        }
    }

    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        val songs = SearchHandler.search(query)

        if (songs.isEmpty()) result.sendResult(null)

        val mediaList = ArrayList<MediaBrowserCompat.MediaItem>(0)
        for (song in songs) {
            mediaList.add(song.toMediaItem())
        }

        result.sendResult(mediaList)
    }

    private fun getNewIndex(currentSongIndex: Int, list: ArrayList<Media>): Int {
        if (list.size == 0) {
            throw ArrayIndexOutOfBoundsException("List length not more than 0")
        }

        return if (mediaSession.controller.shuffleMode == PlaybackStateCompat.SHUFFLE_MODE_ALL) {
            if (list.size > 0) {
                Random.nextInt(list.size)
            } else {
                0
            }
        } else {
            (currentSongIndex + 1) % list.size
        }
    }

    fun nextSong() {
        if (Util.mediaQueue.size == 0) {
            val list: ArrayList<Media> = if (Settings.getBoolean(Settings.shuffleOffline)) {
                downloadedSongs
            } else {
                songList
            }

            val currentSongIndex: Int = if (list.indexOf(currentMedia) == -1) {
                0
            } else {
                list.indexOf(currentMedia)
            }

            var nextSongIndex: Int

            try {
                nextSongIndex = getNewIndex(currentSongIndex, list)
            } catch (e: ArrayIndexOutOfBoundsException) {
                return
            }

            while (nextSongIndex == currentSongIndex) {
                nextSongIndex++
            }

            val nextMedia: Media = list[nextSongIndex]
            val url: String = nextMedia.toUrl()

            mediaSession.controller.transportControls.prepareFromUri(Uri.parse(url), null)
        } else {
            //If there is a media queue loaded
            Util.mediaQueue.removeAt(0)

            if (Util.mediaQueue.size > 0) {
                val nextMedia: Media = Util.mediaQueue[0]

                //If we're only allowed to play offline music, and we don't have the song we're trying to play downloaded, skip the song and try again
                if (Settings.getBoolean(Settings.shuffleOffline) && !downloadedSongs.any { song -> song.id == nextMedia.id }) {
                    nextSong()
                    return
                }

                mediaSession.controller.transportControls.prepareFromUri(nextMedia.toUri(), null)
            } else {
                nextSong()
                return
            }
        }
    }

    fun prevSong() {
        val nextMedia: Media = Util.popSongStack()
        val url: String = nextMedia.toUrl()

        mediaSession.controller.transportControls.prepareFromUri(Uri.parse(url), null)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        nextSong()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        mediaSession.setPlaybackState(
            PlaybackStateCompat.Builder().setErrorMessage(
                what,
                "Some error"
            ).build()
        )
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaSession.controller.transportControls.play()
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        mp?.start()
    }

    fun registerReceivers() {
        noisyAudioStreamReceiver.register(this, intentFilter)

        if (Settings.getBoolean(Settings.useIntents)) {
            pwIntentReceiver.register(this, pwIntentFilter)
        }
    }

    fun unregisterReceivers() {
        noisyAudioStreamReceiver.unregister(this)
        pwIntentReceiver.unregister(this)
    }

    companion object {
        private const val MEDIA_SEARCH_SUPPORTED = "android.media.browse.SEARCH_SUPPORTED"
    }
}
