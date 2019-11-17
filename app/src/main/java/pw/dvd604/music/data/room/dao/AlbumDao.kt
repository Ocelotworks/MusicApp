package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import pw.dvd604.music.data.Album

@Dao
abstract class AlbumDao : BaseDao<Album> {
    @Query("SELECT * FROM album")
    abstract override fun getAll(): List<Album>

    @Query("SELECT * FROM album WHERE id IN (:ids)")
    abstract fun findByIDs(ids: IntArray): List<Album>

    @Query("SELECT * FROM album WHERE title LIKE :title LIMIT 1")
    abstract fun findByTitle(title: String): Album

    @Query("SELECT COUNT(*) FROM album")
    abstract override fun count(): Int
}