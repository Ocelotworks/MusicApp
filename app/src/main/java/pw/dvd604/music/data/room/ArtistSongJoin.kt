package pw.dvd604.music.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.Song

@Entity(
    tableName = "artist_song_join",
    primaryKeys = ["artistID", "songID"],
    foreignKeys = [ForeignKey(
        entity = Artist::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("artistID")
    ), ForeignKey(
        entity = Song::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("songID")
    )]
)
data class ArtistSongJoin(val artistID: String, val songID: String) {
}