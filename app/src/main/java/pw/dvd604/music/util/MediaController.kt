package pw.dvd604.music.util

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.android.volley.Response
import org.json.JSONArray
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song

class MediaController : Service(), MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
    MediaPlayer.OnCompletionListener {

    companion object {
        private const val intentRoot = "pw.dvd604.music"
        const val playIntentCode: String = "$intentRoot.PLAY"
        const val nextIntentCode: String = "$intentRoot.NEXT"
        const val prevIntentCode: String = "$intentRoot.PREV"
        const val pauseIntentCode: String = "$intentRoot.PAUSE"
    }

    private val notificationId = 6969

    private var shuffleCount = 0
    private var http: HTTP? = null
    private var mediaPlayer: MediaPlayer? = null
    private var bR: IntentReceiver? = null
    var nextSong: Song? = null
    var lastSong: Song? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        val filter = IntentFilter()
        filter.addAction(playIntentCode)
        filter.addAction(nextIntentCode)
        filter.addAction(prevIntentCode)
        filter.addAction(pauseIntentCode)

        bR = IntentReceiver(this)

        this.registerReceiver(bR, filter)
        http = HTTP(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        this.unregisterReceiver(bR)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        createNotificationChannel()

        startForeground(notificationId, createNotification(intent.getSerializableExtra("song") as Song))

        when (intent.action) {
            playIntentCode -> {
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
                mediaPlayer?.setOnCompletionListener(this)

                /*val wifiManager = this.getSystemService(Context.WIFI_SERVICE) as WifiManager
                val wifiLock: WifiManager.WifiLock =
                    wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, "petifyWifiLock")
                What the fuck
                wifiLock.acquire()*/

                mediaPlayer?.apply {
                    setDataSource(intent.getStringExtra("url"))
                    prepareAsync()
                }

                http?.getReq(HTTP.getQueue(), QueueListener(this))
            }
        }
        return START_STICKY
    }

    override fun onPrepared(p0: MediaPlayer?) {
        mediaPlayer?.start()
    }

    private fun createNotification(song: Song, notify: Boolean = false): Notification? {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT
        }
        intent.putExtra("song", song)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val prevIntent = Intent(prevIntentCode)
        val prevPIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, prevIntent, 0)

        val nextIntent = Intent(nextIntentCode)
        val nextPIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0)

        val pauseIntent = Intent(pauseIntentCode)
        val pausePIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, pauseIntent, 0)


        val builder = NotificationCompat.Builder(this, "petify_Not_panel")
            .setSmallIcon(R.drawable.baseline_shuffle_white_18)
            .setContentTitle("Now Playing:")
            .setContentText("${song.name} - ${song.author}")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.baseline_skip_previous_white_18, "prev", prevPIntent)
            .addAction(R.drawable.baseline_pause_white_18, "pause", pausePIntent)
            .addAction(R.drawable.baseline_skip_next_white_18, "next", nextPIntent)

        if (notify) {
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("petify_Not_panel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        return true
    }

    //TODO: Forgive me father for I have sinned, beyond this point

    override fun onCompletion(mp: MediaPlayer?) {
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
        mediaPlayer?.setOnErrorListener(this)
        mediaPlayer?.setOnPreparedListener(this)
        mediaPlayer?.setOnCompletionListener(this)

        mediaPlayer?.apply {
            setDataSource(Util.songToUrl(nextSong))
            prepareAsync()
        }

        nextSong?.let { createNotification(it, true) }
        shuffleCount = shuffleCount % 9 + 1
        http?.getReq(HTTP.getQueue(), QueueListener(this))
    }

    class IntentReceiver(private val service: MediaController) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            //TODO: Fix this shit out of this
            when (intent?.action) {
                pauseIntentCode -> {
                    service.mediaPlayer?.pause()
                }
                nextIntentCode -> {
                    if (service.mediaPlayer != null) {
                        service.mediaPlayer?.reset()
                        service.mediaPlayer?.release()
                    }
                    service.mediaPlayer = MediaPlayer()
                    service.mediaPlayer?.setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    service.mediaPlayer?.setOnErrorListener(service)
                    service.mediaPlayer?.setOnPreparedListener(service)
                    service.mediaPlayer?.setOnCompletionListener(service)

                    service.mediaPlayer?.apply {
                        setDataSource(Util.songToUrl(service.nextSong))
                        prepareAsync()
                    }

                    service.nextSong?.let { service.createNotification(it, true) }
                    service.shuffleCount = service.shuffleCount % 9 + 1
                    service.http?.getReq(HTTP.getQueue(), QueueListener(service))
                }
                prevIntentCode -> {
                    if (service.mediaPlayer != null) {
                        service.mediaPlayer?.reset()
                        service.mediaPlayer?.release()
                    }
                    service.mediaPlayer = MediaPlayer()
                    service.mediaPlayer?.setAudioAttributes(
                        AudioAttributes
                            .Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    service.mediaPlayer?.setOnErrorListener(service)
                    service.mediaPlayer?.setOnPreparedListener(service)
                    service.mediaPlayer?.setOnCompletionListener(service)

                    service.mediaPlayer?.apply {
                        setDataSource(Util.songToUrl(service.lastSong))
                        prepareAsync()
                    }

                    service.lastSong?.let { service.createNotification(it, true) }
                }
                playIntentCode -> {
                    service.mediaPlayer?.start()
                }
            }
        }

    }

    class QueueListener(private val mediaController: MediaController) : Response.Listener<String> {
        override fun onResponse(response: String?) {
            val array = JSONArray(response)
            val song: Song = Util.jsonToSong(array.getJSONObject(mediaController.shuffleCount))
            mediaController.lastSong = mediaController.nextSong
            mediaController.nextSong = song
        }

    }
}

