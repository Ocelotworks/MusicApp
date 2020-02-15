package pw.dvd604.music.service.downloader

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Environment
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.data.storage.DatabaseContract.Song.COLUMN_NAME_HASH
import pw.dvd604.music.service.NotificationBuilder
import pw.dvd604.music.util.MD5
import java.io.File

class HashService : Service() {

    private val notificationId: Int = 696905

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        NotificationBuilder(this, null).createChannel(
            this.getString(R.string.petify_hash_channel),
            this.getString(R.string.petify_hash_name),
            this.getString(R.string.petify_hash_description)
        )

        startForeground(notificationId, buildNotification())

        var broken = 0

        val hashmap = HashMap<String, String>(0)
        val path = File(
            "${Environment.getExternalStorageDirectory()}/petify/"
        )

        val cursor = (this.application as MusicApplication).readableDatabase.rawQuery(
            "SELECT id, $COLUMN_NAME_HASH FROM ${DatabaseContract.Song.TABLE_NAME}",
            null,
            null
        )

        with(cursor) {
            while (moveToNext()) {
                hashmap[getString(getColumnIndexOrThrow("id"))] = getString(
                    getColumnIndexOrThrow(
                        COLUMN_NAME_HASH
                    )
                )
            }
        }
        cursor.close()

        Thread {
            val count = path.listFiles().size
            var currentCount = 0
            path.listFiles().forEach { i ->
                currentCount++

                buildNotification(currentCount, count, broken)

                val hash = hashmap[i.name]

                if (hash != null) {
                    if (!MD5.checkMD5(hash, i)) {
                        broken++
                        i.delete()
                    }
                }
            }
        }.start()
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
        progress: Int,
        size: Int,
        broken: Int
    ): Notification? {
        val builder = NotificationCompat.Builder(this, this.getString(R.string.petify_hash_channel))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Currently hashing")
            .setStyle(NotificationCompat.BigTextStyle())
            .setContentText("$progress / $size songs\n$broken broken songs removed")
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