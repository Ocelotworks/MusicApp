package pw.dvd604.music.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.wifi.WifiManager
import android.os.IBinder
import android.os.PowerManager
import android.widget.SeekBar

class MediaController : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    companion object {
        private const val intentRoot = "pw.dvd604.music"
        const val playIntent: String = "$intentRoot.PLAY"
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        when (intent.action) {
            playIntent -> {
                if (mediaPlayer != null) {
                    mediaPlayer?.reset()
                    mediaPlayer?.release()
                }
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                //mediaPlayer?.setWakeMode(this, PowerManager.PARTIAL_WAKE_LOCK)
                mediaPlayer?.setOnErrorListener(this)
                mediaPlayer?.setOnPreparedListener(this)

                /*val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiLock: WifiManager.WifiLock =
                    wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "petifyWifiLock")
                What the fuck
                wifiLock.acquire()*/

                mediaPlayer?.apply {
                    setDataSource(intent.getStringExtra("url"))
                    prepareAsync()
                }
            }
        }
        return START_STICKY
    }

    override fun onPrepared(p0: MediaPlayer?) {
        mediaPlayer?.start()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return true
    }
}