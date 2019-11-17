package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "artist")
class Artist(
    @PrimaryKey var id: String = "",
    title: String = "",
    @Ignore
    var image: String = ""
) : CardData(title, id, "artist") {
    companion object {
        fun parse(obj: JSONObject): Artist {
            val artist = Artist()
            artist.id = obj.getString("id")
            artist.title = obj.getString("name")
            return artist
        }
    }
}