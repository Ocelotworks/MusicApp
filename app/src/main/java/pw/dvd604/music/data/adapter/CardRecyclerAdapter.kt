package pw.dvd604.music.data.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.cardview_item.view.*
import pw.dvd604.music.R
import pw.dvd604.music.data.CardData

class CardRecyclerAdapter(
    private val context: Context,
    private var data: List<CardData> = ArrayList(0),
    private val listener: (CardData) -> Unit
) : RecyclerView.Adapter<CardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return CardViewHolder(inflater.inflate(R.layout.cardview_item, parent, false))
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) =
        holder.bind(data[position], listener)

    fun setData(newData: ArrayList<CardData>) {
        data = newData
    }
}

class CardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(data: CardData, listener: (CardData) -> Unit) = with(itemView) {


        Glide.with(context).asBitmap().load(data.url + data.id).into(itemImage)

        itemTitle.text = data.title

        setOnClickListener { listener(data) }
    }
}
