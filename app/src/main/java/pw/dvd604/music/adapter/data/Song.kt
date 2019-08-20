package pw.dvd604.music.adapter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
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
    @Ignore var name: String,
    @Ignore var author: String,
    @PrimaryKey var id: String,
    @Ignore var album: String = "",
    @Ignore var genre: String = "",
    @Ignore var artistID: String = "",
    @Ignore val type: SongDataType = SongDataType.SONG,
    @ColumnInfo(name = "play_count") var plays : Int = 0
) : Serializable {

    constructor() : this("", "", "", "", "", "", SongDataType.SONG, 0)

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