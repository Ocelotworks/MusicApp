package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import pw.dvd604.music.data.Artist

@Dao
abstract class ArtistDao : BaseDao<Artist> {
    @Query("SELECT * FROM artist")
    abstract fun getAll(): List<Artist>

    @Query("SELECT * FROM artist WHERE id IN (:ids)")
    abstract fun findByIDs(ids: IntArray): List<Artist>

    @Query("SELECT * FROM artist WHERE title LIKE :title LIMIT 1")
    abstract fun findByTitle(title: String): Artist

    @Query("SELECT COUNT(*) FROM artist")
    abstract override fun count(): Int
}