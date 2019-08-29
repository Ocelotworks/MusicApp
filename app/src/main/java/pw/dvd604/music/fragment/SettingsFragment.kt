package pw.dvd604.music.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Util

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
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
}