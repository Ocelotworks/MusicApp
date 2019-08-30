package pw.dvd604.music.util

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import org.json.JSONObject
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.download.Downloader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


class Util {

    companion object {
        /**Used to generate media metadata from the MediaService**/
        private var tempMetadataCompat: MediaMetadataCompat.Builder? = null
        /**Used to track the previous songs played**/
        private val previousSongs = ArrayList<Media>(0)
        /**Used to track the mediaQueue**/
        var mediaQueue: ArrayList<Media>? = null
        /**Instance of the Downloader**/
        @SuppressLint("StaticFieldLeak") //TODO: Fix this better later
        lateinit var downloader: Downloader

        /**Creates a human readable string representing media duration
         * @param seconds The length of media in seconds in a non-nullable Int
         * @return String**/
        fun prettyTime(seconds: Int): String {
            val mins: Int = (seconds % 3600 / 60)
            val secs: Int = seconds % 60

            return "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
        }

        /**Creates a human readable string representing media duration. If the seconds value is null, returns "00:00"
         * @param seconds The length of media in seconds in a nullable Long
         * @return String**/
        fun prettyTime(seconds: Long?): String {
            val secondsInt: Int? = seconds?.toInt()
            return if (secondsInt != null) {
                val mins: Int = (secondsInt % 3600 / 60)
                val secs: Int = secondsInt % 60

                "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
            } else {
                "00:00"
            }
        }

        /**Creates a Media object from a JSON string
         * @param [json] The json object
         * @return Media**/
        fun jsonToSong(json: JSONObject): Media {
            return try {
                Media(
                    json.getString("title"),
                    json.getString("name"),
                    json.getString("song_id"),
                    json.getString("album"),
                    "",
                    json.getString("artist_id")
                )
            } catch (e: Exception) {
                Media(
                    json.getString("title"),
                    json.getString("artist"),
                    json.getString("id"),
                    json.getString("album"),
                    "",
                    json.getString("artistID")
                )
            }
        }

        /**Creates a JSON object from a Media
         * @param [media] The media to create the json from
         * @return JSONObject**/
        fun songToJson(media: Media): JSONObject? {
            return JSONObject()
                .put("title", media.name)
                .put("name", media.author)
                .put("song_id", media.id)
                .put("album", media.album)
                .put("genre", media.genre)
                .put("artist_id", media.artistID)
        }

        /**Returns the location of the media file for a given media
         * Supports offline play, where the local directory is returned, and online play,
         * where the online URL is returned. This online play respects the user defined server URL
         * @param media The media to get the location for
         * @return String, The URL of the media, local or server-based**/
        fun songToUrl(media: Media?): String {
            if (Settings.getBoolean(Settings.offlineMusic)) {
                //Do offline stored check
                if (downloader.hasSong(media)) {
                    return songToPath(media!!)
                }
            }
            return "${Settings.getSetting(
                Settings.server
            )}/media/${media?.id}"
        }

        /**Takes a given Media, and creates a MediaBroswerCompat MediaItem.
         * This is used for populating the MediaService, and allowing external MediaControllers to parse what music we have
         * @param media The media to create the media item from
         * @return MediaItem, The Android MediaItem based off the Media**/
        fun songToMediaItem(media: Media): MediaBrowserCompat.MediaItem {
            val descriptionBuilder = MediaDescriptionCompat.Builder().apply {
                setTitle(media.name)
                setDescription(media.author)
                setMediaUri(Uri.parse(songToUrl(media)))
                setIconUri(Uri.parse(songToAlbumURL(media)))
                setMediaId(media.id)
            }
            return MediaBrowserCompat.MediaItem(
                descriptionBuilder.build(),
                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
            )
        }

        /**Takes an Android internal button ID, and returns a human readable button name. Used in tracking
         * @param id The Android View ID
         * @return String, the Button name**/
        fun idToString(id: Int): String {
            return when (id) {
                R.id.btnTitle -> {
                    "Title filter"
                }
                R.id.btnAlbum -> {
                    "Album filter"
                }
                R.id.btnGenre -> {
                    "Genre filter"
                }
                R.id.btnArtist -> {
                    "Artist filter"
                }

                R.id.btnPause -> {
                    "Pause"
                }
                R.id.btnNext -> {
                    "Skip"
                }
                R.id.btnPrev -> {
                    "Prev"
                }
                R.id.btnStar -> {
                    "Star"
                }
                R.id.btnShuffle -> {
                    "Shuffle"
                }
                else -> {
                    "Button"
                }
            }
        }

        /**Takes a Media, and returns the Media metadata, including title, artist, genre, and art location.
         * Has some trickery to do with media duration too, by storing the unbuilt metadata, before returning the metadata.
         * This allows [addMetadata] and [addMetadataProgress] to return the same basic metadata as this method, while adding additional information.
         * Probably not the best way to do it, but things are constantly getting changed
         * @param media The media to build the metadata from
         * @param builder Whether or not this should potentially destroy the metadata by building it
         * @return MetaData, if [builder] is true, and null if [builder] is false**/
        fun songToMetadata(media: Media, builder: Boolean = false): MediaMetadataCompat? {
            val metaData = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, media.name)
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, media.author)
                .putText(MediaMetadataCompat.METADATA_KEY_GENRE, media.genre)
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, songToAlbumURL(media))
            tempMetadataCompat = metaData
            if (!builder)
                return metaData.build()
            return null
        }

        /**@see songToMetadata
         * @param duration The media Duration
         * @return Completed media metadata**/
        fun addMetadata(duration: Int?): MediaMetadataCompat {
            duration?.let { durationNN ->
                tempMetadataCompat?.putLong(
                    MediaMetadataCompat.METADATA_KEY_DURATION,
                    durationNN.toLong()
                )
                tempMetadataCompat?.let {
                    return it.build()
                }
            }
            return MediaMetadataCompat.Builder().build()
        }

        /**@see songToMetadata
         * @param duration The media progess
         * @return Completed media metadata**/
        fun addMetadataProgress(duration: Int?): MediaMetadataCompat {
            duration?.let { durationNN ->
                tempMetadataCompat?.putLong("progress", durationNN.toLong())
                tempMetadataCompat?.let {
                    return it.build()
                }
            }
            return MediaMetadataCompat.Builder().build()
        }

        /**Creates a URL pointing to the media album art.
         * While this doesn't support offline storage at the minute, it will in future
         * @param media The requested media
         * @return String, the album art URL**/
        fun songToAlbumURL(media: Media): String? {
            return "${Settings.getSetting(Settings.server)}/album/${media.album}"
        }

        /**Adds a media to the last played list
         * @param media The last played media**/
        fun addSongToStack(media: Media?) {
            media?.let { previousSongs.add(it) }
        }

        /**Pops the last played media from the stack, deletes it, and returns it here
         * @return Media, the last played media**/
        fun popSongStack(): Media {
            previousSongs.removeAt(previousSongs.size - 1)
            return previousSongs[previousSongs.size - 1]
        }

        /**Only using when tracking is enabled.
         * Creates a random UUID, as per Google recommendation, and returns it, while writing it to the shared prefs.
         * If there is a UUID in the shard prefs, it returns that instead. This way, a device will only have one generated tracking ID
         * This allows the UUID to be changed, but this is unlikely to happen, as anyone who is likely to change the ID
         * is more than likely to disable tracking
         * @return String, the UUID, or "" if [Settings] is not created beforehand**/
        fun getTrackingID(): String {
            val uuid = Settings.getSetting(Settings.tracking, UUID.randomUUID().toString())
            Settings.putString(Settings.tracking, uuid)

            return uuid ?: ""
        }

        /**Takes an internal Android view ID, and returns a [MediaType] from it.
         * Used when a search button is pressed to tell the app which type of media
         * 'container' we should be asking the server for.
         * @param viewID The ID of the search filter button
         * @return MediaType, the Media object type which corresponds to that button**/
        fun viewIDToDataType(viewID: Int): MediaType {
            return when (viewID) {
                R.id.btnGenre -> {
                    MediaType.GENRE
                }
                R.id.btnArtist -> {
                    MediaType.ARTIST
                }
                R.id.btnAlbum -> {
                    MediaType.ALBUM
                }
                else -> {
                    MediaType.SONG
                }
            }
        }

        /**Used in tracking to create JSON strings from key and value arrays
         * @param keys The string array for the keys
         * @param values The string array for the values
         * @return String, the json string of the paired keys and values**/
        fun generatePayload(keys: Array<String>, values: Array<String>): String {
            val payload = JSONObject()

            for ((i, s) in keys.withIndex()) {
                payload.put(s, values[i])
            }

            return payload.toString()
        }

        /**Ease of use function to stop me having to type this::class.java.name every time I wanted proper error logging
         * @param any The class calling the log function
         * @param s The string to log**/
        fun log(any: Any, s: String) {
            Log.e(any::class.java.name, s)
        }

        /**Takes a Media object, and returns the POTENTIAL local path based off media ID
         * This isn't a given that the file will exist, only that it might be there, or it should be but in this path
         * @param media The media to generate the path from
         * @return String, the path to the media**/
        fun songToPath(media: Media): String {
            return "${Settings.getSetting(Settings.storage)!!}/${media.id}"
        }

        /**Takes a Media object, and returns the POTENTIAL local path based off media ID
         * This isn't a given that the file will exist, only that it might be there, or it should be but in this path
         * @param media The media to generate the path from
         * @return String, the path to the media**/
        fun albumToPath(media: Media): String {
            return "${Settings.getSetting(Settings.storage)!!}/album/${media.id}"
        }

        /**Takes the URL to an album artwork image, extracts the ID and generates a local path instead
         * Used when media objects aren't available, such as when media data is sent from service to activity in media meta data
         * @param url The artwork URL
         * @return String, the local path**/
        fun albumURLToAlbumPath(url: String): String {
            val id = url.substring(url.lastIndexOf('/') + 1, url.length)
            return "${Settings.getSetting(Settings.storage)!!}/album/${id}"
        }

        /**Extension function of the [MainActivity.report] function. Makes a snackbar text, which may or not be 'urgent'
         * If not urgent, the user will only see it if they have aggressive error reporting enabled
         * @param s The text to display
         * @param activity The reference to main activity. This is required as snackbar needs a view to latch to
         * @param b The urgency of the message - defaults to false**/
        fun report(s: String, activity: MainActivity, b: Boolean = false) {
            activity.report(s, b)
        }

        /**Write text to a private file in /data/data/pw.dvd604.music
         * Used to store offline media lists
         * @param context Used to open the file
         * @param file The filename as a string
         * @param text the contents to write to the file**/
        fun writeToFile(context: Context, file: String, text: String) {
            context.openFileOutput(file, Context.MODE_PRIVATE).use {
                it.write(text.toByteArray())
            }
        }

        /**The opposite of [writeToFile]
         * Takes a context and file name, and returns the file contents as a long, possibly null string
         * @param context Context to open file from
         * @param file The filename
         * @return String, nullable file contents**/
        fun readFromFile(context: Context, file: String): String? {
            if (!context.getFileStreamPath(file).exists()) return null

            val fis = context.openFileInput(file)
            val inputStreamReader = InputStreamReader(fis)
            val bufferedReader = BufferedReader(inputStreamReader)
            val builder = StringBuilder()

            do {
                val line: String? = bufferedReader.readLine()
                builder.append(line)
            } while (line != null)

            bufferedReader.close()
            inputStreamReader.close()
            fis.close()
            return builder.toString()
        }

        /**Takes an [ArrayList] of type [T], iterates through creating a new, and distinctly different [ArrayList] of the same Type [T]
         * Which is then returned. Used to ensure that changes to ArrayList A are not carried over to ArrayList B by previously setting them equal
         * @param array ArrayList to copy
         * @return ArrayList<T>, new ArrayList with same contents**/
        fun <T : Any> duplicateArrayList(array: ArrayList<T>): ArrayList<T> {
            val list = ArrayList<T>(0)
            for (s in array) {
                list.add(s)
            }
            return list
        }

        /**Takes a string, such as "artist", and returns the correct [MediaType]. Used in the user entered blacklist settings to
         * convert a string to something the app can process
         * If the string is not recognised, [MediaType.SONG] is returned
         * @param value Type string
         * @return MediaType, the correct media data type**/
        fun stringToDataType(value: String): MediaType {
            return when (value.toLowerCase(Locale.getDefault())) {
                "artist" -> {
                    MediaType.ARTIST
                }
                "media" -> {
                    MediaType.SONG
                }
                "playlist" -> {
                    MediaType.PLAYLIST
                }
                "album" -> {
                    MediaType.ALBUM
                }
                "genre" -> {
                    MediaType.GENRE
                }
                else -> MediaType.SONG
            }
        }

        /**Used everywhere a notification should be made. A system requirement to give users control over notification importance.
         * @param context The context to create the notification channel with
         * @param channelId The Channel Id
         * @param name The name of the channel
         * @param descriptionText Channel description
         * @param importance Defaults to [NotificationManager.IMPORTANCE_LOW]**/
        fun createNotificationChannel(
            context: Context,
            channelId: String,
            name: String,
            descriptionText: String,
            importance: Int = NotificationManager.IMPORTANCE_LOW
        ) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, name, importance).apply {
                    description = descriptionText
                }
                // Register the channel with the system
                val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun idToSong(id: String): Media? {
            val list = SongList.songList.filter { it.id == id }
            return if (list.isNotEmpty()) {
                list[0]
            } else {
                null
            }
        }
    }
}
