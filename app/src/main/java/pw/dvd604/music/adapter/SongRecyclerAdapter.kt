package pw.dvd604.music.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Media

class SongRecyclerAdapter(private val dataSet: ArrayList<Media>) :
    RecyclerView.Adapter<MediaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_songlist_recycler, parent, false)

        return MediaViewHolder(textView as TextView)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.textView.text = dataSet[position].generateText()
    }

}