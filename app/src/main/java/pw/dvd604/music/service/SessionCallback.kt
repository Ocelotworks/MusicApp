package pw.dvd604.music.service

import android.content.Intent
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
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
            KeyEvent.KEYCODE_MEDIA_STOP -> {
                onStop()
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
        service.mMediaContainer.play(mediaId, extras)



        GlobalScope.launch {
            val notification = service.mNotificationBuilder.build(mediaId!!)
            var meta = service.mNotificationBuilder.buildMetaFromID(mediaId)
            if (meta == null) {
                meta = MediaMetadataCompat.Builder().apply {
                    this.putText(
                        MediaMetadata.METADATA_KEY_TITLE, "DoOt Me UP InsIdE"
                    )
                    this.putText(
                        MediaMetadata.METADATA_KEY_ARTIST,
                        "What the fuck did you just fucking say about me, you little bitch? I'll have you know I graduated top of my class in the Navy Seals, and I've been involved in numerous secret raids on Al-Quaeda, and I have over 300 confirmed kills. I am trained in gorilla warfare and I'm the top sniper in the entire US armed forces. You are nothing to me but just another target. I will wipe you the fuck out with precision the likes of which has never been seen before on this Earth, mark my fucking words. You think you can get away with saying that shit to me over the Internet? Think again, fucker. As we speak I am contacting my secret network of spies across the USA and your IP is being traced right now so you better prepare for the storm, maggot. The storm that wipes out the pathetic little thing you call your life. You're fucking dead, kid. I can be anywhere, anytime, and I can kill you in over seven hundred ways, and that's just with my bare hands. Not only am I extensively trained in unarmed combat, but I have access to the entire arsenal of the United States Marine Corps and I will use it to its full extent to wipe your miserable ass off the face of the continent, you little shit. If only you could have known what unholy retribution your little \"clever\" comment was about to bring down upon you, maybe you would have held your fucking tongue. But you couldn't, you didn't, and now you're paying the price, you goddamn idiot. I will shit fury all over you and you will drown in it. You're fucking dead, kiddo."
                    )
                    this.putText(MediaMetadata.METADATA_KEY_MEDIA_ID, "FUCK3D")
                    this.putLong(
                        MediaMetadata.METADATA_KEY_DURATION, 69696969
                    )
                }.build()
            }
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