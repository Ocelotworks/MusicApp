package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Song
import java.io.File

class Downloader {

    private val downloadQueue = ArrayList<Song>()
    private val downloadStates = HashMap<Song, Boolean>(0)

    init {
        val file = File(Settings.getSetting(Settings.storage)!!)
        file.mkdirs()
    }

    fun hasSong(song: Song?): Boolean {
        return File(Util.songToPath(song!!)).exists()
    }

    fun isDownloading(song: Song?): Boolean {
        return if (downloadStates[song] != null) {
            downloadStates[song]!!
        } else {
            false
        }
    }

    fun addToQueue(song: Song) {
        if (downloadQueue.indexOf(song) == -1) {
            downloadStates[song] = true
            downloadQueue.add(song)
        }
    }

    fun doQueue() {
        for (song in downloadQueue) {
            DownloaderAsync(song, ::onUpdate, ::onComplete).execute()
            downloadQueue.remove(song)
        }
    }

    private fun onComplete(song: Song) {
        downloadStates[song] = false
    }

    private fun onUpdate(song: Song, progress: Int) {
        Util.log(this, "${song.generateText()} progress: $progress%")
    }
}