package pw.dvd604.music.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_songs.*
import org.json.JSONArray
import org.json.JSONObject
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.SongAdapter
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.HTTP


class SongFragment : Fragment(), TextWatcher, AdapterView.OnItemClickListener {

    var searchMode: Int = R.id.btnTitle
    var http: HTTP? = null
    //Array of our songs
    var songs = ArrayList<Song>(0)
    //Search modes are how we translate button IDs to JSON array names
    var searchModes = hashMapOf(
        R.id.btnTitle to "songs",
        R.id.btnArtist to "artists",
        R.id.btnGenre to "genres",
        R.id.btnAlbum to "albums"
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_songs, container, false)

        //Tell the search text box to tell us when it's changed
        v.findViewById<EditText>(R.id.songSearch).addTextChangedListener(this)
        v.findViewById<ListView>(R.id.songList).onItemClickListener = this

        http = HTTP(context)
        pullSongs()

        return v
    }

    fun changeTextColour(btn: Int) {
        this.view?.let {
            val buttons = arrayOf(R.id.btnTitle, R.id.btnAlbum, R.id.btnGenre, R.id.btnArtist)

            for (id in buttons) {
                val button: Button = it.findViewById(id)

                if (id == btn) {
                    button.setTextColor(resources.getColor(R.color.colorPrimaryDark, null))
                } else {
                    button.setTextColor(resources.getColor(R.color.colorMainText, null))
                }
            }

            afterTextChanged(songSearch.editableText)
        }
    }

    private fun pullSongs() {
        http?.getReq(HTTP.getSong(), PullSongListener(this))
    }

    private fun setSongs() {
        context?.let {
            songList.adapter = SongAdapter(it, songs)
        }
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(text: Editable?) {
        text?.let {
            if (it.isEmpty()) {
                pullSongs()
                return
            }
            http?.getReq(HTTP.search(it.toString()), SearchListener(this))
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onItemClick(adapter: AdapterView<*>?, v: View?, position: Int, id: Long) {
        val songAdapter = adapter?.adapter as SongAdapter
        val song : Song = songAdapter.getItemAtPosition(position)

        val activity = this.activity as MainActivity
        activity.setSong(song)
    }


    //HTTP req listeners below


    class PullSongListener(private val songFragment: SongFragment) : Response.Listener<String> {
        override fun onResponse(response: String?) {
            songFragment.songs.clear()
            val array = JSONArray(response)
            for (i in 0 until array.length()) {
                val songJSON = array.getJSONObject(i)
                val song = Song(
                    songJSON.getString("title"),
                    songJSON.getString("name"),
                    songJSON.getString("song_id"),
                    songJSON.getString("album"),
                    "",
                    songJSON.getString("artist_id")
                )
                songFragment.songs.add(song)
            }

            songFragment.setSongs()
        }
    }

    class SearchListener(private val songFragment: SongFragment) : Response.Listener<String> {
        override fun onResponse(response: String?) {
            songFragment.songs.clear()
            val json = JSONObject(response)
            val array = json.getJSONArray(songFragment.searchModes[songFragment.searchMode])

            for (i in 0 until array.length()) {
                val songJSON = array.getJSONObject(i)
                var song: Song? = null

                when (songFragment.searchMode) {
                    R.id.btnTitle -> {
                        song = Song(
                            songJSON.getString("title"),
                            songJSON.getString("name"),
                            songJSON.getString("song_id"),
                            songJSON.getString("album"),
                            "",
                            songJSON.getString("artist_id")
                        )
                    }
                    R.id.btnArtist,
                    R.id.btnGenre,
                    R.id.btnAlbum -> {
                        song = Song(
                            songJSON.getString("name"),
                            "",
                            songJSON.getString("id"),
                            "",
                            "",
                            "",
                            true
                        )
                    }
                }

                song?.let { songFragment.songs.add(it) }

            }
            songFragment.setSongs()
        }
    }
}
