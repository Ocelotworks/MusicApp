package pw.dvd604.music.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import pw.dvd604.music.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}