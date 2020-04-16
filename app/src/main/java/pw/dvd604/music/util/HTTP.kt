package pw.dvd604.music.util

import android.content.Context
import android.util.Log
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.service.MediaContainer


class HTTP(val context: Context?) {

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

    fun putReq(
        url: String,
        payload: JSONObject?,
        listener: Response.Listener<JSONObject>? = null,
        error: Response.ErrorListener? = null
    ) {
        queue.add(JsonObjectRequest(Request.Method.PUT, url, payload, listener, error))
    }

    fun putReq(url: String, listener: Response.Listener<String>? = null): CodeRequest {
        val request = CodeRequest(Request.Method.PUT, url, listener, null)

        queue.add(request)

        return request
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

        val url = "${BuildConfig.defaultURL}song/${MediaContainer.songID}/like"
        val listener = CodeListener(this, url)

        val request = this.putReq(url, listener)
        listener.request = request
    }

    fun postDislike() {
        val apiKey = Settings.getSetting(Settings.api)

        if (apiKey == "")
            return

        val url = "${BuildConfig.defaultURL}song/${MediaContainer.songID}/dislike"
        val listener = CodeListener(this, url)

        val request = this.putReq(url, listener)
        listener.request = request
    }
}

class CodeListener(val http: HTTP, private val url: String) : Response.Listener<String> {
    var request: CodeRequest? = null

    override fun onResponse(response: String?) {
        if (request?.statusCode == 201) {
            val id = url.split("/")[url.split("/").size - 1]

            GlobalScope.launch {
                val sql =
                    "UPDATE ${DatabaseContract.Opinion.TABLE_NAME} SET ${DatabaseContract.Opinion.COLUMN_NAME_SENT} = 1 WHERE id = $id"
                (http.context?.applicationContext as MusicApplication).database.execSQL(sql)
            }
        }
    }

}

class CodeRequest(
    method: Int,
    url: String,
    listener: Response.Listener<String>?,
    errorListener: Response.ErrorListener?
) : StringRequest(method, url, listener, errorListener) {

    var statusCode = 0

    override fun parseNetworkError(volleyError: VolleyError?): VolleyError {
        statusCode = volleyError?.networkResponse?.statusCode ?: 0
        return super.parseNetworkError(volleyError)
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
        statusCode = response?.statusCode ?: 0
        return super.parseNetworkResponse(response)
    }

}
