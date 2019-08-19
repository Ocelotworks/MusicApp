package pw.dvd604.music

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import pw.dvd604.music.fragment.NowPlayingFragment
import pw.dvd604.music.fragment.SettingsFragment
import pw.dvd604.music.fragment.SongFragment
import android.os.IBinder
import android.support.design.widget.Snackbar
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v7.preference.PreferenceManager
import com.mixpanel.android.mpmetrics.MixpanelAPI
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.Util


class MainActivity : AppCompatActivity() {

    var inSettings: Boolean = false
    private var nowPlayingFragment: NowPlayingFragment = NowPlayingFragment()
    private var songFragment: SongFragment = SongFragment()
    private var menuItem: MenuItem? = null
    private val permissionsResult: Int = 1
    private var tracking: MixpanelAPI? = null

    private val prefKeys = hashMapOf(
        1 to "address",
        2 to "download",
        3 to "albumart",
        4 to "aggressiveReporting",
        5 to "usageReporting",
        6 to "crashReporting",
        7 to "storage",
        8 to "useIntents"
    )
    private val server: Int = 1
    private val offlineMusic: Int = 2
    private val offlineAlbum: Int = 3
    private val aggressiveReporting: Int = 4
    private val usageReports: Int = 5
    private val crashReports: Int = 6
    private val storage: Int = 7
    private val useIntents: Int = 8
    private lateinit var prefs: SharedPreferences
    private var homeLab: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.getString(prefKeys[server], "https://unacceptableuse.com/petify")?.let {
            HTTP.setup(it)
        }

        //Insert actual fragments into shell containers
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()
        fT.add(R.id.fragmentContainer, nowPlayingFragment)
        fT.add(R.id.slideContainer, songFragment)
        fT.commit()

        //Check permissions
        checkPermissions()

        checkServerPrefs()

        startService(Intent(this, MediaService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mediaController.transportControls.stop()
    }

    fun startTracking() {
        tracking = (application as MusicApplication).mixpanel

        if (!prefs.getBoolean(prefKeys[usageReports], false)) {
            tracking?.optOutTracking()
        } else {
            tracking?.optInTracking()
        }
    }

    private fun checkServerPrefs() {
        if (prefs.getString(prefKeys[server], "") == "https://unacceptableuse.com/petify") {
            //We're connecting to petify
        } else {
            //We're on a home lab, disable all advanced functions
            homeLab = true
            //nowPlayingFragment.hideStar()
        }
    }

    fun onClick(v: View) {
        if (tracking != null) {
            tracking?.track("${Util.idToString(v.id)} Click")
        }

        when (v.id) {
            R.id.btnTitle,
            R.id.btnAlbum,
            R.id.btnGenre,
            R.id.btnArtist -> {
                songFragment.changeTextColour(v.id)
                songFragment.searchMode = v.id
                return
            }

            R.id.btnPause -> {
                val pbState = this.mediaController.playbackState?.state
                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                } else {
                    mediaController.transportControls.play()
                }
                return
            }
            R.id.btnNext -> {
                val pbState = this.mediaController.playbackState?.state
                if (pbState == PlaybackStateCompat.STATE_PLAYING or PlaybackStateCompat.STATE_PAUSED) {
                    mediaController.transportControls.skipToNext()
                }
                return
            }
            R.id.btnPrev -> {
                return
            }
            R.id.btnStar -> {
                return
            }
            R.id.btnShuffle -> {
                mediaController.sendCommand("", null, null)
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Create our options menu
        menuInflater.inflate(R.menu.menu_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.actionSettings -> {
            // User chose the "Settings" item, show the app settings UI...
            //By replacing the now playing fragment with the settings fragment
            //and hiding the menu item
            val fM = this.supportFragmentManager
            val fT = fM.beginTransaction()
            fT.replace(R.id.fragmentContainer, SettingsFragment())
            fT.commit()
            inSettings = true
            item.isVisible = false
            menuItem = item

            supportActionBar?.let {
                it.title = "Settings"
            }
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (inSettings) {
            //If we're currently looking at the settings, replace the fragment back to the old now playing fragment
            // and un-hide the menu item
            val fM = this.supportFragmentManager
            val fT = fM.beginTransaction()

            fT.replace(R.id.fragmentContainer, nowPlayingFragment)
            fT.commit()
            inSettings = false
            menuItem?.let {
                it.isVisible = true
            }
            supportActionBar?.let {
                it.title = resources.getString(R.string.app_name)
            }
        } else {
            //Else let the system deal with the back button
            super.onBackPressed()
        }
    }

    private fun checkPermissions() {
        //Check we have both storage permissions
        //Request them if we don't
        val permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        val check: Boolean = check(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (!check) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                permissionsResult
            )
        }
    }

    private fun check(vararg perms: String): Boolean {
        var result = true
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_DENIED)
                result = false
        }
        return result
    }

    fun report(text: String) {
        if (prefs.getBoolean(prefKeys[aggressiveReporting], true)) {
            Snackbar.make(this.findViewById(R.id.fragmentContainer), text as CharSequence, Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        //Get request result
        when (requestCode) {
            permissionsResult -> {
                if (grantResults.isNotEmpty()) {
                    for (perm in grantResults) {
                        if (perm != PackageManager.PERMISSION_GRANTED) {
                            report("This app will not work without permissions")
                            return
                        }
                    }
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
