package pw.dvd604.music.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

class BecomingNoisyReceiver(private val service: MediaService) : BroadcastReceiver() {
    var registered: Boolean = false

    /**
     * register receiver
     * @param context - Context
     * @param filter - Intent Filter
     * @return see Context.registerReceiver(BroadcastReceiver,IntentFilter)
     */
    fun register(context: Context, filter: IntentFilter): Intent? {
        try {
            return if (!registered)
                context.registerReceiver(this, filter)
            else
                null
        } finally {
            registered = true
        }
    }

    /**
     * unregister received
     * @param context - context
     * @return true if was registered else false
     */
    fun unregister(context: Context): Boolean {
        return registered && unregisterInternal(context)
    }

    private fun unregisterInternal(context: Context): Boolean {
        context.unregisterReceiver(this)
        registered = false
        return true
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (action?.compareTo(AudioManager.ACTION_AUDIO_BECOMING_NOISY) == 0) {
            if (service.player.isPlaying) {
                service.mediaSession.controller.transportControls.pause()
            }
        }
    }
}