package pw.dvd604.music.service

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.media.MediaBrowserServiceCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.ArtistSong
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.AppDatabase

private const val LOG_TAG: String = "MediaService"

class MediaPlaybackService : MediaBrowserServiceCompat() {

    private var mediaSession: MediaSessionCompat? = null
    private lateinit var stateBuilder: PlaybackStateCompat.Builder
    lateinit var db: AppDatabase
    val mNotificationBuilder = NotificationBuilder(this, mediaSession)
    val mMediaContainer = MediaContainer(this)
    lateinit var songList: List<Song>
    lateinit var artistSongList: List<ArtistSong>

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

                songList = db.songDao().getAll()
                artistSongList = db.artistSongJoinDao().getSongsWithArtists()
            } catch (e: Exception) {
                Log.e("Service", "", e)
            }
        }

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
        result.detach()

        GlobalScope.launch {
            val data = db.artistSongJoinDao().getLimit(10)
            val list = ArrayList<MediaBrowserCompat.MediaItem>(0)

            data.forEach {
                list.add(it.toMediaItem())
            }

            result.sendResult(list)
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot("root", null)
    }

}