package pw.dvd604.music.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import pw.dvd604.music.data.Playlist

@Dao
abstract class PlaylistDao : BaseDao<Playlist> {
    @Query("SELECT * FROM playlist")
    abstract override fun getAll(): List<Playlist>

    @Query("SELECT * FROM playlist WHERE id IN (:ids)")
    abstract fun findByIDs(ids: IntArray): List<Playlist>

    @Query("SELECT * FROM playlist WHERE title LIKE :title LIMIT 1")
    abstract fun findByTitle(title: String): Playlist

    @Query("SELECT COUNT(*) FROM playlist")
    abstract override fun count(): Int
}