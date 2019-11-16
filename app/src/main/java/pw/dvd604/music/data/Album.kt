package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "album")
data class Album(
    @PrimaryKey var id: String = "",
    var title: String = "",
    var artistID: String = ""
) {
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