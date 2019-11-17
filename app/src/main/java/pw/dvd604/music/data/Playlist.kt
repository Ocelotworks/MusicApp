package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "playlist")
class Playlist(
    @PrimaryKey var id: String = "",
    title: String = "",
    @Ignore
    var image: String = ""
) : CardData(title, id, "playlist") {
    companion object {
        fun parse(obj: JSONObject): Playlist {
            val playlist = Playlist()
            playlist.id = obj.getString("id")
            playlist.title = obj.getString("name")
            return playlist
        }
    }
}