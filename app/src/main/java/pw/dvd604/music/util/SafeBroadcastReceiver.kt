package pw.dvd604.music.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

abstract class SafeBroadcastReceiver : BroadcastReceiver() {
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
}