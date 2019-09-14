package pw.dvd604.music.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager
import pw.dvd604.music.BuildConfig

class Settings {
    companion object {
        const val server = "address"
        const val offlineMusic = "download"
        const val offlineAlbum = "albumArt"
        const val aggressiveReporting = "aggressiveReporting"
        const val usageReports = "usageReporting"
        const val crashReports = "crashReporting"
        const val storage = "storage"
        const val useIntents = "useIntents"
        const val shuffle = "shuffle"
        const val tracking = "trackingID"
        const val update = "autoUpdate"
        const val buildName = "buildName"
        const val shuffleOffline = "shuffleOffline"
        const val blacklist = "blacklist"
        const val developer = "developerOptions"
        const val running = "enableRunning"
        const val stepTarget = "stepPM"
        const val runningMargin = "marginOfError"
        const val minSongSpeed = "minSongSpeed"
        const val maxSongSpeed = "maxSongSpeed"

        private val prefDefault: HashMap<String, Any> = hashMapOf(
            server to BuildConfig.defaultURL,
            storage to "${Environment.getExternalStorageDirectory().path}/petify",
            offlineMusic to true,
            offlineAlbum to true,
            aggressiveReporting to true,
            usageReports to true,
            crashReports to true,
            useIntents to false,
            update to true,
            shuffle to true,
            shuffleOffline to true,
            blacklist to "",
            developer to false,
            running to false,
            stepTarget to 100,
            runningMargin to 50,
            minSongSpeed to 50,
            maxSongSpeed to 50
        )

        var prefs: SharedPreferences? = null

        fun init(context: Context) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun getSetting(name: String, defaultValue: String = ""): String {
            return prefs?.getString(
                name, if (defaultValue == "") {
                    prefDefault[name] as String
                } else {
                    defaultValue
                }
            ) ?: prefDefault[name] as String
        }

        fun getInt(name: String): Int {
            return prefs?.getInt(
                name, prefDefault[name] as Int
            ) ?: prefDefault[name] as Int
        }

        fun appendSetting(name: String, text: String) {
            val oldText = prefs?.getString(name, prefDefault[name] as String)
            prefs?.edit()?.putString(name, oldText + text)?.apply()
        }

        fun getBoolean(setting: String): Boolean {
            return prefs?.getBoolean(setting, prefDefault[setting] as Boolean)!!
        }

        fun putBoolean(name: String, value: Boolean) {
            prefs?.edit()?.putBoolean(name, value)?.apply()
        }

        fun putString(setting: String, value: String?) {
            prefs?.edit()?.putString(setting, value)?.apply()
        }

        fun getDefault(key: String): Any? {
            return prefDefault[key]
        }
    }
}