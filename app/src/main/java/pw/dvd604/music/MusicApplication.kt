package pw.dvd604.music

import android.app.Application
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import pw.dvd604.music.util.Settings
import kotlin.system.exitProcess


class MusicApplication : Application() {

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

        //here is where we initialise everything that needs a context, but we want to keep out of the main activity for neatness sake

        Settings.init(this)

        if (Settings.getBoolean(Settings.tracking, true)) {
            tracker = TrackerBuilder.createDefault(BuildConfig.apiURL, BuildConfig.siteID)
                .build(Matomo.getInstance(this))
            track("App Event", "Start")
        }


        if (Settings.getBoolean(Settings.crashReports, true)) {
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
}