package pw.dvd604.music

import android.app.Activity
import android.app.Application
import android.os.Bundle
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util
import kotlin.system.exitProcess


class MusicApplication : Application(), Application.ActivityLifecycleCallbacks {
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

        if (Settings.getBoolean(Settings.usageReports)) {
            tracker = TrackerBuilder.createDefault(BuildConfig.apiURL, BuildConfig.siteID)
                .build(Matomo.getInstance(this))
            tracker?.userId = Util.getTrackingID()
            track(
                "App Event",
                Util.generatePayload(
                    arrayOf("event", "buildType"),
                    arrayOf("start", BuildConfig.BUILD_TYPE)
                )
            )

            this.registerActivityLifecycleCallbacks(this)
        }


        if (Settings.getBoolean(Settings.crashReports)) {
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

    override fun onActivityPaused(activity: Activity?) {
        track("Activity Event", Util.generatePayload(arrayOf("event"), arrayOf("Activity Paused")))
    }

    override fun onActivityResumed(activity: Activity?) {
        track("Activity Event", Util.generatePayload(arrayOf("event"), arrayOf("Activity Resumed")))
    }

    override fun onActivityStarted(activity: Activity?) {
        track("Activity Event", Util.generatePayload(arrayOf("event"), arrayOf("Activity Started")))
    }

    override fun onActivityDestroyed(activity: Activity?) {
        track(
            "Activity Event",
            Util.generatePayload(arrayOf("event"), arrayOf("Activity Destroyed"))
        )

        this@MusicApplication.unregisterActivityLifecycleCallbacks(this)
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        track("Activity Event", Util.generatePayload(arrayOf("event"), arrayOf("Activity SIS'd")))
    }

    override fun onActivityStopped(activity: Activity?) {
        track("Activity Event", Util.generatePayload(arrayOf("event"), arrayOf("Activity Stopped")))
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        track("Activity Event", Util.generatePayload(arrayOf("event"), arrayOf("Activity Created")))
    }
}