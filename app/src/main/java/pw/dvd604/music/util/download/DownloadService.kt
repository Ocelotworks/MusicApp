package pw.dvd604.music.util.download

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util

class DownloadService : Service() {

    private var downloadingCount: Int = 0
    private var progress: Int = 0
    private val channelId: String = "petify_download_channel"
    private val notificationId: Int = 696901
    private var queue: ArrayList<Song> = ArrayList(0)
    private var duplicateQueue: ArrayList<Song> = ArrayList(0)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        startForeground(notificationId, buildNotification())

        queue = Util.downloader.downloadQueue
        duplicateQueue = duplicateArrayList(queue)

        Thread {
            for (song in queue) {
                while (downloadingCount > 3 || pauseDownload()) {/*Wait*/
                }
                Util.log(this, "Starting")
                DownloaderAsync(song, ::onUpdate, ::onComplete).execute()
                downloadingCount++

                if (Settings.getBoolean(Settings.offlineAlbum)) {
                    DownloaderAsync(song, null, null, SongDataType.ALBUM).execute()
                }
            }
        }.start()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun onComplete(song: Song) {
        progress++
        downloadingCount--
        duplicateQueue.removeIf {
            it.id == song.id
        }

        buildNotification(true)

        if (duplicateQueue.size == 0) {
            Util.downloader.serviceFinished()
        }
    }

    private fun onUpdate(song: Song, progress: Int) {
        Util.log(this, "${song.generateText()} progress: $progress%")
    }

    private fun buildNotification(notify: Boolean = false): Notification? {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Downloading songs!")
            .setContentText("Currently downloading")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(buildList())
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(queue.size, progress, false)

        if (notify) {
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                if (duplicateQueue.size == 0) {
                    cancel(notificationId)
                    this@DownloadService.stopSelf()
                } else {
                    notify(notificationId, builder.build())
                }
            }
        }
        return builder.build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = this.getString(R.string.channel_name_progress)
            val descriptionText = this.getString(R.string.channel_description_progress)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildList(): String {
        var list = ""

        for (s in duplicateQueue) {
            list += s.generateText() + "\n"
        }

        return list
    }


    @Suppress("DEPRECATION")
    private fun pauseDownload(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        val isConnected: Boolean = activeNetwork?.isConnectedOrConnecting == true

        val isWiFi: Boolean = activeNetwork?.type == ConnectivityManager.TYPE_WIFI


        return !(isWiFi && isConnected) //TODO: Fix this to not be depreciated. Nice android docs though
        // [https://developer.android.com/training/monitoring-device-state/connectivity-monitoring]
    }

    private fun duplicateArrayList(downloadQueue: ArrayList<Song>): ArrayList<Song> {
        val list = ArrayList<Song>(0)
        for (s in downloadQueue) {
            list.add(s)
        }
        return list
    }
}