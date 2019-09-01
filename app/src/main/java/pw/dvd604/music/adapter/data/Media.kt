package pw.dvd604.music.adapter.data

import java.io.Serializable


enum class MediaType {
    SONG, ARTIST, GENRE, ALBUM, PLAYLIST;

    companion object {
        fun getNonSong(): Array<MediaType> {
            return arrayOf(ARTIST, GENRE, ALBUM, PLAYLIST)
        }
    }
}

/**
 * The 'Media' datatype
 **/
data class Media(
    val name: String,
    val author: String,
    val id: String,
    val album: String = "",
    val genre: String = "",
    val artistID: String = "",
    val hash: String = "",
    val type: MediaType = MediaType.SONG
) : Serializable {

    companion object {
        private const val serialVersionUID = 20180617104400L
    }

    fun generateText(): String {

        val separator: String = if (type != MediaType.SONG) {
            " "
        } else {
            " - "
        }

        return "$author$separator$name"
    }
}