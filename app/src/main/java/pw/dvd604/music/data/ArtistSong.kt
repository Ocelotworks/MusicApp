package pw.dvd604.music.data

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat

class ArtistSong {

    lateinit var song: Song

    lateinit var artist: Artist

    fun toCardData(): CardData {
        return CardData(
            title = song.title,
            id = song.id,
            type = "artistsong",
            url = artist.url,
            subtext = artist.title
        )
    }

    fun toMediaItem(): MediaBrowserCompat.MediaItem {
        val descriptionBuilder =
            MediaDescriptionCompat.Builder().setMediaId(song.id).setTitle(song.title)
                .setSubtitle(artist.title)
        return MediaBrowserCompat.MediaItem(
            descriptionBuilder.build(),
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        )
    }
}