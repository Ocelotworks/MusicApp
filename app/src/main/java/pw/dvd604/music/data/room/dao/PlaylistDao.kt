package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Playlist

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlist")
    fun getAll(): List<Playlist>

    @Query("SELECT * FROM playlist WHERE id IN (:ids)")
    fun findByIDs(ids: IntArray): List<Playlist>

    @Query("SELECT * FROM playlist WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Playlist

    @Insert
    fun insertAll(vararg playlist: Playlist)

    @Delete
    fun delete(playlist: Playlist)
}