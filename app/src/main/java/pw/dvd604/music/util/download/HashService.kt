package pw.dvd604.music.util.download

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.util.MD5
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.Util
import java.io.File

class HashService : Service() {

    private val notificationId: Int = 696905

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        Util.createNotificationChannel(
            this,
            this.getString(R.string.petify_hash_channel),
            this.getString(R.string.petify_hash_name),
            this.getString(R.string.petify_hash_description)
        )

        startForeground(notificationId, buildNotification())

        var broken = 0
        SongList.discoverDownloadedSongs()
        Thread {
            SongList.downloadedSongs.forEachIndexed { i, f ->
                val file = File(f.toPath())

                buildNotification(f, i, SongList.downloadedSongs.size)

                if (f.hash != "") {
                    if (!MD5.checkMD5(f.hash, file)) {
                        broken++
                        file.delete()
                        Util.downloader.addToQueue(f)
                    }
                }
            }

            reportDone(broken)
        }.start()
    }

    private fun reportDone(i: Int) {
        if (i > 0) {
            Util.downloader.doQueue()
        }
    }

    private fun buildNotification(): Notification {
        val builder = NotificationCompat.Builder(this, this.getString(R.string.petify_hash_channel))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Currently hashing")
            .setPriority(NotificationCompat.PRIORITY_LOW)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
        return builder.build()
    }

    private fun buildNotification(
        song: Media,
        progress: Int,
        size: Int
    ): Notification? {
        val builder = NotificationCompat.Builder(this, this.getString(R.string.petify_hash_channel))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Currently hashing")
            .setContentText(song.generateText())
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(size, progress, false)

        with(NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define
            if (progress == size - 1) {
                cancel(notificationId)
                this@HashService.stopSelf()
            } else {
                notify(notificationId, builder.build())
            }
        }

        return builder.build()
    }
}