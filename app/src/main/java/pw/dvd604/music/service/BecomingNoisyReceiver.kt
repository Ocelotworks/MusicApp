package pw.dvd604.music.service

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import pw.dvd604.music.util.SafeBroadcastReceiver

class BecomingNoisyReceiver(private val service: MediaPlaybackService) : SafeBroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (action?.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
            if (MediaContainer.player.isPlaying) {
                service.mediaSession?.controller?.transportControls?.pause()
            }
        }
    }
}