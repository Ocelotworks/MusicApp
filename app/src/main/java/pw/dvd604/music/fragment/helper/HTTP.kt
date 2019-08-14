package pw.dvd604.music.fragment.helper

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class HTTP(context : Context?){

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    val getSong : String = "/api/song"
    val search: String = "/search/query/"

    fun getReq(url : String, listener : Response.Listener<String>){
        val req = StringRequest(Request.Method.GET, url, listener, null)
        queue.add(req)
    }
}