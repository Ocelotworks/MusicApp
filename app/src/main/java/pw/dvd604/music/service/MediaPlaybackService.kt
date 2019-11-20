package pw.dvd604.music.service

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.AppDatabase
import pw.dvd604.music.data.room.ArtistSong

private const val LOG_TAG: String = "MediaService"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var db: AppDatabase
    private val mNotificationBuilder = NotificationBuilder(this, mediaSession)

    private lateinit var songs: List<Song>
    private lateinit var artistSongs: List<ArtistSong>

    override fun onCreate() {
        super.onCreate()

        Log.e("Service", "Started")
        mNotificationBuilder.createChannel(
            getString(R.string.petify_music_channel),
            getString(R.string.channel_name),
            getString(R.string.channel_description)
        )

        GlobalScope.launch {
            try {
                db = (application as MusicApplication).db
            } catch (e: Exception) {
                Log.e("Service", "", e)
            }
        }

        // Create a MediaSessionCompat
        mediaSession = MediaSessionCompat(baseContext, LOG_TAG).apply {

            // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
            stateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
            setPlaybackState(stateBuilder.build())

            // MySessionCallback() has methods that handle callbacks from a media controller
            setCallback(SessionCallback(this@MediaPlaybackService))

            // Set the session's token so that client activities can communicate with it.
            setSessionToken(sessionToken)
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(null)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }

}