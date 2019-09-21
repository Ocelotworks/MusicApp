package pw.dvd604.music.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_sub_songs.*
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.SongAdapter
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.network.HTTP
import pw.dvd604.music.util.network.SongListRequest

class SubSongFragment : Fragment(), AdapterView.OnItemClickListener {

    var http: HTTP? = null
    lateinit var mediaList: ArrayList<Media>

    companion object {
        fun create(url: String, name: String): SubSongFragment {
            val data = Bundle()
            data.putString("url", url)
            data.putString("name", name)

            val frag = SubSongFragment()
            frag.arguments = data
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            if (!Settings.getBoolean(Settings.forceExperimentalLayouts)) {
                R.layout.fragment_sub_songs
            } else {
                R.layout.fragment_sub_songs_experimental
            }, container, false
        )
    }

    override fun onStart() {
        super.onStart()
        val data = this.arguments
        subSongTitle.text = data?.getString("name")
        subSongList.onItemClickListener = this

        http = HTTP(this.context)
        http?.getReq(
            data?.getString("url"),
            SongListRequest(::setSongs)
        )
    }

    private fun setSongs(media: ArrayList<Media>) {
        mediaList = media

        this.context?.let {
            subSongList.adapter = SongAdapter(it, mediaList)
        }
    }

    override fun onItemClick(adapter: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val songAdapter = adapter?.adapter as SongAdapter
        val media: Media = songAdapter.getItemAtPosition(position)
        val activity = this.activity as MainActivity

        Util.mediaQueue = mediaList
        val songBundle = Bundle()
        songBundle.putSerializable("media", media)
        activity.mediaController.transportControls.sendCustomAction("setQueue", null)
        activity.mediaController.transportControls.prepareFromUri(
            Uri.parse(media.toUrl()),
            songBundle
        )
    }
}