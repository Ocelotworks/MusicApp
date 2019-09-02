package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SongList {
    companion object {
        var callback: ((data: ArrayList<Media>?) -> Unit)? = null
        var songList = ArrayList<Media>(0)
        var downloadedSongs = ArrayList<Media>(0)
        var backupSongList = ArrayList<Media>(0)
        var translationMap = HashMap<String, String>(0)
        var filterMap = HashMap<String, MediaType>(0)

        fun generateMaps(
            arrayList: ArrayList<Media>
        ) {
            for (media in arrayList) {
                translationMap[media.name.toLowerCase(Locale.getDefault())] = media.id
            }
        }

        fun setSongsAndNotify(media: ArrayList<Media>) {
            songList = media
            discoverDownloadedSongs()
            callback?.let { it(null) }
        }

        fun applyFilter() {
            if (backupSongList.size == 0) {
                //Copy the 'full' media list, pre-filter to a backing place
                //This allows us to 'regenerate' the full media list without having to rerequest it from the server
                //Accounts for filter entries being removed
                backupSongList = Util.duplicateArrayList(songList)
            } else {
                songList = Util.duplicateArrayList(backupSongList)
            }
            val oldSize = songList.size
            for ((k, v) in filterMap) {
                val key = k.toLowerCase(Locale.getDefault())
                when (v) {
                    MediaType.SONG -> {
                        songList.removeIf { song ->
                            song.name.toLowerCase(Locale.getDefault()).contains(key)
                        }
                    }
                    MediaType.ARTIST -> {
                        val id = translationMap[key]
                        songList.removeIf { song ->
                            song.artistID == id
                        }

                    }
                    MediaType.GENRE -> {
                        val id = translationMap[key]
                        songList.removeIf { song ->
                            song.genre == id
                        }
                    }
                    MediaType.ALBUM -> {
                        val id = translationMap[key]
                        songList.removeIf { song ->
                            song.album == id
                        }
                    }
                    MediaType.PLAYLIST -> {
                        // val id = translationMap[k]
                        /*
                        mediaList.removeIf {media ->
                            media.artistID == id
                        }*/
                    }
                }
            }
            val newSize = songList.size
            Util.log(this, "Deleted ${oldSize - newSize} songs for filters")
            callback?.let { it(null) }
        }

        fun discoverDownloadedSongs() {
            val path = Settings.getSetting(Settings.storage)
            path?.let {
                val directory = File(it)

                for (f in directory.listFiles()) {
                    val songID = f.name
                    val songObj = songList.filter { song ->
                        song.id == songID
                    }

                    if (songList.size > 0) {
                        downloadedSongs.addAll(songObj)
                    }
                }
            }
        }
    }
}