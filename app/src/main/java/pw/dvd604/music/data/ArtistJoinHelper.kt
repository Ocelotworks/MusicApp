package pw.dvd604.music.data

import android.util.Log
import pw.dvd604.music.data.room.dao.ArtistDao
import pw.dvd604.music.data.room.dao.ArtistSongJoinDao
import pw.dvd604.music.data.room.dao.SongDao

class ArtistJoinHelper(
    private val songDao: SongDao,
    private val artistSongJoinDao: ArtistSongJoinDao,
    private val artistDao: ArtistDao
) {

    fun get(): ArrayList<ArtistSong> {
        val list = ArrayList<ArtistSong>()

        val songs = songDao.getAll()
        val artists = artistDao.getAll()
        val join = artistSongJoinDao.getAll()

        join.forEach {
            try {
                val item = ArtistSong()

                val artist: Artist? = artists.find { obj -> obj.id == it.artistID }
                val song: Song? = songs.find { obj -> obj.id == it.songID }

                if (artist != null && song != null) {
                    item.artist = artist
                    item.song = song
                }

                list.add(item)
            } catch (e: Exception) {
                Log.e("Test", "", e)
            }
        }

        return list
    }
}