package pw.dvd604.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.android.volley.Response
import org.json.JSONObject
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.network.HTTP

class SongDetailFragment : Fragment(), Response.Listener<String> {
    companion object {
        fun create(media: Media): SongDetailFragment {
            val bundle = Bundle()
            bundle.putSerializable("media", media)
            val frag = SongDetailFragment()
            frag.arguments = bundle
            return frag
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sub_songs, container, false)
    }

    override fun onStart() {
        super.onStart()

        val song = this.arguments?.getSerializable("media") as Media

        HTTP(this.context)
            .getReq(HTTP.songDetail(song.id), this)

        this.view?.findViewById<TextView>(R.id.subSongTitle)?.text = song.generateText()
    }

    override fun onResponse(response: String?) {

        val json = JSONObject(response)
        val array = arrayOf(
            "Title: ${json["title"]}",
            "Artist: ${json["artist_name"]}",
            "Album: ${json["album_name"]}",
            "Genre: ${json["genre_name"]}",
            "Duration: ${Util.prettyTime(json["duration"] as Int)}",
            "----------",
            "Path: ${json["path"]}",
            "Media ID: ${json["song_id"]}",
            "Album ID: ${json["album_id"]}",
            "Artist ID: ${json["artist_id"]}",
            "Genre ID: ${json["genre_id"]}"
        )

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this.context!!, android.R.layout.simple_list_item_1, array)

        this.view?.findViewById<ListView>(R.id.subSongList)?.adapter = adapter
    }
}