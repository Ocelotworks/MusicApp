package pw.dvd604.music.util

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class Settings {
    companion object {
        const val offlineMusic = "download"
        const val shuffle = "shuffle"
        const val shuffleOffline = "shuffleOffline"
        const val developer = "developerOptions"
        const val autoSkip = "autoSkipOnDislike"
        const val tracking = "trackingID"
        const val api = "apiKey"

        private val prefDefault: HashMap<String, Any> = hashMapOf(
            offlineMusic to true,
            shuffleOffline to true,
            autoSkip to true,
            developer to false,
            autoSkip to true,
            tracking to "",
            api to ""
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