package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType

class SongList {
    companion object {
        var callback: ((data: ArrayList<Song>?) -> Unit)? = null
        var songList = ArrayList<Song>(0)
        var albumMap = HashMap<String, String>(0)
        var genreMap = HashMap<String, String>(0)
        var artistMap = HashMap<String, String>(0)
        var playlistMap = HashMap<String, String>(0)

        fun generateMaps() {

        }

        fun setSongsAndNotify(songs: ArrayList<Song>) {
            songList = songs
            callback?.let { it(null) }
        }

        fun applyFilter(map: HashMap<String, SongDataType>) {
            for ((k, v) in map) {
                when (v) {
                    SongDataType.SONG -> {
                        songList.removeIf { song ->
                            song.name.contains(k)
                        }
                    }
                    SongDataType.ARTIST -> {
                        val id = artistMap[k]
                        songList.removeIf { song ->
                            song.artistID == id
                        }
                    }
                    SongDataType.GENRE -> {
                        val id = genreMap[k]
                        songList.removeIf { song ->
                            song.genre == id
                        }
                    }
                    SongDataType.ALBUM -> {
                        val id = albumMap[k]
                        songList.removeIf { song ->
                            song.album == id
                        }
                    }
                    SongDataType.PLAYLIST -> {
                        //TODO: Implement this
                        /*val id = playlistMap[k]
                        songList.removeIf {song ->
                            song.artistID == id
                        }*/
                    }
                }
            }
        }
    }
}