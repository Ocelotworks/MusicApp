package pw.dvd604.music.fragment

import android.app.Activity
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PorterDuff
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.MediaService
import pw.dvd604.music.R
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.download.BitmapAsync
import java.io.File


class NowPlayingFragment : androidx.fragment.app.Fragment(), SeekBar.OnSeekBarChangeListener {
    private var volumeControlStream: Int = 0
    private lateinit var mediaBrowser: MediaBrowserCompat
    private var http: HTTP? = null
    private var controllerCallback: MediaControllerCompat.Callback = ControllerCallback(this)
    private var shuffleMode: Boolean = Settings.getBoolean(Settings.shuffle)
    private var stopUpdatingSeek: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_playing, container, false)
        http = HTTP(context)

        shuffleMode(false, v)
        v.findViewById<SeekBar>(R.id.songProgress).setOnSeekBarChangeListener(this)
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
        MediaControllerCompat.getMediaController(this.activity as Activity)
            ?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    fun buildTransportControls() {
        val mediaController = MediaControllerCompat.getMediaController(this.activity as Activity)

        // Register a Callback to stay in sync
        mediaController.registerCallback(controllerCallback)
    }

    var lastName: String = ""
    var lastArtist: String = ""

    private fun updateUI(metadata: MediaMetadataCompat?) {
        songName.text = metadata?.description?.title
        songAuthor.text = metadata?.getString(MediaMetadataCompat.METADATA_KEY_ARTIST)
        songDuration.text =
            Util.prettyTime(metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION))

        metadata?.getLong(MediaMetadataCompat.METADATA_KEY_DURATION)?.let {
            songProgress.max = it.toInt()
        }

        songProgessText.text = Util.prettyTime(metadata?.getLong("progress")!! / 1000)
        songProgress.progress = metadata.getLong("progress").toInt() / 1000

        //let the metadata update to here, including progress. Stop if it's not a new song
        if (lastName == songName.text.toString() && lastArtist == songAuthor.text.toString()) return
        //This is because the bitmap decoding code is heavy, and shouldn't be run every second

        lastName = songName.text.toString()
        lastArtist = songAuthor.text.toString()

        val filePath = Util.albumURLToAlbumPath(metadata.description?.iconUri.toString())
        val file = File(filePath)

        Util.log(this, filePath)

        if (file.exists() && Settings.getBoolean(Settings.offlineAlbum)) {
            postImage(BitmapFactory.decodeFile(file.canonicalPath))
        } else {
            BitmapAsync(this).execute(metadata.description?.iconUri.toString())
        }
    }

    fun postImage(bmp: Bitmap?) {
        this.view?.findViewById<ImageView>(R.id.songArt)?.setImageBitmap(bmp)
    }

    fun shuffleMode(change: Boolean = true, v: View? = null) {
        if (change) {
            shuffleMode = !shuffleMode
            Settings.putBoolean(Settings.shuffle, shuffleMode)
        }
        val view: View? = if (this.view != null) {
            this.view
        } else {
            v
        }

        view?.let {
            val colour: Int = resources.getColor(R.color.colorAccent, null)
            val img = it.findViewById<ImageView>(R.id.btnShuffle)

            if (shuffleMode) {
                img.drawable.setColorFilter(colour, PorterDuff.Mode.SRC_IN)
            } else {
                img.drawable.clearColorFilter()
            }
        }

        val bundle = Bundle()
        bundle.putBoolean("shuffle", shuffleMode)
        activity?.mediaController?.sendCommand("shuffle", bundle, null)
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
        stopUpdatingSeek = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        activity?.mediaController?.transportControls?.seekTo(seekBar?.progress!! * 1000.toLong())
        stopUpdatingSeek = false
    }

    fun hideStar() {
        this.view?.findViewById<ImageView>(R.id.btnStar)?.visibility = View.INVISIBLE
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
                MediaControllerCompat.setMediaController(
                    nowPlayingFragment.activity as Activity,
                    mediaController
                )
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

    class ControllerCallback(private val nowPlayingFragment: NowPlayingFragment) :
        MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlayingFragment.updateUI(metadata)
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (nowPlayingFragment.stopUpdatingSeek) {
                state?.position?.let {
                    nowPlayingFragment.songProgress.progress = it.toInt()
                }
            }

            when (state?.state) {
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
