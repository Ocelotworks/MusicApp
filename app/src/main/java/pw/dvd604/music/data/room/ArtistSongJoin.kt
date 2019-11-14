package pw.dvd604.music.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import pw.dvd604.music.data.Playlist
import pw.dvd604.music.data.Song

@Entity(
    tableName = "artist_song_join",
    primaryKeys = ["artistID", "songID"],
    foreignKeys = [ForeignKey(
        entity = Playlist::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("artistID")
    ), ForeignKey(
        entity = Song::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("songID")
    )]
)
data class ArtistSongJoin(val artistID: Int, val songID: Int) {

}