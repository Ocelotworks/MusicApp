package pw.dvd604.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import pw.dvd604.music.R
import pw.dvd604.music.adapter.SongAdapter
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.Util

class QueueFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_queue, container, false)

        if (Util.mediaQueue.isEmpty())
            Util.mediaQueue.add(Media("No songs in queue"))

        view.findViewById<ListView>(R.id.queueList).adapter =
            SongAdapter(this.context!!, Util.mediaQueue)
        return view
    }
}