package pw.dvd604.music.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.Playlist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.dao.*

@Database(
    entities = [Album::class, Playlist::class, Song::class, Artist::class, AlbumSongJoin::class, ArtistSongJoin::class, PlaylistSongJoin::class],
    version = 9
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun songDao(): SongDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistSongJoinDao(): PlaylistSongJoinDao
    abstract fun albumSongJoinDao(): AlbumSongJoinDao
    abstract fun artistSongJoinDao(): ArtistSongJoinDao
}