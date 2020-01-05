package pw.dvd604.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.data.adapter.ListRecyclerAdapter

enum class ListLayout {
    GRID, LIST
}

class ListFragment(
    private val title: String,
    private val layout: ListLayout = ListLayout.GRID
) : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //GRID - Pictures
        if (layout == ListLayout.GRID) {
            CoroutineScope(Dispatchers.Main).launch {

            }
        } else if (layout == ListLayout.LIST) {
            //LIST - JUST TEXT
            songList.layoutManager = LinearLayoutManager(context)
            songList.adapter = ListRecyclerAdapter(this@ListFragment.context!!) {
                (this.activity as MainActivity).controllerHandler.play(it.id)
            }

            CoroutineScope(Dispatchers.Main).launch {

            }
        }
    }
}