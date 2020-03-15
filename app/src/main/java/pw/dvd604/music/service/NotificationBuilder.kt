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

        with(cursor) {
            while (moveToNext()) {
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

                try {
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
                try {
                    meta?.putRating(MediaMetadata.METADATA_KEY_RATING, rating)
                } catch (e: ConcurrentModificationException) {
                    //Genuinely don't know why this is an issue
                }
            }
        }
        cursor.close()

        if (meta == null) {
            meta = MediaMetadataCompat.Builder().apply {
                this.putText(
                    MediaMetadata.METADATA_KEY_TITLE, "DoOt Me UP InsIdE"
                )
                this.putText(
                    MediaMetadata.METADATA_KEY_ARTIST,
                    "What the fuck did you just fucking say about me, you little bitch? I'll have you know I graduated top of my class in the Navy Seals, and I've been involved in numerous secret raids on Al-Quaeda, and I have over 300 confirmed kills. I am trained in gorilla warfare and I'm the top sniper in the entire US armed forces. You are nothing to me but just another target. I will wipe you the fuck out with precision the likes of which has never been seen before on this Earth, mark my fucking words. You think you can get away with saying that shit to me over the Internet? Think again, fucker. As we speak I am contacting my secret network of spies across the USA and your IP is being traced right now so you better prepare for the storm, maggot. The storm that wipes out the pathetic little thing you call your life. You're fucking dead, kid. I can be anywhere, anytime, and I can kill you in over seven hundred ways, and that's just with my bare hands. Not only am I extensively trained in unarmed combat, but I have access to the entire arsenal of the United States Marine Corps and I will use it to its full extent to wipe your miserable ass off the face of the continent, you little shit. If only you could have known what unholy retribution your little \"clever\" comment was about to bring down upon you, maybe you would have held your fucking tongue. But you couldn't, you didn't, and now you're paying the price, you goddamn idiot. I will shit fury all over you and you will drown in it. You're fucking dead, kiddo."
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
        }

        return meta?.build()
    }

    fun build(id: String): Notification? {
        /*
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
        */

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