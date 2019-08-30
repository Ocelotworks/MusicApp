package pw.dvd604.music.util.update

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.pm.PackageInfoCompat
import com.android.volley.Response
import org.json.JSONObject
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.R
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util


class Updater(val context: Context) : Response.Listener<String> {

    private val channelId: String = "petify_update_channel"
    private val notificationId: Int = 696902

    init {
        createNotificationChannel()
    }

    fun checkUpdate() {
        val http = HTTP(context)
        http.getReq(BuildConfig.versionURL, this)
    }

    override fun onResponse(response: String?) {
        val json = JSONObject(response)

        try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val version: Int = PackageInfoCompat.getLongVersionCode(pInfo).toInt()

            if (json.getInt(Settings.getSetting(Settings.buildName)) > version) {
                buildNotification(json.getInt(Settings.getSetting(Settings.buildName)))
            }
        } catch (e: Exception) {
            Util.log(this, "Incorrect build name given")
        }

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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name_update)
            val descriptionText = context.getString(R.string.channel_description_update)
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