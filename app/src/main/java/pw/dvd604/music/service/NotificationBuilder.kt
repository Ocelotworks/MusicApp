package pw.dvd604.music.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.MediaMetadata
import android.os.Build
import android.os.Looper
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.RatingCompat
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
        meta = null

        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            Log.e(
                "CRITICAL NEIL ERROR",
                "buildMetaFromID called on UI thread. THIS IS POOR PRACTICE"
            )
        }

        val cursor = (context.applicationContext as MusicApplication).readableDatabase.rawQuery(
            "SELECT ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Song.COLUMN_NAME_ALBUM}, ${DatabaseContract.Song.COLUMN_NAME_DURATION}, ${DatabaseContract.Artist.COLUMN_NAME_NAME}, ${DatabaseContract.Opinion.COLUMN_NAME_OPINION} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} INNER JOIN ${DatabaseContract.Opinion.TABLE_NAME} ON ${DatabaseContract.Opinion.TABLE_NAME}.id =  ${DatabaseContract.Song.TABLE_NAME}.id WHERE ${DatabaseContract.Song.TABLE_NAME}.id = ?",
            arrayOf(id),
            null
        )
        try {
            with(cursor) {
                moveToFirst()
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

                    try {
                        this.putBitmap(
                            MediaMetadata.METADATA_KEY_ALBUM_ART,
                            Glide.with(this@NotificationBuilder.context)
                                .asBitmap()
                                .load(
                                    "https://unacceptableuse.com/petifyv3/api/v2/album/${getString(
                                        getColumnIndexOrThrow(DatabaseContract.Song.COLUMN_NAME_ALBUM)
                                    )}/image"
                                ).placeholder(R.drawable.album)
                                .submit().get()
                            //.get()
                        )
                    } catch (e: Exception) {
                        Log.e("Testing", "", e)
                    }

                    val rating: RatingCompat = try {
                        val ratingInt =
                            getInt(getColumnIndexOrThrow(DatabaseContract.Opinion.COLUMN_NAME_OPINION))

                        if (ratingInt != 0) {
                            RatingCompat.newThumbRating(
                                ratingInt == 1
                            )
                        } else {
                            RatingCompat.newUnratedRating(RatingCompat.RATING_THUMB_UP_DOWN)
                        }

                    } catch (e1: Exception) {
                        Log.e("Error", "Rating error.")
                        RatingCompat.newUnratedRating(RatingCompat.RATING_THUMB_UP_DOWN)
                    }
                    if (!this.build().containsKey(MediaMetadata.METADATA_KEY_RATING))
                        this.putRating(MediaMetadata.METADATA_KEY_RATING, rating)
                }
            }
            cursor.close()
        } catch (e: Exception) {

        }

        if (meta == null) {
            val metaEmpty = MediaMetadataCompat.Builder().apply {
                this.putText(
                    MediaMetadata.METADATA_KEY_TITLE, "Something went wrong"
                )
                this.putText(
                    MediaMetadata.METADATA_KEY_ARTIST,
                    "This can happen for a few reasons, the main one being you're entirely offline."
                )
                this.putText(MediaMetadata.METADATA_KEY_MEDIA_ID, "FUCK3D")
                this.putLong(
                    MediaMetadata.METADATA_KEY_DURATION, 69696969
                )

                this.putRating(
                    MediaMetadata.METADATA_KEY_RATING,
                    RatingCompat.newUnratedRating(RatingCompat.RATING_THUMB_UP_DOWN)
                )
            }
            return metaEmpty.build()
        }

        return meta?.build()
    }

    fun build(id: String): Notification? {
        return build(buildMetaFromID(id))
    }

    fun build(metadataOpt: MediaMetadataCompat? = meta?.build()): Notification? {
        if (metadataOpt == null || mediaSession == null) {
            Log.e(
                "Notification Builder",
                "Meta null: ${metadataOpt == null}, Session null: ${mediaSession == null}"
            )
            return null
        }

        val controller = mediaSession.controller

        return NotificationCompat.Builder(context, channelId).apply {
            // Add the metadata for the currently playing track
            setContentTitle(metadataOpt.description.title)
            setContentText(metadataOpt.description.subtitle)
            setSubText(metadataOpt.description.description)
            setLargeIcon(metadataOpt.description.iconBitmap)

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

            addAction(
                NotificationCompat.Action(
                    R.drawable.baseline_skip_previous_white_18,
                    "Skip forward",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context as MediaPlaybackService,
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    )
                )
            )

            // Add a pause button
            addAction(
                NotificationCompat.Action(
                    if (mediaSession.controller.playbackState.state == PlaybackStateCompat.STATE_PLAYING) {
                        R.drawable.baseline_pause_white_18
                    } else {
                        R.drawable.baseline_play_arrow_white_18
                    },
                    "Pause",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
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
                    .setShowActionsInCompactView(0, 1, 2)

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