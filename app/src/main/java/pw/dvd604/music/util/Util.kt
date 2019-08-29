package pw.dvd604.music.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import org.json.JSONObject
import pw.dvd604.music.MainActivity
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType
import pw.dvd604.music.util.download.Downloader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.collections.ArrayList


class Util {

    companion object {
        /**Used to generate song metadata from the MediaService**/
        private var tempMetadataCompat: MediaMetadataCompat.Builder? = null
        /**Used to track the previous songs played**/
        private val previousSongs = ArrayList<Song>(0)
        /**Used to track the songQueue**/
        var songQueue: ArrayList<Song>? = null
        /**Instance of the Downloader**/
        @SuppressLint("StaticFieldLeak") //TODO: Fix this better later
        lateinit var downloader: Downloader

        fun getApplication(activity: Activity): MusicApplication {
            return activity.application as MusicApplication
        }

        /**Creates a human readable string representing song duration
         * @param seconds The length of song in seconds in a non-nullable Int
         * @return String**/
        fun prettyTime(seconds: Int): String {
            val mins: Int = (seconds % 3600 / 60)
            val secs: Int = seconds % 60

            return "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
        }

        /**Creates a human readable string representing song duration. If the seconds value is null, returns "00:00"
         * @param seconds The length of song in seconds in a nullable Long
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

        /**Creates a Song object from a JSON string
         * @param [json] The json object
         * @return Song**/
        fun jsonToSong(json: JSONObject): Song {
            return try {
                Song(
                    json.getString("title"),
                    json.getString("name"),
                    json.getString("song_id"),
                    json.getString("album"),
                    "",
                    json.getString("artist_id")
                )
            } catch (e: Exception) {
                Song(
                    json.getString("title"),
                    json.getString("artist"),
                    json.getString("id"),
                    json.getString("album"),
                    "",
                    json.getString("artistID")
                )
            }
        }

        /**Creates a JSON object from a Song
         * @param [song] The song to create the json from
         * @return JSONObject**/
        fun songToJson(song: Song): JSONObject? {
            return JSONObject()
                .put("title", song.name)
                .put("name", song.author)
                .put("song_id", song.id)
                .put("album", song.album)
                .put("genre", song.genre)
                .put("artist_id", song.artistID)
        }

        /**Returns the location of the media file for a given song
         * Supports offline play, where the local directory is returned, and online play,
         * where the online URL is returned. This online play respects the user defined server URL
         * @param song The song to get the location for
         * @return String, The URL of the song, local or server-based**/
        fun songToUrl(song: Song?): String {
            if (Settings.getBoolean(Settings.offlineMusic)) {
                //Do offline stored check
                if (downloader.hasSong(song)) {
                    return songToPath(song!!)
                }
            }
            return "${Settings.getSetting(
                Settings.server
            )}/song/${song?.id}"
        }

        /**Takes a given Song, and creates a MediaBroswerCompat MediaItem.
         * This is used for populating the MediaService, and allowing external MediaControllers to parse what music we have
         * @param song The song to create the media item from
         * @return MediaItem, The Android MediaItem based off the Song**/
        fun songToMediaItem(song: Song): MediaBrowserCompat.MediaItem {
            val descriptionBuilder = MediaDescriptionCompat.Builder().apply {
                setTitle(song.name)
                setDescription(song.author)
                setMediaUri(Uri.parse(songToUrl(song)))
                setIconUri(Uri.parse(songToAlbumURL(song)))
                setMediaId(song.id)
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

        /**Takes a Song, and returns the Song metadata, including title, artist, genre, and art location.
         * Has some trickery to do with song duration too, by storing the unbuilt metadata, before returning the metadata.
         * This allows [addMetadata] and [addMetadataProgress] to return the same basic metadata as this method, while adding additional information.
         * Probably not the best way to do it, but things are constantly getting changed
         * @param song The song to build the metadata from
         * @param builder Whether or not this should potentially destroy the metadata by building it
         * @return MetaData, if [builder] is true, and null if [builder] is false**/
        fun songToMetadata(song: Song, builder: Boolean = false): MediaMetadataCompat? {
            val metaData = MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_TITLE, song.name)
                .putText(MediaMetadataCompat.METADATA_KEY_ARTIST, song.author)
                .putText(MediaMetadataCompat.METADATA_KEY_GENRE, song.genre)
                .putText(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, songToAlbumURL(song))
            tempMetadataCompat = metaData
            if (!builder)
                return metaData.build()
            return null
        }

        /**@see songToMetadata
         * @param duration The song Duration
         * @return Completed song metadata**/
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
         * @param duration The song progess
         * @return Completed song metadata**/
        fun addMetadataProgress(duration: Int?): MediaMetadataCompat {
            duration?.let { durationNN ->
                tempMetadataCompat?.putLong("progress", durationNN.toLong())
                tempMetadataCompat?.let {
                    return it.build()
                }
            }
            return MediaMetadataCompat.Builder().build()
        }

        /**Creates a URL pointing to the song album art.
         * While this doesn't support offline storage at the minute, it will in future
         * @param song The requested song
         * @return String, the album art URL**/
        fun songToAlbumURL(song: Song): String? {
            return "${Settings.getSetting(Settings.server)}/album/${song.album}"
        }

        /**Adds a song to the last played list
         * @param song The last played song**/
        fun addSongToStack(song: Song?) {
            song?.let { previousSongs.add(it) }
        }

        /**Pops the last played song from the stack, deletes it, and returns it here
         * @return Song, the last played song**/
        fun popSongStack(): Song {
            previousSongs.removeAt(previousSongs.size - 1)
            return previousSongs[previousSongs.size - 1]
        }

        /**Only using when tracking is enabled.
         * Creates a random UUID, as per Google recommendation, and returns it, while writing it to the shard prefs.
         * If there is a UUID in the shard prefs, it returns that instead. This way, a device will only have one generated tracking ID
         * This allows the UUID to be changed, but this is unlikely to happen, as anyone who is likely to change the ID
         * is more than likely to disable tracking
         * @return String, the UUID, or "" if [Settings] is not created beforehand**/
        fun getTrackingID(): String {
            val uuid = Settings.getSetting(Settings.tracking, UUID.randomUUID().toString())
            Settings.putString(Settings.tracking, uuid)

            return uuid ?: ""
        }

        /**Takes an internal Android view ID, and returns a [SongDataType] from it.
         * Used when a search button is pressed to tell the app which type of song
         * 'container' we should be asking the server for.
         * @param viewID The ID of the search filter button
         * @return SongDataType, the Song object type which corresponds to that button**/
        fun viewIDToDataType(viewID: Int): SongDataType {
            return when (viewID) {
                R.id.btnGenre -> {
                    SongDataType.GENRE
                }
                R.id.btnArtist -> {
                    SongDataType.ARTIST
                }
                R.id.btnAlbum -> {
                    SongDataType.ALBUM
                }
                else -> {
                    SongDataType.SONG
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

        /**Takes a Song object, and returns the POTENTIAL local path based off song ID
         * This isn't a given that the file will exist, only that it might be there, or it should be but in this path
         * @param song The song to generate the path from
         * @return String, the path to the song**/
        fun songToPath(song: Song): String {
            return "${Settings.getSetting(Settings.storage)!!}/${song.id}"
        }

        /**Takes a Song object, and returns the POTENTIAL local path based off song ID
         * This isn't a given that the file will exist, only that it might be there, or it should be but in this path
         * @param song The song to generate the path from
         * @return String, the path to the song**/
        fun albumToPath(song: Song): String {
            return "${Settings.getSetting(Settings.storage)!!}/album/${song.id}"
        }

        fun albumURLToAlbumPath(url: String): String {
            val id = url.substring(url.lastIndexOf('/') + 1, url.length)
            return "${Settings.getSetting(Settings.storage)!!}/album/${id}"
        }

        fun report(s: String, activity: MainActivity, b: Boolean = false) {
            activity.report(s, b)
        }

        fun writeToFile(context: Context, file: String, text: String) {
            context.openFileOutput(file, Context.MODE_PRIVATE).use {
                it.write(text.toByteArray())
            }
        }

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

        fun duplicateArrayList(downloadQueue: ArrayList<Song>): ArrayList<Song> {
            val list = ArrayList<Song>(0)
            for (s in downloadQueue) {
                list.add(s)
            }
            return list
        }
    }
}
