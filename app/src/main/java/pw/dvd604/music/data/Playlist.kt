package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.Ignore
import org.json.JSONObject

@Entity(tableName = "playlist")
class Playlist(
    id: String = "",
    title: String = "",
    @Ignore
    var image: String = ""
) : CardData(title, id, "playlist", "https://unacceptableuse.com/petify/playlist/") {
    companion object {
        fun parse(obj: JSONObject): Playlist {
            val playlist = Playlist()
            playlist.id = obj.getString("id")
            playlist.title = obj.getString("name")
            return playlist
        }
    }
}