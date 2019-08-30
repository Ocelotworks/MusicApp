package pw.dvd604.music.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.Util

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        this.findPreference("downloadAll").onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return if (Settings.getBoolean(Settings.offlineMusic)) {
            (this.activity as MainActivity).songFragment.downloadAll()
            true
        } else {
            Util.report(
                "Cannot download music without offline music enabled",
                this.activity as MainActivity,
                true
            )
            false
        }

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.blacklist -> {
                val value = sharedPreferences?.getString(key, "")
                value?.let {

                    val separator = if (System.getProperty("line.separator") == null) {
                        "\n"
                    } else {
                        System.getProperty("line.separator")
                    }

                    val entryArray = value.split(separator)

                    for (entry in entryArray) {
                        try {
                            val splitKey = entry.split(':')
                            val wholeKey = splitKey.subList(1, splitKey.size).joinToString()
                            val wholeValue = splitKey[0]

                            if (sanityCheck(wholeKey, wholeValue)) {
                                throw Exception()
                            }

                            val sdtValue: MediaType = Util.stringToDataType(wholeValue)

                            SongList.filterMap[wholeKey] = sdtValue

                            Util.log(this, "$wholeValue:$wholeKey")
                        } catch (e: Exception) {
                            Util.report(
                                "Invalid blacklist item",
                                this.activity as MainActivity,
                                true
                            )
                        }
                    }
                    SongList.applyFilter()
                }
            }
        }
    }

    private fun sanityCheck(vararg strings: String): Boolean {
        for (s in strings) {
            if (s == "") { //TODO: Add more strings here
                return true
            }
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

    }

    override fun onPause() {
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }
}