package pw.dvd604.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
import pw.dvd604.music.data.CardData
import pw.dvd604.music.data.adapter.CardRecyclerAdapter
import pw.dvd604.music.data.adapter.ListRecyclerAdapter
import pw.dvd604.music.ui.FastScroller

enum class ListLayout {
    GRID, LIST
}

class ListFragment(
    private val title: String,
    private val layout: ListLayout = ListLayout.GRID,
    private val getData: (() -> ArrayList<CardData>)?,
    private val onClick: ((id: String) -> Unit)? = null
) : Fragment() {

    constructor() : this("", ListLayout.GRID, null, null)

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

        if (layout == ListLayout.GRID) {
            songList.layoutManager = GridLayoutManager(this@ListFragment.context, 3)
            val adapter = CardRecyclerAdapter(this@ListFragment.context!!) {}
            songList.adapter = adapter

            FastScroller(
                songList,
                ContextCompat.getColor(this@ListFragment.context!!, R.color.colorAccent),
                ContextCompat.getColor(this@ListFragment.context!!, R.color.colorPrimaryDark)
            )

            CoroutineScope(Dispatchers.Main).launch {
                val task = async(Dispatchers.Main) {
                    getData?.invoke()
                }

                if (task.await() != null) {
                    adapter.setData(task.await()!!)
                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            songList.layoutManager = LinearLayoutManager(context)
            val adapter = ListRecyclerAdapter(this@ListFragment.context!!) {
                onClick?.invoke(it.id)
            }
            songList.adapter = adapter

            FastScroller(
                songList,
                ContextCompat.getColor(this@ListFragment.context!!, R.color.colorAccent),
                ContextCompat.getColor(this@ListFragment.context!!, R.color.colorPrimaryDark)
            )

            CoroutineScope(Dispatchers.Main).launch {
                val task = async(Dispatchers.Main) {
                    getData?.invoke()
                }

                if (task.await() != null) {
                    adapter.setData(task.await()!!)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}