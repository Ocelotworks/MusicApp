package pw.dvd604.music.data

import android.content.ContentValues
import androidx.room.Entity
import org.json.JSONObject
import pw.dvd604.music.data.storage.DatabaseContract

@Entity(tableName = "playlist")
class Playlist(
    id: String = "",
    title: String = "",
    var image: String = ""
) : CardData(title, id, "playlist", "https://unacceptableuse.com/petify/playlist/") {
    fun toValues(): ContentValues {
        return ContentValues().apply {
            put("id", id)
            put(DatabaseContract.Playlist.COLUMN_NAME_NAME, title)
        }
    }

    companion object {
        fun parse(obj: JSONObject): Playlist {
            val playlist = Playlist()
            playlist.id = obj.getString("id")
            playlist.title = obj.getString("name")
            return playlist
        }
    }
}