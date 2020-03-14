package pw.dvd604.music.service

import android.animation.ValueAnimator
import android.media.MediaMetadata
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
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
        if (metadata == null) return

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
                img.placeholder(R.drawable.album).into(activity.songArt)
                img.placeholder(R.drawable.albumsmall).into(activity.smallAlbumArt)
            }

        activity.opinionController.resetState()

        val rating: RatingCompat = metadata.getRating(MediaMetadataCompat.METADATA_KEY_RATING)
            ?: return

        if (!rating.isRated) {
            activity.btnNeutral.setImageResource(R.drawable.baseline_thumbs_up_down_white_36)
            return
        }

        if (rating.isThumbUp) {
            activity.btnNeutral.setImageResource(R.drawable.baseline_thumb_up_white_36)
        } else {
            activity.btnNeutral.setImageResource(R.drawable.baseline_thumb_down_white_36)
            this.activity.mediaController.transportControls.skipToNext()
        }
    }

    fun stopAnimation() {
        if (mProgressAnimator != null) {
            mProgressAnimator?.cancel()
            mProgressAnimator = null
        }
    }

    override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
        super.onPlaybackStateChanged(state)

        if (mProgressAnimator != null) {
            mProgressAnimator?.cancel()
            mProgressAnimator = null
        }

        when (state?.state) {
            PlaybackStateCompat.STATE_BUFFERING -> {
                activity.btnPause.setImageResource(R.drawable.baseline_shuffle_white_48)
                activity.smallPausePlay.setImageResource(R.drawable.baseline_shuffle_white_48)
                activity.opinionController.openable = false
            }
            PlaybackStateCompat.STATE_PLAYING -> {
                activity.btnPause.setImageResource(R.drawable.baseline_pause_white_48)
                activity.smallPausePlay.setImageResource(R.drawable.baseline_pause_white_48)
                activity.opinionController.openable = true

                val progress = state.position.toInt()
                activity.songProgress.progress = progress

                val timeToEnd =
                    ((activity.songProgress.max - progress) / state.playbackSpeed).toInt()

                if (timeToEnd < 0) return

                mProgressAnimator =
                    ValueAnimator.ofInt(progress, activity.songProgress.max)
                        .setDuration(timeToEnd.toLong())
                mProgressAnimator?.interpolator = LinearInterpolator()
                mProgressAnimator?.addUpdateListener(this)
                mProgressAnimator?.start()
            }
            PlaybackStateCompat.STATE_PAUSED -> {
                activity.btnPause.setImageResource(R.drawable.baseline_play_arrow_white_48)
                activity.smallPausePlay.setImageResource(R.drawable.baseline_play_arrow_white_48)
            }
            PlaybackStateCompat.STATE_STOPPED -> {
                activity.opinionController.openable = false
            }
        }
    }

    override fun onAnimationUpdate(valueAnimator: ValueAnimator?) {
        val animatedIntValue = valueAnimator?.animatedValue as Int
        activity.songProgress.progress = animatedIntValue
        activity.songProgessText.text = Util.prettyTime(animatedIntValue / 1000)
    }

}