package pw.dvd604.music.util

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.service.MediaContainer


class HTTP(context: Context?) {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun getReq(url: String?, listener: Response.Listener<String>?) {
        val req =
            object : StringRequest(Method.GET, url, listener, Response.ErrorListener { error ->
            try {
                Log.e(
                    "Volley",
                    error.localizedMessage
                )
            } catch (ignored: Exception) {
            }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()

                    val apiKey = Settings.getSetting(Settings.api)

                    if (apiKey != "")
                        headers["Authorization"] = "Bearer $apiKey"

                    headers["User-Agent"] =
                        "PetifyApp V${BuildConfig.VERSION_NAME} Build:${BuildConfig.VERSION_CODE}"
                    return headers
                }
            }

        queue.add(req)

    }

    fun postReq(url: String) {
        queue.add(StringRequest(Request.Method.POST, url, null, null))
    }

    fun putReq(url: String, payload: JSONObject?) {
        queue.add(JsonObjectRequest(Request.Method.PUT, url, payload, null, null))
    }

    fun postSkip() {
        val apiKey = Settings.getSetting(Settings.api)

        if (apiKey == "")
            return

        this.postReq("${BuildConfig.defaultURL}song/${MediaContainer.songID}")
    }

    fun postLike() {
        val apiKey = Settings.getSetting(Settings.api)

        if (apiKey == "")
            return

        this.putReq("${BuildConfig.defaultURL}song/${MediaContainer.songID}/like", null)
    }

    fun postDislike() {
        val apiKey = Settings.getSetting(Settings.api)

        if (apiKey == "")
            return

        this.putReq("${BuildConfig.defaultURL}song/${MediaContainer.songID}/dislike", null)
    }
}
