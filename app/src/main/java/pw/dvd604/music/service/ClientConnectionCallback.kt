package pw.dvd604.music.service

import android.media.MediaMetadata
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
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

class ControllerCallback(private val activity: MainActivity) : MediaControllerCompat.Callback() {

    override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
        Log.e("Meta", "A")
        if (metadata == null) return
        Log.e("Meta", "B")

        activity.songName.text = metadata.description.title
        activity.songAuthor.text = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        activity.songDuration.text =
            Util.prettyTime(metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))
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

}