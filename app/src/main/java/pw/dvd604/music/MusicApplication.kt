package pw.dvd604.music

import android.app.Application
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory
import pw.dvd604.music.util.Settings
import kotlin.system.exitProcess


class MusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Settings.init(this)

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