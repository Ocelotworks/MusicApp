package pw.dvd604.music.util

import pw.dvd604.music.adapter.data.Song

class Downloader {

    private val downloadQueue = ArrayList<Song>()

    fun hasSong(song: Song?): Boolean {
        return false
    }

    fun addToQueue(song: Song) {
        if (downloadQueue.indexOf(song) == -1) {
            downloadQueue.add(song)
        }
    }

    fun doQueue() {

    }
}