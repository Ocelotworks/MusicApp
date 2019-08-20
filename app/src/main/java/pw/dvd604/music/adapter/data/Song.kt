package pw.dvd604.music.adapter.data

import java.io.Serializable


enum class SongDataType{
    SONG, ARTIST, GENRE, ALBUM
}

/**
 * The 'Song' datatype
 * Despite the name, Song is, in fact, the container for most data coming from the server
 * This is a remnant of early versions of the Petify app, and will likely  be overhauled in the near future
 **/
data class Song(
    var name: String,
    var author: String,
    var id: String,
    var album: String = "",
    var genre: String = "",
    var artistID: String = "",
    val type : SongDataType = SongDataType.SONG
) : Serializable {

    companion object {
        private const val serialVersionUID = 20180617104400L
    }

    fun generateText(): String {

        val separator: String = if (type != SongDataType.SONG) {
            " "
        } else {
            " - "
        }

        return "$author$separator$name"
    }
}