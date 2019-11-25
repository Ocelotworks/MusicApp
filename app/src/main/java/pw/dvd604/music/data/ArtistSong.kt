package pw.dvd604.music.data

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat

class ArtistSong {
    var songID: String = ""
    var artistTitle: String = ""
    var songTitle = ""

    fun toCardData(): CardData {
        return CardData(title = songTitle, id = songID, type = "", url = "", subtext = artistTitle)
    }

    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        val descriptionBuilder =
            MediaDescriptionCompat.Builder().setMediaId(songID).setTitle(songTitle)
                .setSubtitle(songTitle)
        return MediaBrowserCompat.MediaItem(
            descriptionBuilder.build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
}