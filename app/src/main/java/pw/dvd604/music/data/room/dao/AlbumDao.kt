package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import pw.dvd604.music.data.Album

@Dao
interface AlbumDao {
    @Query("SELECT * FROM album")
    fun getAll(): List<Album>

    @Query("SELECT * FROM album WHERE id IN (:ids)")
    fun findByIDs(ids: IntArray): List<Album>

    @Query("SELECT * FROM album WHERE title LIKE :title LIMIT 1")
    fun findByTitle(title: String): Album

    @Insert
    fun insertAll(vararg albums: Album)

    @Delete
    fun delete(album: Album)
}