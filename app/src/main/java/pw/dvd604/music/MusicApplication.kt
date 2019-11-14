package pw.dvd604.music

import android.app.Application
import androidx.room.Room
import androidx.room.RoomDatabase
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import pw.dvd604.music.data.room.AppDatabase
import kotlin.system.exitProcess


class MusicApplication : Application() {

    lateinit var db: RoomDatabase

    companion object {
        private var tracker: Tracker? = null
        fun track(category: String, event: String) {
            if (tracker != null) {
                TrackHelper.track().event(category, event).with(tracker)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        GlobalScope.launch {
            db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, "petify-db"
            ).build()
        }


        Sentry.init(
            BuildConfig.sentryKey,
            AndroidSentryClientFactory(this)
        )


        Thread.setDefaultUncaughtExceptionHandler { _, throwable ->
            //Catch your exception
            // Without System.exit() this will not work.
            Sentry.capture(throwable)
            exitProcess(2)
        }
    }
}