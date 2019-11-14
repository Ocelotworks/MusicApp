package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Playlist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.PlaylistSongJoin

@Dao
interface PlaylistSongJoinDao {
    @Insert
    fun insert(playlistSongJoin: PlaylistSongJoin)

    @Query(
        """
           SELECT * FROM playlist
           INNER JOIN playlist_song_join
           ON playlist.id=playlist_song_join.playlistId
           WHERE playlist_song_join.songId=:songId
           """
    )
    fun getPlaylistsForSong(songId: Int): Array<Playlist>

    @Query(
        """
           SELECT * FROM song
           INNER JOIN playlist_song_join
           ON song.id=playlist_song_join.songId
           WHERE playlist_song_join.playlistId=:playlistId
           """
    )
    fun getSongsForPlaylist(playlistId: Int): Array<Song>
}
