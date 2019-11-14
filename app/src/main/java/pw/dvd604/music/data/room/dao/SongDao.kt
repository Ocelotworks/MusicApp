package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Song

@Dao
interface SongDao {
    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE id IN (:ids)")
    fun findByIDs(ids: IntArray): List<Song>

    @Query("SELECT * FROM song WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Song

    @Insert
    fun insertAll(vararg songs: Song)

    @Delete
    fun delete(song: Song)
}