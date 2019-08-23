package pw.dvd604.music.util

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import org.json.JSONObject
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.adapter.data.SongDataType
import java.util.*
import kotlin.collections.ArrayList

class Util {
    companion object {
        private var tempMetadataCompat: MediaMetadataCompat.Builder? = null
        private val previousSongs = ArrayList<Song>(0)
        var songQueue: ArrayList<Song>? = null
        var downloader = Downloader()

        fun prettyTime(seconds: Int): String {
            val mins: Int = (seconds % 3600 / 60)
            val secs: Int = seconds % 60

            return "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
        }

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

        fun songToJson(song: Song): JSONObject? {
            return JSONObject()
                .put("title", song.name)
                .put("name", song.author)
                .put("song_id", song.id)
                .put("album", song.album)
                .put("genre", song.genre)
                .put("artist_id", song.artistID)
        }

        fun songToUrl(song: Song?): String {
            if (Settings.getBoolean(Settings.offlineMusic)) {
                //Do offline stored check
                if (downloader.hasSong(song)) {
                    return "TODO"
                }
            }
            return "${Settings.getSetting(
                Settings.server,
                "https://unacceptableuse.com/petify"
            )}/song/${song?.id}"
        }

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

        fun songToMediaItem(song: Song): MediaBrowserCompat.MediaItem {
            val descriptionBuilder = MediaDescriptionCompat.Builder().apply {
                setTitle(song.name)
                setDescription(song.author)
                setMediaUri(Uri.parse(songToUrl(song)))
                setIconUri(Uri.parse(songToAlbumURL(song)))
                setMediaId(song.id)
            }
            return MediaBrowserCompat.MediaItem(descriptionBuilder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
        }

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

        fun addMetadata(duration: Int?): MediaMetadataCompat {
            duration?.let { durationNN ->
                tempMetadataCompat?.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, durationNN.toLong())
                tempMetadataCompat?.let {
                    return it.build()
                }
            }
            return MediaMetadataCompat.Builder().build()
        }

        fun addMetadataProgress(duration: Int?): MediaMetadataCompat {
            duration?.let { durationNN ->
                tempMetadataCompat?.putLong("progress", durationNN.toLong())
                tempMetadataCompat?.let {
                    return it.build()
                }
            }
            return MediaMetadataCompat.Builder().build()
        }

        private fun songToAlbumURL(song: Song): String? {
            return "${Settings.getSetting(Settings.server)}/album/${song.album}"
        }

        fun addSongToStack(song: Song?) {
            song?.let { previousSongs.add(it) }
        }

        fun popSongStack(): Song {
            previousSongs.removeAt(previousSongs.size - 1)
            return previousSongs[previousSongs.size - 1]
        }

        fun getTrackingID(): String {
            val uuid = Settings.getSetting(Settings.tracking, UUID.randomUUID().toString())
            Settings.putString(Settings.tracking, uuid)

            return uuid ?: ""
        }

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

        fun log(any: Any, s: String) {
            Log.e(any::class.java.name, s)
        }
    }
}
