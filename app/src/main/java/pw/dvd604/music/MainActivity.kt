package pw.dvd604.music

import android.Manifest
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
import android.content.SharedPreferences
import android.support.design.widget.Snackbar
import android.support.v7.preference.PreferenceManager


class MainActivity : AppCompatActivity() {

    var inSettings: Boolean = false
    private var nowPlayingFragment: NowPlayingFragment = NowPlayingFragment()
    private var songFragment: SongFragment = SongFragment()
    private var menuItem: MenuItem? = null
    private val wes: Int = 1
    private val res: Int = 2


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
    private var prefs: SharedPreferences? = null
    private var homeLab: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        //Insert actual fragments into shell containers
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()
        songFragment.address = prefs?.getString(prefKeys[server],"")
        fT.add(R.id.fragmentContainer, nowPlayingFragment)
        fT.add(R.id.slideContainer, songFragment)
        fT.commit()

        //Check permissions
        checkPermissions()

        checkServerPrefs()
    }

    private fun checkServerPrefs() {
        if (prefs?.getString(prefKeys[server], "") == "https://unacceptableuse.com/petify") {
            //We're connecting to petify
        } else {
            //We're on a home lab, disable all advanced functions
            homeLab = true
            nowPlayingFragment.hideStar()
        }
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnTitle -> {
                songFragment.changeTextColour(v.id)
                songFragment.searchMode = v.id
                return
            }
            R.id.btnAlbum -> {
                songFragment.changeTextColour(v.id)
                songFragment.searchMode = v.id
                return
            }
            R.id.btnGenre -> {
                songFragment.changeTextColour(v.id)
                songFragment.searchMode = v.id
                return
            }
            R.id.btnArtist -> {
                songFragment.changeTextColour(v.id)
                songFragment.searchMode = v.id
                return
            }

            R.id.btnPause -> {
                nowPlayingFragment.changePausePlay()
                return
            }
            R.id.btnNext -> {
                nowPlayingFragment.nextSong()
                return
            }
            R.id.btnPrev -> {
                nowPlayingFragment.prevSong()
                return
            }
            R.id.btnStar -> {
                nowPlayingFragment.starSong()
                return
            }
            R.id.btnShuffle -> {
                nowPlayingFragment.toggleShuffle()
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                wes
            )
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                wes
            )
        }

    }

    fun report(text: String) {
        prefs?.let {
            if (it.getBoolean(prefKeys[aggressiveReporting], true)) {
                Snackbar.make(this.findViewById(R.id.fragmentContainer), text as CharSequence, Snackbar.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        //Get request result - keep asking until we get the required permissions
        when (requestCode) {
            wes -> {
                if (!((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                    checkPermissions()
                }
                return
            }
            res -> {
                if (!((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))) {
                    checkPermissions()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }


}
