package pw.dvd604.music

import android.app.Application
import pw.dvd604.music.util.Settings

class MusicApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Settings.init(this)

        if (Settings.getBoolean(Settings.crashReports, true)) {

        }
    }
}