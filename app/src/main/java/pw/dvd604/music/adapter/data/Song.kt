package pw.dvd604.music.adapter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


enum class SongDataType{
    SONG, ARTIST, GENRE, ALBUM
}

/**
 * The 'Song' datatype
 * Despite the name, Song is, in fact, the container for most data coming from the server
 * This is a remnant of early versions of the Petify app, and will likely  be overhauled in the near future
 **/
@Entity
data class Song(
    var name: String,
    var author: String,
    @PrimaryKey var id: String,
    var album: String = "",
    var genre: String = "",
    var artistID: String = "",
    val type : SongDataType = SongDataType.SONG,
    @ColumnInfo(name = "play_count") var plays : Int = 0
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