package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "artist")
data class Artist(
    @PrimaryKey var id: String = "",
    var title: String = "",
    @Ignore val songs: ArrayList<Song>? = null
) {
    companion object {
        fun parse(obj: JSONObject): Artist {
            val artist = Artist()
            artist.id = obj.getString("id")
            artist.title = obj.getString("name")
            return artist
        }
    }
}