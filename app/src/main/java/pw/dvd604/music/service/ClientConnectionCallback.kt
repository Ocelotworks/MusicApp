package pw.dvd604.music.service

import android.animation.ValueAnimator
import android.media.MediaMetadata
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.animation.LinearInterpolator
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.util.Util

class ClientConnectionCallback(private val activity: MainActivity) :
    MediaBrowserCompat.ConnectionCallback() {
    override fun onConnected() {

        // Get the token for the MediaSession
        activity.mediaBrowser.sessionToken.also { token ->

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(
                activity,
                token
            )

            // Save the controller
            MediaControllerCompat.setMediaController(
                activity,
                mediaController
            )
        }

        // Finish building the UI
        buildTransportControls()
    }

    private fun buildTransportControls() {
        MediaControllerCompat.getMediaController(activity)
            .registerCallback(activity.controllerCallback)
    }

    override fun onConnectionSuspended() {
        // The Service has crashed. Disable transport controls until it automatically reconnects
    }

    override fun onConnectionFailed() {
        // The Service has refused our connection
    }
}

class ControllerCallback(private val activity: MainActivity) : MediaControllerCompat.Callback(),
    ValueAnimator.AnimatorUpdateListener {

    private var mProgressAnimator: ValueAnimator? = null

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        Log.e("Meta", "A")
        if (metadata == null) return
        Log.e("Meta", "B")

        activity.songName.text = metadata.description.title
        activity.songAuthor.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        activity.songDuration.text =
            Util.prettyTime(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) / 1000)
        activity.songProgress.max =
            metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION).toInt()

        activity.smallSongTitle.text = metadata.description.title
        activity.smallArtistText.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)

        Glide.with(activity).load(metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART))
            .also { img ->
                img.into(activity.songArt)
                img.into(activity.smallAlbumArt)
            }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        super.onPlaybackStateChanged(state)

        // If there's an ongoing animation, stop it now.
        // If there's an ongoing animation, stop it now.
        if (mProgressAnimator != null) {
            mProgressAnimator?.cancel()
            mProgressAnimator = null
        }

        val progress = state?.position?.toInt() ?: 0
        activity.songProgress.progress = progress

        // If the media is playing then the seekbar should follow it, and the easiest
        // way to do that is to create a ValueAnimator to update it so the bar reaches
        // the end of the media the same time as playback gets there (or close enough).
        // If the media is playing then the seekbar should follow it, and the easiest
// way to do that is to create a ValueAnimator to update it so the bar reaches
// the end of the media the same time as playback gets there (or close enough).
        if (state != null && state.state == PlaybackStateCompat.STATE_PLAYING) {
            val timeToEnd =
                ((activity.songProgress.max - progress) / state.playbackSpeed).toInt()
            mProgressAnimator =
                ValueAnimator.ofInt(progress, activity.songProgress.max)
                    .setDuration(timeToEnd.toLong())
            mProgressAnimator?.interpolator = LinearInterpolator()
            mProgressAnimator?.addUpdateListener(this)
            mProgressAnimator?.start()
        }

        when (state?.state) {
            PlaybackStateCompat.STATE_BUFFERING -> {
                activity.btnPause.setImageResource(R.drawable.baseline_shuffle_white_48)
                activity.smallPausePlay.setImageResource(R.drawable.baseline_shuffle_white_48)
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                activity.btnPause.setImageResource(R.drawable.baseline_pause_white_48)
                activity.smallPausePlay.setImageResource(R.drawable.baseline_pause_white_48)
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                activity.btnPause.setImageResource(R.drawable.baseline_play_arrow_white_48)
                activity.smallPausePlay.setImageResource(R.drawable.baseline_play_arrow_white_48)
            }
        }
    }

    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
        val animatedIntValue = valueAnimator?.animatedValue as Int
        activity.songProgress.progress = animatedIntValue
        activity.songProgessText.text = Util.prettyTime(animatedIntValue / 1000)
    }

}