package pw.dvd604.music.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import pw.dvd604.music.data.Playlist
import pw.dvd604.music.data.Song

@Entity(
    tableName = "playlist_song_join",
    primaryKeys = ["playlistID", "songID"],
    foreignKeys = [ForeignKey(
        entity = Playlist::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("playlistID")
    ), ForeignKey(
        entity = Song::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("songID")
    )]
)
data class PlaylistSongJoin(val playlistID: Int, val songID: Int) {

}