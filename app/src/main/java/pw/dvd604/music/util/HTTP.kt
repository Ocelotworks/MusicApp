package pw.dvd604.music.util

import android.content.Context
import android.text.Editable
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.jetbrains.annotations.Nullable

class HTTP(context: Context?) {

    companion object {
        var address: String? = null
        fun setup(addr: String) {
            address = addr
        }

        private const val songAPI: String = "/api/song"
        private const val searchAPI: String = "/search/query/"
        private const val queueAPI: String = "/templates/songs/shuffleQueue"

        fun songInfo(id: String): String {
            return "$address$songAPI/$id/info"
        }

        fun getSong(): String {
            return address + songAPI
        }

        fun search(term: String): String {
            return address + searchAPI + term
        }

        fun getQueue(): String? {
            return address + queueAPI
        }

        fun like(id: String?): String {
            return "$address/song/$id/vote/like"
        }
    }

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun getReq(url: String?, listener: Response.Listener<String>?) {
        val req = StringRequest(Request.Method.GET, url, listener, null)
        queue.add(req)
    }
}