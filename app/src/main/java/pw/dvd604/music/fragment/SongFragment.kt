package pw.dvd604.music.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.text.Editable
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
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.network.HTTP
import pw.dvd604.music.util.network.SearchAllListener
import pw.dvd604.music.util.network.SongListRequest
import java.io.File

class SongFragment : androidx.fragment.app.Fragment(), TextWatcher,
    AdapterView.OnItemClickListener {
    var searchMode: Int = R.id.btnTitle
    var http: HTTP? = null
    //Search modes are how we translate button IDs to JSON array names
    var searchModes = hashMapOf(
        R.id.btnTitle to "songs",
        R.id.btnArtist to "artists",
        R.id.btnGenre to "genres",
        R.id.btnAlbum to "albums"
    )
    private var state: Bundle? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_songs, container, false)

        view.let {
            it.findViewById<EditText>(R.id.songSearch).addTextChangedListener(this)
            it.findViewById<ListView>(R.id.mediaList).onItemClickListener = this
            activity?.registerForContextMenu(it.findViewById(R.id.mediaList))
        }

        http = HTTP(context)

        state = savedInstanceState

        return view
    }

    fun updateSearchMode(btn: Int) {
        searchMode = btn
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

    fun setSongs(data: ArrayList<Media>? = null) {
        this.activity?.runOnUiThread {
            context?.let { con ->
                if (data != null) {
                    mediaList.adapter = SongAdapter(con, data)
                } else {
                    mediaList.adapter = SongAdapter(con, SongList.songList)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mediaList != null)
            outState.putInt("scrolly", mediaList.firstVisiblePosition)

        super.onSaveInstanceState(outState)
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(text: Editable?) {
        text?.let {
            if (it.isEmpty()) {
                val mediaType = Util.viewIDToDataType(searchMode)

                if (mediaType != MediaType.SONG) {
                    http?.getReq(
                        HTTP.getAllMedia(mediaType),
                        SearchAllListener(mediaType, ::setSongs)
                    )
                } else {
                    setSongs()
                }
                return
            }
            http?.getReq(HTTP.search(it.toString()), SearchListener(this))
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onItemClick(adapter: AdapterView<*>?, v: View?, position: Int, id: Long) {
        val songAdapter = adapter?.adapter as SongAdapter
        val media: Media = songAdapter.getItemAtPosition(position)
        val activity = this.activity as MainActivity

        MusicApplication.track("Media play", media.toJson().toString())

        if (media.type == MediaType.SONG) {
            MediaControllerCompat.getMediaController(activity)
                .transportControls.prepareFromUri(Uri.parse(media.toUrl()), null)

        } else {
            //Open sub media fragment
            (this.activity as MainActivity).createSubFragment(
                HTTP.getDetailedSong(
                    media.id,
                    media.type
                ), media.name
            )
        }
    }

    fun buildContext(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ): ContextMenu {
        val position: Int = (menuInfo as AdapterView.AdapterContextMenuInfo).position
        val songAdapter = mediaList.adapter as SongAdapter
        val media: Media = songAdapter.getItemAtPosition(position)

        if (!Util.downloader.hasSong(media)) {
            menu.add(0, v.id, 0, "Download")
        } else {
            menu.add(0, v.id, 0, "Remove from local storage")
        }
        menu.add(0, v.id, 0, "Add to queue")
        if (media.type == MediaType.SONG) {
            menu.add(0, v.id, 0, "Go to album")
            menu.add(0, v.id, 0, "Go to artist")
            menu.add(0, v.id, 0, "Media info")
        }
        return menu
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        val position: Int = (item?.menuInfo as AdapterView.AdapterContextMenuInfo).position
        val songAdapter = mediaList.adapter as SongAdapter
        val media: Media = songAdapter.getItemAtPosition(position)

        when (item.title) {
            "Download" -> {
                if (media.type == MediaType.SONG) {
                    Util.downloader.addToQueue(media)
                    Util.downloader.doQueue()
                } else {
                    http?.getReq(
                        HTTP.getDetailedData(media),
                        SongListRequest(::setContextSongs)
                    )
                }

                MusicApplication.track("Media Download", media.generateText())
            }
            "Remove from local storage" -> {
                if (Util.downloader.hasSong(media)) {
                    val file = File(media.toPath())
                    file.delete()

                    Util.report("Deleted song!", this.activity as MainActivity, true)

                    MusicApplication.track("Media Delete", media.generateText())
                }
            }
            "Add to queue" -> {
                (activity as MainActivity).report("Not yet implemented", true)
            }
            "Go to album" -> {
                (activity as MainActivity).createSubFragment(
                    HTTP.getAlbum(media.album),
                    media.name
                )
            }
            "Go to artist" -> {
                (activity as MainActivity).createSubFragment(
                    HTTP.getArtist(media.artistID),
                    media.author
                )
            }
            "Media info" -> {
                (activity as MainActivity).createDetailFragment(media)
            }

        }

        return true
    }

    private fun setContextSongs(media: ArrayList<Media>) {
        for (s in media) {
            Util.downloader.addToQueue(s)
        }
        Util.downloader.doQueue()
    }

    fun downloadAll() {
        Util.report("Checking previously downloaded songs", this.activity as MainActivity, true)
        var i = 0
        for (song in SongList.songList) {
            if (!Util.downloader.hasSong(song)) {
                Util.downloader.addToQueue(song)
            } else {
                i++
            }
        }
        Util.report("Skipping $i songs. Starting download", this.activity as MainActivity, true)
        Util.downloader.doQueue()
    }
    //HTTP req listeners below

    class SearchListener(private val songFragment: SongFragment) : Response.Listener<String> {
        override fun onResponse(response: String?) {
            val data = ArrayList<Media>()
            val json = JSONObject(response)
            val array = json.getJSONArray(songFragment.searchModes[songFragment.searchMode])

            for (i in 0 until array.length()) {
                val songJSON = array.getJSONObject(i)
                var media = Media()

                when (songFragment.searchMode) {
                    R.id.btnTitle -> {
                        media.fromJson(json)
                    }
                    R.id.btnArtist,
                    R.id.btnGenre,
                    R.id.btnAlbum -> {
                        media = Media(
                            songJSON.getString("name"),
                            "",
                            songJSON.getString("id"),
                            "",
                            "",
                            "",
                            "",
                            Util.viewIDToDataType(songFragment.searchMode)
                        )
                    }
                }

                data.add(media)
            }
            songFragment.setSongs(data)
        }
    }
}
