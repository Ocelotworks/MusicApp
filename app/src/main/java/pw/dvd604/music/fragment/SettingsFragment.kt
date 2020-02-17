package pw.dvd604.music.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pw.dvd604.music.R
import pw.dvd604.music.service.downloader.DownloadService
import pw.dvd604.music.service.downloader.HashService

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.findPreference<Preference>("download")?.onPreferenceClickListener = this
        this.findPreference<Preference>("hash")?.onPreferenceClickListener = this
    }

    override fun onPreferenceClick(preference: Preference?): Boolean {
        return when (preference?.key) {
            "download" -> {
                Intent(this.context, DownloadService::class.java).also { intent ->
                    this.context?.startService(intent)
                }
                true
            }
            "hash" -> {
                Intent(this.context, HashService::class.java).also { intent ->
                    this.context?.startService(intent)
                }
                true
            }
            else -> false
        }
    }

}