package pw.dvd604.music.util.download

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import java.io.File

class Downloader {

    private val downloadQueue = ArrayList<Song>()
    private val downloadStates = HashMap<Song, Boolean>(0)
    private val channelId: String = "petify_download_channel"
    private val notificationId: Int = 696901
    private var progress: Int = 0
    private var currentlyDownloading: Boolean = false
    lateinit var context: Context

    init {
        val file = File(Settings.getSetting(Settings.storage)!!)
        file.mkdirs()

        createNotificationChannel()
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
        if (!currentlyDownloading) {
            currentlyDownloading = true
            buildNotification()
            for (song in downloadQueue) {
                DownloaderAsync(song, ::onUpdate, ::onComplete).execute()

                if (Settings.getBoolean(Settings.offlineAlbum)) {
                    DownloaderAsync(song, null, null, SongDataType.ALBUM).execute()
                }
                downloadQueue.remove(song)
            }
        }
    }

    private fun onComplete(song: Song) {
        downloadStates[song] = false
        progress++

        if (downloadQueue.size == 0) {
            currentlyDownloading = false
        }
    }

    private fun onUpdate(song: Song, progress: Int) {
        Util.log(this, "${song.generateText()} progress: $progress%")
    }

    private fun buildNotification() {
        var builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Downloading songs!")
            .setContentText("Currently downloading")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildList())
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(downloadQueue.size, progress, false)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }

    private fun buildList(): String {
        var list: String = ""

        for (s in downloadQueue) {
            list += s.generateText() + "\n"
        }

        return list
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name_progress)
            val descriptionText = context.getString(R.string.channel_description_progress)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}