package pw.dvd604.music.util.network

import com.android.volley.Response
import org.json.JSONArray
import org.json.JSONObject
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.Util

class SearchAllListener(
    val type: MediaType,
    val callback: (data: ArrayList<Media>) -> Unit
) : Response.Listener<String> {

    override fun onResponse(response: String?) {
        var array = if (type != MediaType.PLAYLIST) {
            JSONArray(response)


        } else {
            val parent = JSONObject(response)
            parent.getJSONArray("public")
        }

        val mediaList = ArrayList<Media>(0)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val media = Util.jsonToGenericMedia(obj, type)

            mediaList.add(media)
        }

        callback(mediaList)
    }

}