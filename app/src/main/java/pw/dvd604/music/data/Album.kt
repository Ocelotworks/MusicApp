package pw.dvd604.music.data

import android.content.ContentValues
import android.database.Cursor
import androidx.room.Entity
import org.json.JSONObject
import pw.dvd604.music.data.storage.DatabaseContract

@Entity(tableName = "album")
class Album(
    id: String = "",
    title: String = "",
    var artistID: String = "",
    var image: String = ""
) : CardData(title, id, "album", "https://unacceptableuse.com/petify/album/") {

    companion object {
        fun parse(obj: JSONObject): Album {
            val album = Album()
            album.id = obj.getString("id")
            album.title = obj.getString("name")
            album.artistID = obj.getString("artistID")
            return album
        }

        fun cursorToArray(query: Cursor): ArrayList<Album> {
            val list = ArrayList<Album>(0)
            with(query) {
                while (moveToNext()) {
                    val album = Album()
                    album.id = getString(getColumnIndexOrThrow("id"))
                    album.title =
                        getString(getColumnIndexOrThrow(DatabaseContract.Album.COLUMN_NAME_NAME))
                    album.artistID =
                        getString(getColumnIndexOrThrow(DatabaseContract.Album.COLUMN_NAME_ARTIST))
                    list.add(album)
                }
            }
            return list
        }
    }

    fun toValues(): ContentValues {
        return ContentValues().apply {
            put("id", id)
            put(DatabaseContract.Album.COLUMN_NAME_NAME, title)
            put(DatabaseContract.Album.COLUMN_NAME_ARTIST, artistID)
        }
    }
}