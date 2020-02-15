package pw.dvd604.music.service.downloader

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.service.NotificationBuilder

class DownloadService : Service() {

    private var downloadingCount: Int = 0
    private var progress: Int = 0
    private lateinit var channelId: String
    private val notificationId: Int = 696901
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        channelId = getString(R.string.petify_download_channel)

        NotificationBuilder(this, null).createChannel(
            channelId,
            getString(R.string.channel_name_progress),
            getString(R.string.channel_description_progress)
        )

        startForeground(notificationId, buildNotification())

        Thread {
            val cursor = (this.application as MusicApplication).readableDatabase.rawQuery(
                "SELECT id FROM ${DatabaseContract.Song.TABLE_NAME}",
                null,
                null
            )

            Log.e("Downloader", "Done query. Got ${cursor.count} results")
            downloadingCount = cursor.count

            with(cursor) {
                while (moveToNext()) {

                    val id = getString(
                        getColumnIndexOrThrow("id")
                    )

                    DownloaderAsync(
                        ::onUpdate,
                        ::onComplete
                    ).execute("https://unacceptableuse.com/petify/song/${id}")
                }
            }
            cursor.close()
        }.start()

        buildNotification(true)

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun onComplete() {
        progress++
        buildNotification(true)
        if (progress == downloadingCount) {
            with(NotificationManagerCompat.from(this)) {
                cancel(notificationId)
            }
        }
    }

    private fun onUpdate() {

    }

    private fun buildNotification(notify: Boolean = false): Notification? {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Downloading songs!")
            .setContentText("Currently downloading $progress / $downloadingCount songs")
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (notify) {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }

        return builder.build()
    }
}