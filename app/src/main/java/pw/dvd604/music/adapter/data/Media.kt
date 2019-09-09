package pw.dvd604.music.adapter.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import org.json.JSONObject
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.download.BitmapAsync
import java.io.File
import java.io.Serializable


enum class MediaType {
    SONG, ARTIST, GENRE, ALBUM, PLAYLIST;

    companion object {
        fun getNonSong(): Array<MediaType> {
            return arrayOf(ARTIST, GENRE, ALBUM, PLAYLIST)
        }
    }
}

/**
 * The 'Media' datatype
 **/
data class Media(
    var name: String = "",
    var author: String = "",
    var id: String = "",
    var album: String = "",
    var genre: String = "",
    var artistID: String = "",
    var hash: String = "",
    val type: MediaType = MediaType.SONG
) : Serializable {

    companion object {
        private const val serialVersionUID = 20180617104400L
    }

    fun generateText(): String {

        val separator: String = if (type != MediaType.SONG) {
            " "
        } else {
            " - "
        }

        return "$author$separator$name"
    }

    /**Creates a Media object from a JSON string
     * @param [json] The json object
     * @return Media**/
    fun fromJson(json: JSONObject): Media {
        name = safeGet(json, "title")
        author = safeGet(json, "name")
        id = safeGet(json, "song_id")
        album = safeGet(json, "album")
        genre = safeGet(json, "genre")
        artistID = safeGet(json, "artist_id")
        hash = safeGet(json, "hash")
        return this
    }

    private fun safeGet(json: JSONObject, key: String): String {
        return try {
            json.getString(key)
        } catch (e: Exception) {
            Util.log(this, e.localizedMessage)
            Util.log(this, json.toString())
            ""
        }
    }

    /**Creates a JSON object from this Media
     * @return JSONObject**/
    fun toJson(): JSONObject {
        return JSONObject()
            .put("title", name)
            .put("name", author)
            .put("song_id", id)
            .put("album", album)
            .put("genre", genre)
            .put("artist_id", artistID)
            .put("hash", hash)
    }

    /**Returns the location of the media file for a given media
     * Supports offline play, where the local directory is returned, and online play,
     * where the online URL is returned. This online play respects the user defined server URL
     * @return String, The URL of the media, local or server-based**/
    fun toUrl(): String {
        if (Settings.getBoolean(Settings.offlineMusic)) {
            //Do offline stored check
            if (Util.downloader.hasSong(this)) {
                return this.toPath()
            }
        }
        return "${Settings.getSetting(
            Settings.server
        )}/song/${id}"
    }

    /**Takes a given Media, and creates a MediaBroswerCompat MediaItem.
     * This is used for populating the MediaService, and allowing external MediaControllers to parse what music we have
     * @return MediaItem, The Android MediaItem based off the Media**/
    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        val descriptionBuilder = MediaDescriptionCompat.Builder().apply {
            setTitle(name)
            setDescription(author)
            setMediaUri(Uri.parse(this@Media.toUrl()))
            setIconUri(Uri.parse(this@Media.toAlbumUrl()))
            setMediaId(id)
        }
        return MediaBrowserCompat.MediaItem(
            descriptionBuilder.build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }

    fun toBitmap(): Bitmap? {
        val file = File(this.toAlbumPath())
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.canonicalPath)
        } else {
            BitmapAsync(null, false).execute(this@Media.toAlbumUrl()).get()
        }
    }

    /**Creates a URL pointing to the media album art.
     * While this doesn't support offline storage at the minute, it will in future
     * @return String, the album art URL**/
    fun toAlbumUrl(): String? {
        return "${Settings.getSetting(Settings.server)}/album/${album}"
    }

    /**Takes a Media object, and returns the POTENTIAL local path based off media ID
     * This isn't a given that the file will exist, only that it might be there, or it should be put in this path
     * @return String, the path to the media**/
    fun toPath(): String {
        return "${Settings.getSetting(Settings.storage)}/${id}"
    }

    /**Takes a Media object, and returns the POTENTIAL local path based off media ID
     * This isn't a given that the file will exist, only that it might be there, or it should be but in this path
     * @return String, the path to the media**/
    fun toAlbumPath(): String {
        return "${Settings.getSetting(Settings.storage)}/album/${id}"
    }

}