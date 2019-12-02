package pw.dvd604.music.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.listview_item.view.*
import pw.dvd604.music.R
import pw.dvd604.music.data.CardData

class ListRecyclerAdapter(
    private val context: Context,
    private var data: ArrayList<CardData> = ArrayList(0),
    private val listener: (CardData) -> Unit
) : RecyclerView.Adapter<ListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ListViewHolder(inflater.inflate(R.layout.listview_item, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) =
        holder.bind(data[position], listener)

    fun setData(newData: ArrayList<CardData>) {
        data = newData
    }

    fun addData(cardData: CardData) {
        data.add(cardData)
    }
}

class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: CardData, listener: (CardData) -> Unit) = with(itemView) {
        bodyText.text = data.title
        subText.text = data.subtext

        setOnClickListener { listener(data) }
    }
}
