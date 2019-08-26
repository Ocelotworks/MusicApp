package pw.dvd604.music.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.*
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_songs.*
import org.json.JSONObject
import pw.dvd604.music.MainActivity
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.adapter.SongAdapter
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.SongListRequest
import pw.dvd604.music.util.Util

class SongFragment : androidx.fragment.app.Fragment(), TextWatcher, AdapterView.OnItemClickListener {
    var searchMode: Int = R.id.btnTitle
    var http: HTTP? = null
    //Array of our songs
    var songData = ArrayList<Song>(0)
    //Search modes are how we translate button IDs to JSON array names
    var searchModes = hashMapOf(
        R.id.btnTitle to "songs",
        R.id.btnArtist to "artists",
        R.id.btnGenre to "genres",
        R.id.btnAlbum to "albums"
    )
    var createCount: Int = 0
    var state: Bundle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        view.let {
            it.findViewById<EditText>(R.id.songSearch).addTextChangedListener(this)
            it.findViewById<ListView>(R.id.songList).onItemClickListener = this
            activity?.registerForContextMenu(it.findViewById(R.id.songList))
        }

        http = HTTP(context)

        if (createCount == 0) {
            pullSongs()
        }

        state = savedInstanceState

        createCount++

        return view
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
        http?.getReq(HTTP.getSong(), SongListRequest(::setSongs))
    }

    fun setSongs(songs : ArrayList<Song>) {
        songData = songs
        context?.let {
            songList.adapter = SongAdapter(it, songs)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("scrolly", songList.firstVisiblePosition)

        super.onSaveInstanceState(outState)
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

        MusicApplication.track("Song play", Util.songToJson(song).toString())


        if(song.type == SongDataType.SONG) {
            //activity.setSong(song)
            val bundle = Bundle()
            bundle.putSerializable("song", song)
            MediaControllerCompat.getMediaController(activity)
                .transportControls.prepareFromUri(Uri.parse(Util.songToUrl(song)), bundle)
        } else {
            //Open sub song fragment
            (this.activity as MainActivity).createSubFragment(
                HTTP.getDetailedSong(
                    song.id,
                    song.type
                ), song.name
            )
        }
    }

    fun reset() {
        this.view?.findViewById<EditText>(R.id.songSearch)?.text = SpannableStringBuilder("")
    }

    fun buildContext(menu: ContextMenu, v: View): ContextMenu {
        menu.add(0, v.id, 0, "Download")
        menu.add(0, v.id, 0, "Add to queue")
        menu.add(0, v.id, 0, "Go to album")
        menu.add(0, v.id, 0, "Go to artist")
        menu.add(0, v.id, 0, "Song info")
        return menu
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val position: Int = (item?.menuInfo as AdapterView.AdapterContextMenuInfo).position
        val songAdapter = songList.adapter as SongAdapter
        val song: Song = songAdapter.getItemAtPosition(position)

        when (item.title) {
            "Download" -> {
                Util.downloader.addToQueue(song)
                Util.downloader.doQueue()
            }
            "Add to queue" -> {
                (activity as MainActivity).report("Not yet implemented", true)
            }
            "Go to album" -> {
                (activity as MainActivity).createSubFragment(HTTP.getAlbum(song.album), song.name)
            }
            "Go to artist" -> {
                (activity as MainActivity).createSubFragment(
                    HTTP.getArtist(song.artistID),
                    song.author
                )
            }
            "Song info" -> {
                (activity as MainActivity).createDetailFragment(song)
            }
        }

        return true
    }


    //HTTP req listeners below

    class SearchListener(private val songFragment: SongFragment) : Response.Listener<String> {
        override fun onResponse(response: String?) {
            val data = ArrayList<Song>()
            songFragment.songData.clear()
            val json = JSONObject(response)
            val array = json.getJSONArray(songFragment.searchModes[songFragment.searchMode])

            for (i in 0 until array.length()) {
                val songJSON = array.getJSONObject(i)
                var song: Song? = null

                when (songFragment.searchMode) {
                    R.id.btnTitle -> {
                        song = Util.jsonToSong(songJSON)
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
                            Util.viewIDToDataType(songFragment.searchMode)
                        )
                    }
                }

                song?.let { data.add(it) }

            }
            songFragment.setSongs(data)
        }
    }
}
