package pw.dvd604.music.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song

open class SongAdapter(context: Context, list: ArrayList<Song>, resource: Int = R.layout.item_songlist) : ArrayAdapter<Song>(context, resource, list){

    private var resource: Int = R.layout.item_songlist
    private var list: ArrayList<Song>
    private var inflater: LayoutInflater


    init{
        this.resource = resource
        this.list = list
        this.inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view : View = convertView ?: inflater.inflate(resource, null)
        /*
        view.findViewById<LinearLayout>(R.id.layoutList).isLongClickable = true
        view.findViewById<LinearLayout>(R.id.layoutList).isClickable = true
        view.findViewById<LinearLayout>(R.id.layoutList).descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS*/

        val song: Song = list[position]

        view.findViewById<TextView>(R.id.songText).text = song.generateText()

        return view
    }

    fun getItemAtPosition(position: Int) : Song{
        return list[position]
    }

}