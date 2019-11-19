package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.ArtistSong
import pw.dvd604.music.data.room.ArtistSongJoin

@Dao
interface ArtistSongJoinDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(artistSongJoin: ArtistSongJoin)

    @Query(
        "SELECT * FROM artist INNER JOIN artist_song_join ON artist.id=artist_song_join.artistID WHERE artist_song_join.songId=:songId"
    )
    fun getArtistForSong(songId: String): Array<Artist>

    @Query("SELECT * FROM song INNER JOIN artist_song_join ON song.id=artist_song_join.songId WHERE artist_song_join.artistID=:artistId")
    fun getSongsForArtist(artistId: String): Array<Song>

    @Query("SELECT song.id as songID, artist.title as artistTitle, song.title as songTitle FROM artist_song_join INNER JOIN artist ON artist.id=artist_song_join.artistID INNER JOIN song ON song.id=artist_song_join.songID")
    fun getSongsWithArtists(): List<ArtistSong>

    @Query("SELECT COUNT(*) FROM artist_song_join")
    fun count(): Int
}
