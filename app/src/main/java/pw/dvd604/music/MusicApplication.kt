package pw.dvd604.music

import android.app.Activity
import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import pw.dvd604.music.data.storage.DatabaseHelper
import pw.dvd604.music.util.Settings
import kotlin.system.exitProcess


class MusicApplication : Application(), Application.ActivityLifecycleCallbacks {

    lateinit var internalStorage: String
    lateinit var dbHelper: DatabaseHelper
    lateinit var database: SQLiteDatabase
    lateinit var readableDatabase: SQLiteDatabase

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
        registerActivityLifecycleCallbacks(this)

        tracker = TrackerBuilder.createDefault(BuildConfig.apiURL, BuildConfig.siteID)
            .build(Matomo.getInstance(this))

        internalStorage = filesDir.path

        Settings.init(this)

        GlobalScope.launch {
            try {
                dbHelper = DatabaseHelper(this@MusicApplication)
                database = dbHelper.writableDatabase
                readableDatabase = dbHelper.readableDatabase
            } catch (e: Exception) {
                Log.e("Neilify Database", "", e)
            }
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

    override fun onActivityPaused(p0: Activity?) {
        track("APP STATE", "Paused")
    }

    override fun onActivityResumed(p0: Activity?) {
        track("APP STATE", "Resumed")
    }

    override fun onActivityStarted(p0: Activity?) {
        track("APP STATE", "Started")
    }

    override fun onActivityDestroyed(p0: Activity?) {
        track("APP STATE", "Destroyed")
        dbHelper.close()
    }

    override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {
        track("APP STATE", "SIS'd")
    }

    override fun onActivityStopped(p0: Activity?) {
        track("APP STATE", "Stopped")
    }

    override fun onActivityCreated(p0: Activity?, p1: Bundle?) {
        track("APP STATE", "Created")
    }
}