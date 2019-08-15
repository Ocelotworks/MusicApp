package pw.dvd604.music.adapter.data

import android.view.View
import android.widget.TextView
import pw.dvd604.music.R
import java.io.Serializable

data class Song(
    var name: String,
    var author: String,
    var id: String,
    var album: String = "",
    var genre: String = "",
    var artistID: String = "",
    val hideDash: Boolean = false
) : Serializable{

    companion object {
        private const val serialVersionUID = 20180617104400L
    }

    fun generateText(view: View): View {
        var text = "%author% - %name%"
        if(hideDash)
            text = "%author% %name%"
        text = text.replace("%author%", author)
        text = text.replace("%name%", name)

        view.findViewById<TextView>(R.id.songText).text = text
        return view
    }
}