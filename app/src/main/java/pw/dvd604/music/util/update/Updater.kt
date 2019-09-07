package pw.dvd604.music.util.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.R
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util


class Updater(val context: Context) {

    private val channelId: String = context.getString(R.string.petify_update_channel)
    private val notificationId: Int = 696902

    init {
        Util.createNotificationChannel(
            context,
            channelId,
            context.getString(R.string.channel_name_update),
            context.getString(R.string.channel_description_update)
        )
    }

    fun checkUpdate() {
        //TODO
    }

    private fun buildNotification(version: Int) {
        val url =
            "${BuildConfig.versionURLRoot}/${Settings.getSetting(Settings.buildName)}/$version.apk"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setPackage("com.android.chrome")

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_update_title))
            .setContentText(context.getString(R.string.notification_update_text))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }
}