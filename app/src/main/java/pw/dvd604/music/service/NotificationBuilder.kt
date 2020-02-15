package pw.dvd604.music.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.os.Build
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.Glide
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.data.storage.DatabaseContract


private const val channelId = "petify_music"

class NotificationBuilder(
    private val context: Context,
    private val mediaSession: MediaSessionCompat?
) {

    var meta: MediaMetadataCompat.Builder? = null

    fun createChannel(
        id: String,
        name: String,
        descriptionText: String,
        importance: Int = NotificationManager.IMPORTANCE_LOW
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(id, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun buildMetaFromID(id: String): MediaMetadataCompat? {

        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            Log.e(
                "CRITICAL NEIL ERROR",
                "buildMetaFromID called on UI thread. THIS IS POOR PRACTICE"
            )
        }

        val cursor = (context.applicationContext as MusicApplication).readableDatabase.rawQuery(
            "SELECT ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Song.COLUMN_NAME_ALBUM}, ${DatabaseContract.Song.COLUMN_NAME_DURATION}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} WHERE ${DatabaseContract.Song.TABLE_NAME}.id = ?",
            arrayOf(id),
            null
        )

        with(cursor) {
            while (moveToNext()) {
                try {
                    meta = MediaMetadataCompat.Builder().apply {
                        this.putText(
                            MediaMetadata.METADATA_KEY_TITLE, getString(
                                getColumnIndexOrThrow(
                                    DatabaseContract.Song.COLUMN_NAME_TITLE
                                )
                            )
                        )
                        this.putText(
                            MediaMetadata.METADATA_KEY_ARTIST, getString(
                                getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME)
                            )
                        )
                        this.putText(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
                        this.putLong(
                            MediaMetadata.METADATA_KEY_DURATION, getLong(
                                getColumnIndexOrThrow(
                                    DatabaseContract.Song.COLUMN_NAME_DURATION
                                )
                            ) * 1000
                        )
                    }

                    meta?.putBitmap(
                        MediaMetadata.METADATA_KEY_ALBUM_ART,
                        Glide.with(this@NotificationBuilder.context)
                            .asBitmap()
                            .load(
                                "https://unacceptableuse.com/petifyv3/api/v2/album/${getString(
                                    getColumnIndexOrThrow(DatabaseContract.Song.COLUMN_NAME_ALBUM)
                                )}/image"
                            )
                            .submit()
                            .get()
                    )
                } catch (e: Exception) {
                    Log.e("Shite", "", e)
                }
            }
        }
        cursor.close()
        return meta?.build()
    }

    fun build(id: String): Notification? {
        val cursor = (context.applicationContext as MusicApplication).readableDatabase.rawQuery(
            "SELECT ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Song.COLUMN_NAME_ALBUM}, ${DatabaseContract.Song.COLUMN_NAME_DURATION}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} WHERE ${DatabaseContract.Song.TABLE_NAME}.id = ?",
            arrayOf(id),
            null
        )

        with(cursor) {
            while (moveToNext()) {
                try {
                    meta = MediaMetadataCompat.Builder().apply {
                        this.putText(
                            MediaMetadata.METADATA_KEY_TITLE, getString(
                                getColumnIndexOrThrow(
                                    DatabaseContract.Song.COLUMN_NAME_TITLE
                                )
                            )
                        )
                        this.putText(
                            MediaMetadata.METADATA_KEY_ARTIST, getString(
                                getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME)
                            )
                        )
                        this.putText(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
                        this.putLong(
                            MediaMetadata.METADATA_KEY_DURATION, getLong(
                                getColumnIndexOrThrow(
                                    DatabaseContract.Song.COLUMN_NAME_DURATION
                                )
                            )
                        )
                    }

                    Log.e(
                        "Test", "https://unacceptableuse.com/petifyv3/api/v2/album/${getString(
                            getColumnIndexOrThrow(DatabaseContract.Song.COLUMN_NAME_ALBUM)
                        )}/image"
                    )

                    meta?.putBitmap(
                        MediaMetadata.METADATA_KEY_ALBUM_ART,
                        Glide.with(this@NotificationBuilder.context)
                            .asBitmap()
                            .load(
                                "https://unacceptableuse.com/petifyv3/api/v2/album/${getString(
                                    getColumnIndexOrThrow(DatabaseContract.Song.COLUMN_NAME_ALBUM)
                                )}/image"
                            )
                            .submit()
                            .get()
                    )
                } catch (e: Exception) {
                    Log.e("Shite", "", e)
                }
            }
        }
        cursor.close()



        return build()
    }

    fun build(): Notification? {
        if (meta == null || mediaSession == null) {
            Log.e("Notification Builder", "Meta: ${meta == null}, Session: ${mediaSession == null}")
            return null
        }

        val controller = mediaSession.controller

        val metadata = meta!!.build()

        return NotificationCompat.Builder(context, channelId).apply {
            // Add the metadata for the currently playing track
            setContentTitle(metadata.description.title)
            setContentText(metadata.description.subtitle)
            setSubText(metadata.description.description)
            setLargeIcon(metadata.description.iconBitmap)

            // Enable launching the player by clicking the notification
            setContentIntent(controller.sessionActivity)

            // Stop the service when the notification is swiped away
            setDeleteIntent(
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    context,
                    PlaybackStateCompat.ACTION_STOP
                )
            )

            // Make the transport controls visible on the lockscreen
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Add an app icon and set its accent color
            // Be careful about the color
            setSmallIcon(R.drawable.ic_notification)
            color = ContextCompat.getColor(context, R.color.colorPrimaryDark)

            // Add a pause button
            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_pause_white_18,
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_previous_white_18,
                    "Skip forward",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_next_white_18,
                    "Skip backward",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    )
                )
            )

            // Take advantage of MediaStyle features
            setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(1, 0, 2)

                    // Add a cancel button
                    .setShowCancelButton(true)
                    .setCancelButtonIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_STOP
                        )
                    )
            )
        }.build()
    }
}