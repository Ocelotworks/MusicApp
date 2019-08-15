package pw.dvd604.music.util

import org.json.JSONObject
import pw.dvd604.music.adapter.data.Song
import java.lang.Exception

class Util{
    companion object{
        fun prettyTime(seconds : Int) : String{
            val mins : Int = (seconds % 3600 / 60)
            val secs : Int = seconds % 60
            return "$mins:$secs"
        }

        fun jsonToSong(json: JSONObject): Song {
            try {
                val song = Song(
                    json.getString("title"),
                    json.getString("name"),
                    json.getString("song_id"),
                    json.getString("album"),
                    "",
                    json.getString("artist_id")
                )
                return song
            } catch(e : Exception) {
                val song = Song(
                    json.getString("title"),
                    json.getString("artist"),
                    json.getString("id"),
                    json.getString("album"),
                    "",
                    json.getString("artistID")
                )
                return song
            }
        }

        fun songToUrl(song: Song?): String {
            return "https://unacceptableuse.com/petify/song/${song?.id}"
        }
    }
}
