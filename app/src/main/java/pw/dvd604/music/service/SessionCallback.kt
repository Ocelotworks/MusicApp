package pw.dvd604.music.service

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.storage.DatabaseContract

class SessionCallback(private val service: MediaPlaybackService) : MediaSessionCompat.Callback() {

    private var lastID: String = ""

    private fun ui(call: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            call()
        }
    }

    private fun buildNotification() {
        ui { service.startForeground(6969, service.mNotificationBuilder.build()) }
    }

    override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
        when ((mediaButtonEvent?.extras?.get(Intent.EXTRA_KEY_EVENT) as KeyEvent).keyCode) {

            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                if (MediaContainer.player.isPlaying) {
                    onPause()
                } else {
                    onPlay()
                }
                buildNotification()
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                onSkipToNext()
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                onSkipToPrevious()
            }
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                onStop()
            }
        }
        return super.onMediaButtonEvent(mediaButtonEvent)
    }

    override fun onCustomAction(action: String?, extras: Bundle?) {
        super.onCustomAction(action, extras)
        when (action) {
            "requestData" -> {

            }
        }
    }

    override fun onSetRating(rating: RatingCompat?) {
        super.onSetRating(rating)
        if (rating?.ratingStyle == RatingCompat.RATING_THUMB_UP_DOWN) {

            GlobalScope.launch {
                val content = ContentValues().apply {
                    put("id", lastID)
                    put(
                        DatabaseContract.Opinion.COLUMN_NAME_OPINION, if (!rating.isRated) {
                            0
                        } else {
                            if (rating.isThumbUp) {
                                1
                            } else {
                                -1
                            }
                        }
                    )
                }

                (service.application as MusicApplication).database.insertWithOnConflict(
                    DatabaseContract.Opinion.TABLE_NAME,
                    null,
                    content,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
        }
    }

    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {

    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        service.mMediaContainer.prepare(uri)
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        service.mMediaContainer.play(uri)
    }

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        service.mMediaContainer.prepare(mediaId)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        service.mMediaContainer.play(mediaId, extras)
        lastID = mediaId ?: ""

        if (mediaId == null) return

        GlobalScope.launch {
            val meta = service.mNotificationBuilder.buildMetaFromID(mediaId)
            val notification = service.mNotificationBuilder.build()

            service.mediaSession?.setMetadata(meta)

            service.mediaSession?.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    service.mMediaContainer.currentPosition(),
                    1f
                ).build()
            )

            if (notification != null) {
                ui { service.startForeground(6969, notification) }
            }
        }
    }

    override fun onPlay() {
        super.onPlay()
        service.mMediaContainer.play()

        service.mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_PLAYING,
                service.mMediaContainer.currentPosition(),
                1f
            ).build()
        )
    }

    override fun onSkipToPrevious() {
        super.onSkipToPrevious()
        service.mMediaContainer.skip(-1)
    }

    override fun onSkipToNext() {
        super.onSkipToNext()
        service.mMediaContainer.skip(1)
    }

    override fun onStop() {
        service.mMediaContainer.stop()

        service.mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_STOPPED,
                service.mMediaContainer.currentPosition(),
                1f
            ).build()
        )
    }

    override fun onPause() {
        service.mMediaContainer.pause()

        service.mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_PAUSED,
                service.mMediaContainer.currentPosition(),
                1f
            ).build()
        )
    }

    override fun onSeekTo(pos: Long) {
        service.mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder().setState(
                PlaybackStateCompat.STATE_BUFFERING,
                service.mMediaContainer.currentPosition(),
                1f
            ).build()
        )

        MediaContainer.player.seekTo(pos, MediaPlayer.SEEK_CLOSEST)

        onPlay()
    }
}