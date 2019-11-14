package pw.dvd604.music.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Song

@Entity(
    tableName = "album_song_join",
    primaryKeys = ["albumID", "songID"],
    foreignKeys = [ForeignKey(
        entity = Album::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("albumID")
    ), ForeignKey(
        entity = Song::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("songID")
    )]
)
data class AlbumSongJoin(val albumID: Int, val songID: Int) {

}