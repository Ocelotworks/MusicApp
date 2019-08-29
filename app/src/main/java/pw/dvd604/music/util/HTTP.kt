package pw.dvd604.music.util

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType

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

        fun songDetail(id: String): String {
            return "$address$songAPI/$id/details"
        }

        fun getSong(): String {
            return address + songAPI
        }

        fun getDetailedSong(id: String, type: SongDataType): String {
            val typeString: String = when (type) {
                SongDataType.ALBUM -> {
                    "/album/"
                }
                SongDataType.ARTIST -> {
                    "/artist/"
                }
                SongDataType.PLAYLIST -> {
                    "/playlist/"
                }
                SongDataType.GENRE -> {
                    "/genre/"
                }
                else -> {
                    //This shouldn't ever occur
                    "/song/"
                }
            }

            return "$address$songAPI$typeString$id"
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

        fun getQueue(): String? {
            return address + queueAPI
        }

        fun like(id: String?): String {
            return "$address/song/$id/vote/like"
        }

        fun getDetailedData(song: Song): String? {
            val typeString: String = when (song.type) {
                SongDataType.ALBUM -> {
                    "/album/"
                }
                SongDataType.ARTIST -> {
                    "/artist/"
                }
                SongDataType.PLAYLIST -> {
                    "/playlist/"
                }
                SongDataType.GENRE -> {
                    "/genre/"
                }
                else -> {
                    //This shouldn't ever occur
                    "/song/"
                }
            }

            return "$address$songAPI$typeString${song.id}"
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