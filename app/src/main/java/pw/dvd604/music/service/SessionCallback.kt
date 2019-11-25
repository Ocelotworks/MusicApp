package pw.dvd604.music.service

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

class SessionCallback(private val service: MediaPlaybackService) : MediaSessionCompat.Callback() {


    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {

    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        service.mMediaContainer.prepare(uri)
    }

    override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
        service.mMediaContainer.play(uri)
    }

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        service.mMediaContainer.prepare(mediaId)
    }

    override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
        service.mMediaContainer.play(mediaId)
    }

    override fun onPlay() {
        service.mMediaContainer.play()
    }


    override fun onSkipToPrevious() {

    }

    override fun onSkipToNext() {

    }

    override fun onStop() {

    }

    override fun onPause() {

    }
}