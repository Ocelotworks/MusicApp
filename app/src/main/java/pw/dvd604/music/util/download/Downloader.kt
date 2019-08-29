package pw.dvd604.music.util.download

import android.content.Context
import android.content.Intent
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import java.io.File


class Downloader(val context: Context) {

    val downloadQueue = ArrayList<Song>()
    private var currentlyDownloading: Boolean = false

    init {
        val file = File("${Settings.getSetting(Settings.storage)}/album/")
        file.mkdirs()
    }

    fun hasSong(song: Song?): Boolean {
        return File(Util.songToPath(song!!)).exists()
    }

    fun addToQueue(song: Song) {
        if (downloadQueue.indexOf(song) == -1) {
            downloadQueue.add(song)
        }
    }

    fun doQueue() {
        if (!currentlyDownloading) {
            currentlyDownloading = true
            Intent(this.context, DownloadService::class.java).also { intent ->
                this.context.startService(intent)
            }

        }
    }

    fun serviceFinished() {
        currentlyDownloading = false
    }
}