package pw.dvd604.music.fragment

import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.media.AudioManager
import android.os.Bundle
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
import pw.dvd604.music.MediaService
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.HTTP


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
        // Grab the view for the play/pause button
        val playPause = btnPause.apply {
            setOnClickListener {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                val pbState = mediaController.playbackState.state
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                } else {
                    mediaController.transportControls.play()
                }
            }
        }

        // Display the initial state
        val metadata = mediaController.metadata
        val pbState = mediaController.playbackState

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }



    fun postImage(bmp: Bitmap?) {
        this.view?.findViewById<ImageView>(R.id.songArt)?.setImageBitmap(bmp)
    }


    fun setSong(song: Song, fromService: Boolean = false) {
        val bundle = Bundle()
        bundle.putSerializable("song", song)
        mediaBrowser.sendCustomAction("setSong", bundle, null)
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
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {}

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {}

    }

}
