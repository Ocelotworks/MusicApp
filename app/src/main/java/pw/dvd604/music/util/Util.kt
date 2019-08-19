package pw.dvd604.music.util

import android.media.browse.MediaBrowser
import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import org.json.JSONObject
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import java.lang.Exception

class Util {
    companion object {
        fun prettyTime(seconds: Int): String {
            val mins: Int = (seconds % 3600 / 60)
            val secs: Int = seconds % 60

            return "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
        }

        fun jsonToSong(json: JSONObject): Song {
            try {
                val song = Song(
                    json.getString("title"),
                    json.getString("name"),
                    json.getString("song_id"),
                    json.getString("album"),
                    "",
                    json.getString("artist_id")
                )
                return song
            } catch (e: Exception) {
                val song = Song(
                    json.getString("title"),
                    json.getString("artist"),
                    json.getString("id"),
                    json.getString("album"),
                    "",
                    json.getString("artistID")
                )
                return song
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
            return "https://unacceptableuse.com/petify/song/${song?.id}"
        }

        fun idToString(id: Int): String {
            when (id) {
                R.id.btnTitle -> {
                    return "Title filter"
                }
                R.id.btnAlbum -> {
                    return "Album filter"
                }
                R.id.btnGenre -> {
                    return "Genre filter"
                }
                R.id.btnArtist -> {
                    return "Artist filter"
                }

                R.id.btnPause -> {
                    return "Pause"
                }
                R.id.btnNext -> {
                    return "Skip"
                }
                R.id.btnPrev -> {
                    return "Prev"
                }
                R.id.btnStar -> {
                    return "Star"
                }
                R.id.btnShuffle -> {
                    return "Shuffle"
                }
                else -> {
                    return "Button"
                }
            }
        }

        fun songToMediaItem(song: Song): MediaBrowserCompat.MediaItem {
            val descriptionBuilder = MediaDescriptionCompat.Builder().apply {
                setTitle(song.name)
                setDescription(song.author)
                setMediaUri(Uri.parse(songToUrl(song)))
                setIconUri(Uri.parse("https://unacceptableuse.com/petify/album/${song.album}"))
                setMediaId(song.id)
            }
            return MediaBrowserCompat.MediaItem(descriptionBuilder.build(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
        }
    }
}
