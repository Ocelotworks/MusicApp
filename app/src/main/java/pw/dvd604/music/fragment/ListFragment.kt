package pw.dvd604.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pw.dvd604.music.MainActivity
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.CardData
import pw.dvd604.music.data.adapter.CardRecyclerAdapter
import pw.dvd604.music.data.adapter.ListRecyclerAdapter
import pw.dvd604.music.data.storage.DatabaseContract

enum class ListLayout {
    GRID, LIST
}

class ListFragment(
    private val title: String,
    private val layout: ListLayout = ListLayout.GRID
) : Fragment() {

    constructor() : this("")

    private lateinit var application: MusicApplication

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        application = (this.activity as MainActivity).getApp()

        //GRID - Pictures
        if (layout == ListLayout.GRID) {
            CoroutineScope(Dispatchers.Main).launch {
                songList.layoutManager = GridLayoutManager(this@ListFragment.context, 3)
                songList.adapter = CardRecyclerAdapter(this@ListFragment.context!!) {}

                var adapter = songList.adapter as CardRecyclerAdapter

                val task = async(Dispatchers.IO) {
                    Album.cursorToArray(
                        application.readableDatabase.query(
                            DatabaseContract.Album.TABLE_NAME,
                            null,
                            null,
                            null,
                            null,
                            null,
                            "${DatabaseContract.Album.COLUMN_NAME_NAME} ASC"
                        )
                    )
                }

                val data = task.await()
                adapter.setData(data)
                adapter.notifyDataSetChanged()
            }
        } else if (layout == ListLayout.LIST) {
            //LIST - JUST TEXT
            songList.layoutManager = LinearLayoutManager(context)
            songList.adapter = ListRecyclerAdapter(this@ListFragment.context!!) {
                (this.activity as MainActivity).controllerHandler.play(it.id)
            }

            CoroutineScope(Dispatchers.Main).launch {
                val adapter = songList.adapter as ListRecyclerAdapter
                when (title) {
                    "Songs" -> {
                        val task = async {
                            (activity as MainActivity).mContentManager.getSongsWithArtists()
                        }

                        adapter.setData(task.await())
                        adapter.notifyDataSetChanged()
                    }
                    "Artists" -> {
                        val task = async(Dispatchers.IO) {
                            Artist.cursorToArray(
                                application.readableDatabase.query(
                                    DatabaseContract.Artist.TABLE_NAME,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    "${DatabaseContract.Artist.COLUMN_NAME_NAME} ASC"
                                )
                            )
                        }

                        val artists = task.await()
                        adapter.setData(artists as ArrayList<CardData>)
                        adapter.notifyDataSetChanged()
                    }
                    "Playlist" -> {
                        val data = ArrayList<CardData>(0)
                        data.add(CardData("To do", "123", "Placeholder", "//"))
                        adapter.setData(data)
                        adapter.notifyDataSetChanged()
                    }
                }

            }
        }
    }
}