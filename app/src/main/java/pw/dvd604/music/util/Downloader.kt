package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Song
import java.io.File

class Downloader {

    private val downloadQueue = ArrayList<Song>()

    init {
        val file = File(Settings.getSetting(Settings.storage)!!)
        file.mkdirs()
    }

    fun hasSong(song: Song?): Boolean {
        return false
    }

    fun addToQueue(song: Song) {
        if (downloadQueue.indexOf(song) == -1) {
            downloadQueue.add(song)
        }
    }

    fun doQueue() {
        for (song in downloadQueue) {
            DownloaderAsync(song, ::onUpdate).execute(song)
            downloadQueue.remove(song)
        }
    }

    fun onUpdate(song: Song, progress: Int) {
        Util.log(this, "${song.generateText()} progress: $progress%")
    }
}