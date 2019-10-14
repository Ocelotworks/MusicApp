package pw.dvd604.music.util.network

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.Util

class HTTP(context: Context?) {

    companion object {
        private var address: String? = null
        fun setup(addr: String) {
            address = addr
        }

        private const val songAPI: String = "/api/song"
        private const val searchAPI: String = "/search/query/"
        private const val queueAPI: String = "/templates/songs/shuffleQueue"

        fun songInfo(id: String): String {
            return "$address$songAPI/$id/info"
        }

        fun songDetail(id: String): String {
            return "$address$songAPI/$id/details"
        }

        fun getSong(): String {
            return address + songAPI
        }

        fun getAllMedia(value: MediaType): String {
            return "$address/api/${Util.dataTypeToString(value)}"
        }

        fun getAlbum(id: String): String {
            return "$address$songAPI/album/$id"
        }

        fun getArtist(id: String): String {
            return "$address$songAPI/artist/$id"
        }

        fun getGenre(id: String): String {
            return "$address$songAPI/genre/$id"
        }

        fun getPlaylist(id: String): String {
            return "$address$songAPI/playlist/$id"
        }

        fun search(term: String): String {
            return address + searchAPI + term
        }

        @Deprecated(message = "Use local randomisation of songs to ensure offline play works")
        fun getQueue(): String? {
            return address + queueAPI
        }

        fun like(id: String?): String {
            return "$address/song/$id/vote/like"
        }

        fun getDetailedData(media: Media): String {
            val typeString: String = when (media.type) {
                MediaType.ALBUM -> {
                    "/album/"
                }
                MediaType.ARTIST -> {
                    "/artist/"
                }
                MediaType.PLAYLIST -> {
                    "/playlist/"
                }
                MediaType.GENRE -> {
                    "/genre/"
                }
                else -> {
                    throw Exception("getDetailedData used incorrectly")
                }
            }

            return "$address$songAPI$typeString${media.id}"
        }
    }

    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun getReq(url: String?, listener: Response.Listener<String>?) {
        val req = StringRequest(Request.Method.GET, url, listener, null)
        queue.add(req)
    }

    fun putReq(url: String, payload: JSONObject?) {
        queue.add(JsonObjectRequest(Request.Method.PUT, url, payload, null, null))
    }
}