package pw.dvd604.music.service

import android.content.Context
import android.content.Intent
import pw.dvd604.music.util.SafeBroadcastReceiver

class MusicIntentController(private val service: MediaService) : SafeBroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PLAY -> {
                if (service.player.isPlaying) {
                    service.mediaSession.controller.transportControls.pause()
                } else {
                    service.mediaSession.controller.transportControls.play()
                }
            }
            SKIP -> {
                service.mediaSession.controller.transportControls.skipToNext()
            }
            PREV -> {
                service.mediaSession.controller.transportControls.skipToPrevious()
            }
        }
    }

    companion object {
        private const val ROOT = "pw.dvd604.music"
        const val PLAY = "$ROOT.PLAY"
        const val SKIP = "$ROOT.NEXT"
        const val PREV = "$ROOT.PREV"
    }

}