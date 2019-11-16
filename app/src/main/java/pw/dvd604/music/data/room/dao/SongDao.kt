package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import pw.dvd604.music.data.Song

@Dao
abstract class SongDao : BaseDao<Song> {
    @Query("SELECT * FROM song")
    abstract fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE id IN (:ids)")
    abstract fun findByIDs(ids: IntArray): List<Song>

    @Query("SELECT * FROM song WHERE title LIKE :title LIMIT 1")
    abstract fun findByTitle(title: String): Song

    @Query("SELECT COUNT(*) FROM song")
    abstract override fun count(): Int
}