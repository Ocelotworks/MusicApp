package pw.dvd604.music.data

import androidx.room.Entity
import org.json.JSONObject

@Entity(tableName = "song")
class Song(
    id: String = "",
    title: String = "",
    var duration: Int = 0,
    var hash: String = ""
) : CardData(title, id, "song", "https://unacceptableuse.com/petify/album/") {

    companion object {
        fun parse(obj: JSONObject): Song {
            val song = Song()
            song.id = obj.getString("id")
            song.title = obj.getString("title")
            song.duration = obj.getInt("duration")
            song.hash = obj.getString("hash")
            return song
        }
    }
}