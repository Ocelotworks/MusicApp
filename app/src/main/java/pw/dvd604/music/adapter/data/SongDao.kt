package pw.dvd604.music.adapter.data

import androidx.room.*

@Dao
interface SongDao{
    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE id IN (:songIds)")
    fun loadAllByIds(songIds: IntArray): List<Song>

    @Update
    fun updateSong(song : Song)

    @Insert
    fun insertAll(vararg song: Song?)

    @Delete
    fun delete(song: Song)

}