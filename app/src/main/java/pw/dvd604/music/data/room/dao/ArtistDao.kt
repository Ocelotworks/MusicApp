package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Artist

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artist")
    fun getAll(): List<Artist>

    @Query("SELECT * FROM artist WHERE id IN (:ids)")
    fun findByIDs(ids: IntArray): List<Artist>

    @Query("SELECT * FROM artist WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Artist

    @Insert
    fun insertAll(vararg artists: Artist)

    @Delete
    fun delete(artist: Artist)
}