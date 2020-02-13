package pw.dvd604.music.service

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import pw.dvd604.music.R

private const val LOG_TAG: String = "MediaService"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var mNotificationBuilder: NotificationBuilder
    val mMediaContainer = MediaContainer(this)

    override fun onCreate() {
        super.onCreate()

        Log.e("Service", "Started")


        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {

            setRatingType(RatingCompat.RATING_NONE)
            setSessionActivity(sessionActivityPendingIntent)

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                            or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                            or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                )
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