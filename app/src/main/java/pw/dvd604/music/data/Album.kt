package pw.dvd604.music.data

import androidx.room.Entity
import org.json.JSONObject

@Entity(tableName = "album")
class Album(
    id: String = "",
    title: String = "",
    var artistID: String = ""
) : CardData(title, id, "album", "https://unacceptableuse.com/petify/album/") {

    companion object {
        fun parse(obj: JSONObject): Album {
            val album = Album()
            album.id = obj.getString("id")
            album.title = obj.getString("name")
            album.artistID = obj.getString("artistID")
            return album
        }
    }
}