package pw.dvd604.music.service

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.KeyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SessionCallback(private val service: MediaPlaybackService) : MediaSessionCompat.Callback() {

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
        }
        return super.onMediaButtonEvent(mediaButtonEvent)
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
        service.mMediaContainer.play(mediaId)



        GlobalScope.launch {
            val notification = service.mNotificationBuilder.build(mediaId!!)
            val meta = service.mNotificationBuilder.buildMetaFromID(mediaId)
            service.mediaSession?.setMetadata(meta!!)

            service.mediaSession?.setPlaybackState(
                PlaybackStateCompat.Builder().setState(
                    PlaybackStateCompat.STATE_PLAYING,
                    service.mMediaContainer.currentPosition(),
                    1f
                ).build()
            )

            ui { service.startForeground(6969, notification) }
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
}