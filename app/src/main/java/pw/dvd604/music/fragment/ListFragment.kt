package pw.dvd604.music.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.data.adapter.CardRecyclerAdapter

class ListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_songs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songList.layoutManager = GridLayoutManager(this@ListFragment.context, 3)
        songList.adapter = CardRecyclerAdapter(this@ListFragment.context!!) {
            Log.e("Clicked", "${it.title} click")
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val adapter = songList.adapter as CardRecyclerAdapter


                val task = async(Dispatchers.IO) {
                    (this@ListFragment.activity as MainActivity).getApp().db.albumDao().getAll()
                }

                val data = task.await()

                adapter.setData(data)

                Log.e("Test", "Done...")

                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("Error", "", e)
            }
        }
    }
}