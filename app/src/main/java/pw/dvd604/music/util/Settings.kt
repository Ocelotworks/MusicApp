package pw.dvd604.music.util

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import androidx.preference.PreferenceManager

class Settings {
    companion object {
        private val prefKeys = hashMapOf(
            1 to "address",
            2 to "download",
            3 to "albumart",
            4 to "aggressiveReporting",
            5 to "usageReporting",
            6 to "crashReporting",
            7 to "storage",
            8 to "useIntents",
            9 to "shuffle",
            10 to "trackingID"
        )
        const val server: Int = 1
        const val offlineMusic: Int = 2
        const val offlineAlbum: Int = 3
        const val aggressiveReporting: Int = 4
        const val usageReports: Int = 5
        const val crashReports: Int = 6
        const val storage: Int = 7
        const val useIntents: Int = 8
        const val shuffle: Int = 9
        const val tracking: Int = 10

        private val prefDefault = hashMapOf(
            server to "https://unacceptableuse.com/petify",
            storage to "${Environment.getExternalStorageDirectory().path}/petify"
        )

        private var prefs: SharedPreferences? = null

        fun init(context: Context) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun getSetting(name: Int, defaultValue: String = ""): String? {
            return prefs?.getString(
                prefKeys[name], if (defaultValue == "") {
                    prefDefault[name]
                } else {
                    defaultValue
                }
            )
        }

        fun setSetting(name: Int, value: String) {
            prefs?.edit()?.putString(prefKeys[name], value)?.apply()
        }

        fun getBoolean(setting: Int, b: Boolean = false): Boolean {
            return prefs?.getBoolean(prefKeys[setting], b)!!
        }

        fun putBoolean(name: Int, value: Boolean) {
            prefs?.edit()?.putBoolean(prefKeys[name], value)?.apply()
        }

        fun putString(setting: Int, value: String?) {
            prefs?.edit()?.putString(prefKeys[setting], value)?.apply()
        }

        fun getDefault(key: Int): Any? {
            return prefDefault[key]
        }
    }
}