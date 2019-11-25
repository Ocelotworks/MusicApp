package pw.dvd604.music.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_songs.*
import kotlinx.coroutines.*
import pw.dvd604.music.R
import pw.dvd604.music.data.ArtistSong
import pw.dvd604.music.data.CardData
import pw.dvd604.music.data.adapter.CardRecyclerAdapter
import pw.dvd604.music.data.adapter.ListRecyclerAdapter
import pw.dvd604.music.data.room.dao.ArtistSongJoinDao
import pw.dvd604.music.data.room.dao.BaseDao

enum class ListLayout {
    GRID, LIST
}

class ListFragment(
    private val dao: BaseDao<*>,
    private val title: String,
    private val layout: ListLayout = ListLayout.GRID,
    val artistJoinDao: ArtistSongJoinDao
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
                try {
                    songList.layoutManager = GridLayoutManager(this@ListFragment.context, 3)
                    songList.adapter = CardRecyclerAdapter(this@ListFragment.context!!) {}
                    val adapter = songList.adapter as CardRecyclerAdapter


                    val task = async(Dispatchers.IO) {
                        dao.getAll()
                    }

                    val data = task.await()

                    adapter.setData(data as List<CardData>)

                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("Error", "", e)
                }
            }
        } else if (layout == ListLayout.LIST) {
            //LIST - JUST TEXT
            songList.layoutManager = LinearLayoutManager(context)
            songList.adapter = ListRecyclerAdapter(this@ListFragment.context!!) { }

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val adapter = songList.adapter as ListRecyclerAdapter

                    if (title == "Songs") {
                        val songData: List<ArtistSong> =
                            withContext(Dispatchers.IO) {
                                artistJoinDao.getSongsWithArtists()
                            }

                        val cardData = ArrayList<CardData>(0)

                        songData.forEach { cardData.add(it.toCardData()) }

                        adapter.setData(cardData)
                    } else {
                        val songTask = async(Dispatchers.IO) {
                            dao.getAll()
                        }

                        val songData: List<CardData> = songTask.await() as List<CardData>

                        adapter.setData(songData)
                    }

                    adapter.notifyDataSetChanged()
                } catch (e: Exception) {
                    Log.e("Error", "", e)
                }
            }
        }
    }
}