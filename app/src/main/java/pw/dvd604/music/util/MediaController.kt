package pw.dvd604.music.util

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_playing.*
import org.json.JSONArray
import pw.dvd604.music.MainActivity
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.fragment.NowPlayingFragment
import java.util.*

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
    var currentSong: Song? = null

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

        var timer = Timer()
        timer.scheduleAtFixedRate(createRunnable(this), 0,1000)
    }

    private fun createRunnable(media : MediaController): TimerTask = object : TimerTask() {
        override fun run() {
            val intent = Intent(NowPlayingFragment.timingIntent)

            mediaPlayer?.let {
                intent.putExtra("time",it.currentPosition/1000)
            }
            media.sendBroadcast(intent)
        }
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
                currentSong = intent.getSerializableExtra("song") as Song
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

                (this.application as MusicApplication).mixpanel?.track(
                    "Song play",
                    Util.songToJson(intent.getSerializableExtra("song") as Song)
                )

                mediaPlayer?.apply {
                    setDataSource(intent.getStringExtra("url"))
                    prepareAsync()
                }

                http?.getReq(HTTP.getQueue(), QueueListener(this))
            }
        }
        return START_STICKY
    }

    private fun prepMediaPlayer(shuffle: Boolean = false) {
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

        val intent = Intent(NowPlayingFragment.songIntent)
        intent.putExtra("song", nextSong)
        currentSong = nextSong
        this.sendBroadcast(intent)
        if (!shuffle)
            return


        nextSong?.let {
            createNotification(it, true)
            (this.application as MusicApplication).mixpanel?.track("Song play", Util.songToJson(it))
        }
        shuffleCount = shuffleCount % 9 + 1
        http?.getReq(HTTP.getQueue(), QueueListener(this))
    }

    override fun onPrepared(p0: MediaPlayer?) {
        mediaPlayer?.start()
    }

    private fun createNotification(song: Song, notify: Boolean = false, pausePlay: Boolean = false): Notification? {
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

        val playIntent = Intent(playIntentCode)
        val playPIntent: PendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0)

        val builder = NotificationCompat.Builder(this, "petify_Not_panel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setSound(null)
            .setVibrate(null)
            .setContentTitle("Now Playing:")
            .setContentText("${song.name} - ${song.author}")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.baseline_skip_previous_white_18, "prev", prevPIntent)

        if (pausePlay) {
            builder.addAction(R.drawable.baseline_play_arrow_white_18, "play", playPIntent)
        } else {
            builder.addAction(R.drawable.baseline_pause_white_18, "pause", pausePIntent)
        }
        builder.addAction(R.drawable.baseline_skip_next_white_18, "next", nextPIntent)



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
            val importance = NotificationManager.IMPORTANCE_MIN
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

    override fun onCompletion(mp: MediaPlayer?) {
        prepMediaPlayer(true)
    }

    class IntentReceiver(private val service: MediaController) : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                pauseIntentCode -> {
                    service.mediaPlayer?.pause()
                    service.currentSong?.let { service.createNotification(it, notify = true, pausePlay = true) }
                }
                nextIntentCode -> {
                    service.prepMediaPlayer(true)
                }
                prevIntentCode -> {
                    service.prepMediaPlayer()
                    service.lastSong?.let { service.createNotification(it, true) }
                }
                playIntentCode -> {
                    service.mediaPlayer?.start()
                    service.currentSong?.let { service.createNotification(it, notify = true, pausePlay = false) }
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

