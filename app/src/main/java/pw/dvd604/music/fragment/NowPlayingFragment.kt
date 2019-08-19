package pw.dvd604.music.fragment

import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.media.AudioManager
import android.media.session.PlaybackState
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.MainActivity
import pw.dvd604.music.MediaService
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.BitmapAsync
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.Util


class NowPlayingFragment : Fragment() {

    companion object {
        private const val intentRoot = "pw.dvd604.music.service"
        const val songIntent = "$intentRoot.song"
        const val timingIntent = "$intentRoot.timing"
        const val updateIntent = "$intentRoot.update"
    }

    private var volumeControlStream: Int = 0
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var http: HTTP? = null
    private var controllerCallback: MediaControllerCompat.Callback = ControllerCallback(this)
    private var shuffleMode : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_playing, container, false)
        http = HTTP(context)
        return v
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaBrowser = MediaBrowserCompat(
            this.context,
            ComponentName(this.context!!, MediaService::class.java),
            ConnectionCallback(this),
            null // optional Bundle
        )
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()
        MediaControllerCompat.getMediaController(this.activity as Activity)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this.activity as Activity)

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    private fun updateUI(metadata: MediaMetadataCompat?) {
        songName.text = metadata?.description?.title
        songAuthor.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        songDuration.text = Util.prettyTime(metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))

        metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.let {
            songProgress.max = it.toInt()
        }
        BitmapAsync(this).execute(metadata?.description?.iconUri.toString())
    }

    fun postImage(bmp: Bitmap?) {
        this.view?.findViewById<ImageView>(R.id.songArt)?.setImageBitmap(bmp)
    }

    class ConnectionCallback(private val nowPlayingFragment: NowPlayingFragment) :
        MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {

            // Get the token for the MediaSession
            nowPlayingFragment.mediaBrowser.sessionToken.also { token ->

                // Create a MediaControllerCompat
                val mediaController = MediaControllerCompat(
                    nowPlayingFragment.context, // Context
                    token
                )

                // Save the controller
                MediaControllerCompat.setMediaController(nowPlayingFragment.activity as Activity, mediaController)
            }

            // Finish building the UI
            nowPlayingFragment.buildTransportControls()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }

    }

    class ControllerCallback(private val nowPlayingFragment: NowPlayingFragment) : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlayingFragment.updateUI(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.position?.let {
                nowPlayingFragment.songProgress.progress = it.toInt()
            }

            when(state?.state){
                PlaybackStateCompat.STATE_BUFFERING -> {
                    nowPlayingFragment.btnPause.setImageResource(R.drawable.baseline_shuffle_white_48)
                }
                PlaybackStateCompat.STATE_PLAYING -> {
                    nowPlayingFragment.btnPause.setImageResource(R.drawable.baseline_pause_white_48)
                }
                PlaybackStateCompat.STATE_PAUSED -> {
                    nowPlayingFragment.btnPause.setImageResource(R.drawable.baseline_play_arrow_white_48)
                }
            }
        }

    }
}
