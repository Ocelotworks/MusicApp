package pw.dvd604.music.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pw.dvd604.music.MainActivity
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.CardData
import pw.dvd604.music.data.adapter.CardRecyclerAdapter
import pw.dvd604.music.data.adapter.GenericRecyclerAdapter
import pw.dvd604.music.data.adapter.ListRecyclerAdapter
import pw.dvd604.music.ui.FastScroller

enum class ListLayout {
    GRID, LIST
}

class ListFragment(
    private val layout: ListLayout = ListLayout.GRID,
    private val getData: (() -> ArrayList<CardData>)?,
    private val onClick: ((id: String) -> Unit)? = null
) : Fragment() {

    private var oldData: ArrayList<CardData>? = null
    private var oldPosition: Int = 0
    var isInSub = false

    constructor() : this(ListLayout.GRID, null, null)

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
        retainInstance = true
        application = (this.activity as MainActivity).getApp()

        val adapter = if (layout == ListLayout.GRID) {
            songList.layoutManager = GridLayoutManager(this@ListFragment.context, 3)
            CardRecyclerAdapter(this@ListFragment.context!!) { onClick?.invoke(it.id) }
        } else {
            songList.layoutManager = LinearLayoutManager(context)
            ListRecyclerAdapter(this@ListFragment.context!!) {
                onClick?.invoke(it.id)
            }
        }

        songList.adapter = adapter

        FastScroller(
            songList,
            ContextCompat.getColor(this@ListFragment.context!!, R.color.colorAccent),
            ContextCompat.getColor(this@ListFragment.context!!, R.color.colorPrimaryDark)
        )

        GlobalScope.launch {
            val task = async {
                getData?.invoke()
            }

            if (task.await() != null) {
                adapter.data = task.await()!!
                ui {
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun changeFormat(layout: ListLayout) {
        ui {
            songList.adapter = if (layout == ListLayout.GRID) {
                songList.layoutManager = GridLayoutManager(this@ListFragment.context, 3)
                CardRecyclerAdapter(this@ListFragment.context!!) { onClick?.invoke(it.id) }
            } else {
                songList.layoutManager = LinearLayoutManager(context)
                ListRecyclerAdapter(this@ListFragment.context!!) {
                    onClick?.invoke(it.id)
                }
            }
        }
    }

    fun expandData(id: String) {
        isInSub = true

        val adapter = songList.adapter as GenericRecyclerAdapter

        oldData = adapter.data
        oldPosition = adapter.data.indexOf(adapter.data.find { it.id == id })

        GlobalScope.launch {
            val dataTask = async {
                val newData = ArrayList<CardData>(0)
                val item = adapter.data.find { it.id == id }
                Log.e("Test", item?.url)
                if (item != null) {
                    //This is awful and I hate it, but it'll do for now
                    when {
                        item.url.contains("artist") -> {
                            //Must be an artist
                            (this@ListFragment.activity as MainActivity).mContentManager.getSongsFromArtist(
                                id
                            ).forEach { newData.add(it) }
                        }
                        item.url.contains("album") -> {

                            (this@ListFragment.activity as MainActivity).mContentManager.getSongsFromAlbum(
                                id
                            ).forEach {
                                it.id = item.id
                                it.url = item.url
                                newData.add(it)
                            }

                        }
                        item.url.contains("playlist") -> {
                            (this@ListFragment.activity as MainActivity).mContentManager.getPlaylistContents(
                                id
                            ).forEach {
                                newData.add(it)
                            }
                        }
                    }
                }

                newData
            }

            adapter.data = dataTask.await()
            ui { adapter.notifyDataSetChanged() }
        }
    }

    fun onBackPressed(): Boolean {
        if (oldData != null) {
            isInSub = false

            val adapter = songList.adapter as GenericRecyclerAdapter


            adapter.data = oldData as ArrayList<CardData>

            adapter.notifyDataSetChanged()

            songList.scrollToPosition(oldPosition)

            oldData = null
            return false
        }

        return true
    }

    private fun ui(call: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            call()
        }
    }
}