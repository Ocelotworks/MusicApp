package pw.dvd604.music.data.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import pw.dvd604.music.data.CardData

abstract class GenericRecyclerAdapter<E : RecyclerView.ViewHolder?>(
    private val context: Context,
    open var data: ArrayList<CardData> = ArrayList(0),
    private val listener: (CardData) -> Unit
) : RecyclerView.Adapter<E>() {
    override fun getItemCount(): Int = data.size
}
