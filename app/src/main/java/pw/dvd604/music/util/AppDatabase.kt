package pw.dvd604.music.util

import androidx.room.Database
import androidx.room.RoomDatabase
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDao

@Database(entities = [Song::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}
