package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Playlist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.ArtistSongJoin

@Dao
interface ArtistSongJoinDao {
    @Insert
    fun insert(artistSongJoin: ArtistSongJoin)

    @Query(
        """
           SELECT * FROM artist
           INNER JOIN artist_song_join
           ON artist.id=artist_song_join.artistID
           WHERE artist_song_join.songId=:songId
           """
    )
    fun getPlaylistsForSong(songId: Int): Array<Playlist>

    @Query(
        """
           SELECT * FROM song
           INNER JOIN artist_song_join
           ON song.id=artist_song_join.songId
           WHERE artist_song_join.artistID=:playlistId
           """
    )
    fun getSongsForPlaylist(playlistId: Int): Array<Song>
}
