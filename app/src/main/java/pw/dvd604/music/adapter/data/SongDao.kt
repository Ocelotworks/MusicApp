package pw.dvd604.music.adapter.data

import androidx.room.*

@Dao
interface SongDao{
    @Query("SELECT * FROM song")
    fun getAll(): List<Song>

    @Query("SELECT * FROM song WHERE song_id IN (:songIds) LIMIT 1")
    fun loadByID(songIds: IntArray): Song

    @Update
    fun updateSong(song : Song)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg song: Song?)

    @Delete
    fun delete(song: Song)

}