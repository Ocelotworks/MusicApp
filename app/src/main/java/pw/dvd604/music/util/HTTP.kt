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

class HTTP(context: Context?) {

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun getReq(url: String?, listener: Response.Listener<String>?) {
        val req = StringRequest(Request.Method.GET, url, listener, Response.ErrorListener { error ->
            Log.e(
                "Volley",
                error.localizedMessage
            )
        })
        queue.add(req)
    }

    fun putReq(url: String, payload: JSONObject?) {
        queue.add(JsonObjectRequest(Request.Method.PUT, url, payload, null, null))
    }
}
