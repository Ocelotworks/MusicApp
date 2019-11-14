package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Playlist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.AlbumSongJoin

@Dao
interface AlbumSongJoinDao {
    @Insert
    fun insert(albumSongJoin: AlbumSongJoin)

    @Query(
        """
           SELECT * FROM album
           INNER JOIN album_song_join
           ON album.id=album_song_join.albumID
           WHERE album_song_join.songId=:songId
           """
    )
    fun getPlaylistsForSong(songId: Int): Array<Playlist>

    @Query(
        """
           SELECT * FROM song
           INNER JOIN album_song_join
           ON song.id=album_song_join.songId
           WHERE album_song_join.albumID=:playlistId
           """
    )
    fun getSongsForPlaylist(playlistId: Int): Array<Song>
}
