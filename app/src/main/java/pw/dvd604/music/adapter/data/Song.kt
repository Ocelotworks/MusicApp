package pw.dvd604.music.adapter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable


enum class SongDataType {
    SONG, ARTIST, GENRE, ALBUM, PLAYLIST
}

/**
 * The 'Song' datatype
 * Despite the name, Song is, in fact, the container for most data coming from the server
 * This is a remnant of early versions of the Petify app, and will likely  be overhauled in the near future
 **/
@Entity
data class Song(
    @ColumnInfo(name = "title") var name: String,
    @ColumnInfo(name = "artist") var author: String,
    @PrimaryKey @ColumnInfo(name = "song_id") var id: String,
    @ColumnInfo(name = "album_id") var album: String = "",
    @ColumnInfo(name = "genre_id") var genre: String = "",
    @ColumnInfo(name = "artist_id") var artistID: String = "",
    @Ignore val type: SongDataType = SongDataType.SONG
) : Serializable {

    constructor() : this("", "", "", "", "", "", SongDataType.SONG)

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