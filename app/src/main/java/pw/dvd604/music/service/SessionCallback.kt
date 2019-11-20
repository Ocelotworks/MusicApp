package pw.dvd604.music.service

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.session.MediaSessionCompat

class SessionCallback(val service: MediaPlaybackService) : MediaSessionCompat.Callback() {
    override fun onPlayFromSearch(query: String?, extras: Bundle?) {
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        onPlayFromSearch(query, extras)
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        super.onPrepareFromUri(uri, extras)
    }

    override fun onPlay() {
        super.onPlay()
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