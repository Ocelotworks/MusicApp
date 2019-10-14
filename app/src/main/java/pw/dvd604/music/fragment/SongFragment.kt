package pw.dvd604.music.fragment

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.text.Editable
import android.text.TextWatcher
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import com.android.volley.Response
import kotlinx.android.synthetic.main.activity_main.*
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
import java.util.*
import kotlin.collections.ArrayList

class SongFragment : androidx.fragment.app.Fragment(), TextWatcher,
    AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    var http: HTTP? = null
    private var state: Bundle? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(
            R.layout.fragment_songs, container, false
        )

        view.let {
            it.findViewById<EditText>(R.id.songSearch).addTextChangedListener(this)
            it.findViewById<ListView>(R.id.mediaList).onItemClickListener = this
            activity?.registerForContextMenu(it.findViewById(R.id.mediaList))
            it.findViewById<Spinner>(R.id.searchSpinner).onItemSelectedListener = this
            sliding_layout?.setScrollableView(it.findViewById(R.id.mediaList))
        }

        http = HTTP(context)

        state = savedInstanceState

        return view
    }

    fun setSongs(data: ArrayList<Media>? = null) {
        this.activity?.runOnUiThread {
            context?.let { con ->
                if (mediaList == null) {
                    MusicApplication.track("Error", "MediaList was null")
                } else {
                    if (data != null) {
                        mediaList.adapter = SongAdapter(con, data)
                    } else {
                        mediaList.adapter = SongAdapter(con, SongList.songList)
                    }
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
                val mediaType = Util.stringToDataType(searchSpinner.selectedItem as String)

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

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        afterTextChanged(songSearch.editableText)
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
                HTTP.getDetailedData(media), media.name
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

        menu.add(1, v.id, 0, "Add to queue")

        if (media.type == MediaType.SONG) {
            menu.add(2, v.id, 0, "Go to album")
            menu.add(2, v.id, 1, "Go to artist")
            menu.add(2, v.id, 2, "Song info")
        }
        if (media.type == MediaType.PLAYLIST) {
            menu.add(2, v.id, 0, "Copy ID")
        }
        if (Util.isDeveloper()) {
            menu.add(3, v.id, 0, "Blacklist")
        }
        return menu
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

            val searchType =
                Util.stringToDataType(songFragment.searchSpinner.selectedItem as String)
            val array = json.getJSONArray(
                "${(songFragment.searchSpinner.selectedItem as String).toLowerCase(
                    Locale.getDefault()
                )}s"
            )

            for (i in 0 until array.length()) {
                val songJSON = array.getJSONObject(i)
                lateinit var media: Media

                when (searchType) {
                    MediaType.SONG -> media = Media().fromJson(songJSON)
                    MediaType.ARTIST,
                    MediaType.GENRE,
                    MediaType.ALBUM,
                    MediaType.PLAYLIST -> media = Media(
                        songJSON.getString("name"),
                        "",
                        songJSON.getString("id"),
                        "",
                        "",
                        "",
                        "",
                        searchType
                    )
                }

                data.add(media)
            }

            songFragment.setSongs(data)
        }
    }
}
