package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Media
import java.util.*
import kotlin.collections.ArrayList

class SearchHandler {
    companion object {
        fun search(query: String?): List<Media> {

            query?.let {
                return SongList.songList.filter {
                    it.name.toLowerCase(Locale.getDefault())
                        .contains(query.toLowerCase(Locale.getDefault()))
                }
            }
            return ArrayList(0)
        }
    }
}