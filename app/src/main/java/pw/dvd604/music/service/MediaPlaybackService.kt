package pw.dvd604.music.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import pw.dvd604.music.R

private const val LOG_TAG: String = "MediaService"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var mNotificationBuilder: NotificationBuilder
    val mMediaContainer = MediaContainer(this)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(
            LOG_TAG,
            "onStartCommand(): received intent " + intent?.action + " with flags " + flags + " and startId " + startId
        )
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()

        Log.e("Service", "Started")


        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {

            isActive = true

            setRatingType(RatingCompat.RATING_NONE)
            setSessionActivity(sessionActivityPendingIntent)

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_STOP
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                ).setState(PlaybackStateCompat.STATE_PLAYING, 0, 1f, SystemClock.elapsedRealtime())
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(SessionCallback(this@MediaPlaybackService))

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }

        mNotificationBuilder = NotificationBuilder(this, mediaSession)

        mNotificationBuilder.createChannel(
            getString(R.string.petify_music_channel),
            getString(R.string.channel_name),
            getString(R.string.channel_description)
        )
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.detach()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }

}