package pw.dvd604.music.service.downloader

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.service.NotificationBuilder
import java.io.File

class DownloadService : Service() {

    private var downloadingCount: Int = 0
    private var progress: Int = 0
    private lateinit var channelId: String
    private val notificationId: Int = 696901
    var skipped = 0
    private var failed = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        channelId = getString(R.string.petify_download_channel)

        NotificationBuilder(this, null).createChannel(
            channelId,
            getString(R.string.channel_name_progress),
            getString(R.string.channel_description_progress)
        )

        startForeground(notificationId, buildNotification())

        val array = GlobalScope.async { buildArray() }

        runBlocking {
            DownloaderAsync(
                ::onUpdate,
                ::onComplete
            ).execute(array.await())
        }

        buildNotification(true)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun buildArray(): ArrayList<String> {
        val array = ArrayList<String>(0)

        val cursor =
            (this@DownloadService.application as MusicApplication).readableDatabase.rawQuery(
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

                val file = File(
                    "${Environment.getExternalStorageDirectory()}/petify/${id}"
                )

                if (!file.exists()) {
                    array.add("https://unacceptableuse.com/petify/song/${id}")
                } else {
                    skipped++
                }
            }
        }
        cursor.close()

        return array
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun onComplete() {
        with(NotificationManagerCompat.from(this)) {
            cancel(notificationId)
        }
        stopSelf()
    }

    private fun onUpdate(vararg value: Int?) {
        progress = value[1]!!
        failed = value[0]!!
        buildNotification(true)
        if (progress == downloadingCount) {
            with(NotificationManagerCompat.from(this)) {
                cancel(notificationId)
            }
        }
    }

    private fun buildNotification(notify: Boolean = false): Notification? {
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentTitle("Downloading songs!")
            .setContentText("Currently downloading $progress / ${downloadingCount - skipped} songs\nSkipped songs: $skipped\nFailed songs: $failed")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setProgress(downloadingCount - skipped, progress, false)

        if (notify) {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }

        return builder.build()
    }
}