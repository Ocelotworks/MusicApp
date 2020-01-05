package pw.dvd604.music

import android.app.Application
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.matomo.sdk.Tracker
import org.matomo.sdk.extra.TrackHelper
import pw.dvd604.music.util.Settings
import kotlin.system.exitProcess


class MusicApplication : Application() {

    lateinit var internalStorage: String

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

        internalStorage = filesDir.path

        Settings.init(this)


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