package pw.dvd604.music.data

import android.content.ContentValues
import android.database.Cursor
import org.json.JSONObject
import pw.dvd604.music.data.storage.DatabaseContract

class Artist(
    id: String = "",
    title: String = ""
) : CardData(title, id, "artist", "https://unacceptableuse.com/petify/artist/") {
    companion object {
        fun parse(obj: JSONObject): Artist {
            val artist = Artist()
            artist.id = obj.getString("id")
            artist.title = obj.getString("name")
            return artist
        }

        fun cursorToArray(query: Cursor): ArrayList<Artist> {
            val list = ArrayList<Artist>(0)
            with(query) {
                while (moveToNext()) {
                    val artist = Artist()
                    artist.id = getString(getColumnIndexOrThrow("id"))
                    artist.title =
                        getString(getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME))
                    list.add(artist)
                }
            }
            return list
        }
    }

    fun toValues(): ContentValues {
        return ContentValues().apply {
            put("id", id)
            put(DatabaseContract.Artist.COLUMN_NAME_NAME, title)
        }
    }
}