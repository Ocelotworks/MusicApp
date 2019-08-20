package pw.dvd604.music.adapter.data

import androidx.room.*

@Dao
interface SongDao{
    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE id IN (:songIds)")
    fun loadAllByIds(songIds: IntArray): List<Song>

    @Query("SELECT * FROM song ORDER BY play_count DESC LIMIT :limit ")
    fun loadTopSongs(limit: Int): List<Song>

    @Update
    fun updateSong(song : Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg song: Song?)

    @Delete
    fun delete(song: Song)

}