package pw.dvd604.music.util.network

import com.android.volley.Response
import org.json.JSONArray
import org.json.JSONObject
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.Util

class FilterMapRequest(
    private val callback: (mediaList: ArrayList<Media>) -> Unit,
    private val type: MediaType,
    private val rawCallback: ((response: String, type: MediaType) -> Unit)? = null
) : Response.Listener<String> {

    override fun onResponse(response: String?) {
        val media = ArrayList<Media>(0)

        val array = if (type == MediaType.PLAYLIST) {
            JSONObject(response).getJSONArray("public")
        } else {
            JSONArray(response)
        }

        for (i in 0 until array.length()) {
            val itemJson = array.getJSONObject(i)

            media.add(Util.jsonToGenericMedia(itemJson, type))
        }

        callback(media)
        rawCallback?.let { rCB ->
            response?.let {
                rCB(it, type)
            }
        }

    }
}