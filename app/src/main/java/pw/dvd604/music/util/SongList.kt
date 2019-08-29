package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType

class SongList {
    companion object {
        var callback: ((data: ArrayList<Song>?) -> Unit)? = null
        var songList = ArrayList<Song>(0)
        var backupSongList = ArrayList<Song>(0)
        var albumMap = HashMap<String, String>(0)
        var genreMap = HashMap<String, String>(0)
        var artistMap = HashMap<String, String>(0)
        var playlistMap = HashMap<String, String>(0)
        var filterMap = HashMap<String, SongDataType>(0)

        fun generateMaps() {

        }

        fun setSongsAndNotify(songs: ArrayList<Song>) {
            songList = songs
            callback?.let { it(null) }
        }

        fun applyFilter() {
            if (backupSongList.size == 0) {
                //Copy the 'full' song list, pre-filter to a backing place
                //This allows us to 'regenerate' the full song list without having to rerequest it from the server
                //Accounts for filter entries being removed
                backupSongList = Util.duplicateArrayList(songList)
            } else {
                songList = Util.duplicateArrayList(backupSongList)
            }
            for ((k, v) in filterMap) {
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