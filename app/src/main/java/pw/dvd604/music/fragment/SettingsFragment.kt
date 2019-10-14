package pw.dvd604.music.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.download.HashService

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener {

    companion object {
        var enabled = false
        private var clicks = 0
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        this.findPreference<Preference>("downloadAll")?.onPreferenceClickListener = this
        this.findPreference<Preference>("checkHash")?.onPreferenceClickListener = this
        this.findPreference<Preference>("appCrash")?.onPreferenceClickListener = this
        this.findPreference<Preference>("downloadAlbumArt")?.onPreferenceClickListener = this
        this.findPreference<Preference>("version")?.apply {
            onPreferenceClickListener = this@SettingsFragment
            title = "Petify ${BuildConfig.VERSION_NAME}"
            summary = "Build: ${BuildConfig.VERSION_CODE}"
        }

        if (!(BuildConfig.DEBUG || enabled || Settings.getBoolean(Settings.developer))) {
            val experimental = this.findPreference<Preference>("experimentalCategory")
            this.preferenceScreen.removePreference(experimental)
        }

        setSeekBars(preferenceScreen)
    }

    private fun setSeekBars(group: PreferenceGroup) {
        for (i in 0 until group.preferenceCount) {
            val pref = group.getPreference(i)
            if (pref is SeekBarPreference) {
                pref.showSeekBarValue = true
            } else if (pref is PreferenceGroup) {
                setSeekBars(pref)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        preference?.summary = "${newValue as Int}"
        return true
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            "downloadAll" -> {
                if (Settings.getBoolean(Settings.offlineMusic)) {
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
            "checkHash" -> {
                Intent(this.context, HashService::class.java).also { intent ->
                    this.context?.startService(intent)
                }
                true
            }
            "appCrash" -> {
                throw Exception("Planned App Crash from ${this::class.java.name}")
            }
            "version" -> {
                clicks++
                if (clicks == 10) {
                    Util.report("Activated", this@SettingsFragment.activity as MainActivity, true)
                    enabled = true
                    Settings.putBoolean("developerOptions", true)
                }
                true
            }
            else -> false
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            Settings.blacklist -> {
                val value = sharedPreferences?.getString(key, "")
                value?.let {

                    val separator = System.getProperty("line.separator") ?: "\n"

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