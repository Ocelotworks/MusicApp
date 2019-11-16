package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(tableName = "playlist")
data class Playlist(
    @PrimaryKey var id: String = "",
    var title: String = "",
    @Ignore val songs: ArrayList<Song>? = null
) {
    companion object {
        fun parse(obj: JSONObject): Playlist {
            val playlist = Playlist()
            playlist.id = obj.getString("id")
            playlist.title = obj.getString("name")
            return playlist
        }
    }
}