package pw.dvd604.music.util

import android.content.Context
import android.content.SharedPreferences
import android.support.v7.preference.PreferenceManager

class Settings {
    companion object{
        private val prefKeys = hashMapOf(
            1 to "address",
            2 to "download",
            3 to "albumart",
            4 to "aggressiveReporting",
            5 to "usageReporting",
            6 to "crashReporting",
            7 to "storage",
            8 to "useIntents",
            9 to "shuffle"
        )
        val server: Int = 1
        val offlineMusic: Int = 2
        val offlineAlbum: Int = 3
        val aggressiveReporting: Int = 4
        val usageReports: Int = 5
        val crashReports: Int = 6
        val storage: Int = 7
        val useIntents: Int = 8
        val shuffle : Int = 9

        private var prefs : SharedPreferences? = null

        fun init(context : Context){
            prefs = PreferenceManager.getDefaultSharedPreferences(context)
        }

        fun getSetting(name : Int, defaultValue : String = "") : String?{
            return prefs?.getString(prefKeys[name], defaultValue)
        }

        fun setSetting(name : Int, value : String){
            prefs?.edit()?.putString(prefKeys[name], value)?.apply()
        }

        fun getBoolean(setting: Int, b: Boolean = false): Boolean {
            return prefs?.getBoolean(prefKeys[setting], b)!!
        }

        fun putBoolean(name: Int, value: Boolean) {
            prefs?.edit()?.putBoolean(prefKeys[name], value)?.apply()
        }
    }
}