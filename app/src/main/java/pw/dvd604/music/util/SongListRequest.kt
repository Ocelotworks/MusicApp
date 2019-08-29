package pw.dvd604.music.util

import com.android.volley.Response
import org.json.JSONArray
import pw.dvd604.music.adapter.data.Song

class SongListRequest(
    val callback: (songList: ArrayList<Song>) -> Unit,
    val responseCallback: ((response: String?) -> Unit)? = null
) : Response.Listener<String> {

    private val songs = ArrayList<Song>()

    override fun onResponse(response: String?) {
        songs.clear()
        val array = JSONArray(response)
        for (i in 0 until array.length()) {
            val songJSON = array.getJSONObject(i)
            val song = Util.jsonToSong(songJSON)
            songs.add(song)
        }
        callback(songs)

        responseCallback?.let {
            it(response)
        }
    }

}