package pw.dvd604.music.fragment

import android.content.Intent
import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.service.downloader.DownloadService
import pw.dvd604.music.service.downloader.HashService
import pw.dvd604.music.util.Settings

class SettingsFragment : PreferenceFragmentCompat(), Preference.OnPreferenceClickListener,
    Preference.OnPreferenceChangeListener {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.findPreference<Preference>("download")?.onPreferenceClickListener = this
        this.findPreference<Preference>("hash")?.onPreferenceClickListener = this
        this.findPreference<Preference>("rebuild")?.onPreferenceClickListener = this
        this.findPreference<Preference>("apiKey")?.onPreferenceChangeListener = this

        if (!Settings.getBoolean(Settings.developer)) {
            this.preferenceScreen.removePreference(this.findPreference("trackingID"))
            this.preferenceScreen.removePreference(this.findPreference("randomSql"))
        } else {
            this.preferenceScreen.findPreference<EditTextPreference>("randomSql")?.text =
                Settings.getSetting(Settings.sql)
        }

        this.preferenceScreen.findPreference<Preference>("version")?.title =
            "Petify ${BuildConfig.VERSION_NAME}"
        this.preferenceScreen.findPreference<Preference>("version")?.summary =
            "Build Number: ${BuildConfig.VERSION_CODE}"
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
            "rebuild" -> {
                (this.activity as MainActivity).getApp().database.delete(
                    DatabaseContract.Song.TABLE_NAME,
                    null,
                    null
                )
                (this.activity as MainActivity).mContentManager.buildDatabase()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
        (this.activity as MainActivity).getApp().database.delete(
            DatabaseContract.Song.TABLE_NAME,
            null,
            null
        )
        (this.activity as MainActivity).mContentManager.buildDatabase()
        return true
    }

}