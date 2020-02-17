package pw.dvd604.music.data

import android.content.ContentValues
import android.media.MediaMetadata
import androidx.room.Entity
import org.json.JSONObject
import pw.dvd604.music.data.storage.DatabaseContract

@Entity(tableName = "song")
class Song(
    id: String = "",
    title: String = "",
    var duration: Int = 0,
    var hash: String = "",
    var albumID: String = "",
    var artistID: String = "",
    var genreID: String = ""
) : CardData(title, id, "song", "https://unacceptableuse.com/petify/album/") {

    companion object {
        fun parse(obj: JSONObject): Song {
            val song = Song()
            song.id = obj.getString("id")
            song.title = obj.getString("title")
            song.duration = obj.getInt("duration")
            song.hash = obj.getString("hash")
            song.albumID = obj.getString("albumID")
            song.artistID = obj.getString("artistID")
            song.genreID = obj.getString("genreID")
            return song
        }
    }

    fun toMetaData(artist: Artist): MediaMetadata {
        return MediaMetadata.Builder().apply {
            this.putText(MediaMetadata.METADATA_KEY_TITLE, title)
            this.putText(MediaMetadata.METADATA_KEY_ARTIST, artist.title)
            this.putText(MediaMetadata.METADATA_KEY_MEDIA_ID, id)
            this.putLong(MediaMetadata.METADATA_KEY_DURATION, duration.toLong())
        }.build()
    }

    fun toValues(): ContentValues {
        return ContentValues().apply {
            put("id", id)
            put(DatabaseContract.Song.COLUMN_NAME_TITLE, title)
            put(DatabaseContract.Song.COLUMN_NAME_DURATION, duration)
            put(DatabaseContract.Song.COLUMN_NAME_HASH, hash)
            put(DatabaseContract.Song.COLUMN_NAME_ALBUM, albumID)
            put(DatabaseContract.Song.COLUMN_NAME_ARTIST, artistID)
            put(DatabaseContract.Song.COLUMN_NAME_GENRE, genreID)
        }
    }
}