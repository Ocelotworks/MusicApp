package pw.dvd604.music.fragment

import android.app.Activity
import android.content.*
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_playing.*
import org.json.JSONObject
import pw.dvd604.music.MediaService
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.BitmapAsync
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.MediaServiceOld
import pw.dvd604.music.util.Util


class NowPlayingFragment : Fragment() {

    companion object {
        private const val intentRoot  = "pw.dvd604.music.service"
        const val songIntent = "$intentRoot.song"
        const val timingIntent = "$intentRoot.timing"
        const val updateIntent = "$intentRoot.update"
    }

    private lateinit var mediaBrowser: MediaBrowserCompat
    private var http: HTTP? = null

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
    }

    override fun onStop() {
        super.onStop()
        mediaBrowser.disconnect()
    }


    fun postImage(bmp: Bitmap?) {
        this.view?.findViewById<ImageView>(R.id.songArt)?.setImageBitmap(bmp)
    }


    fun setSong(song: Song, fromService: Boolean = false) {
        val bundle = Bundle()
        bundle.putSerializable("song",song)
        mediaBrowser.sendCustomAction("setSong", bundle, null)
    }

    class ConnectionCallback(private val nowPlayingFragment: NowPlayingFragment) : MediaBrowserCompat.ConnectionCallback() {

    }

}