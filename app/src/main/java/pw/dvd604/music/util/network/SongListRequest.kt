package pw.dvd604.music.util.network

import com.android.volley.Response
import org.json.JSONArray
import pw.dvd604.music.adapter.data.Media

class SongListRequest(
    val callback: (mediaList: ArrayList<Media>) -> Unit,
    private val responseCallback: ((response: String?) -> Unit)? = null
) : Response.Listener<String> {

    override fun onResponse(response: String?) {
        val songs = ArrayList<Media>()

        val array = JSONArray(response)
        for (i in 0 until array.length()) {
            val songJSON = array.getJSONObject(i)
            val song = Media()
            song.fromJson(songJSON)
            songs.add(song)
        }


        callback(songs)

        responseCallback?.let {
            it(response)
        }
    }

}