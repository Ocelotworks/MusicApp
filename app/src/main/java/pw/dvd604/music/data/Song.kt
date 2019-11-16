package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "song")
data class Song(
    @PrimaryKey var id: String = "",
    var title: String = "",
    var duration: Int = 0,
    var hash: String = ""
) {

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