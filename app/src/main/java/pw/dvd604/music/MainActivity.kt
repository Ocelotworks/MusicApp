package pw.dvd604.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.fragment.*
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Settings.Companion.aggressiveReporting
import pw.dvd604.music.util.Settings.Companion.server
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.download.Downloader
import pw.dvd604.music.util.update.Updater
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    var inSettings: Boolean = false
    private var nowPlayingFragment: NowPlayingFragment = NowPlayingFragment()
    var songFragment: SongFragment = SongFragment()
    private lateinit var subSongFragment: SubSongFragment
    private lateinit var detailFragment: SongDetailFragment
    private var menuItem: MenuItem? = null
    private val permissionsResult: Int = 1
    private var homeLab: Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Settings.getSetting(server)?.let { HTTP.setup(it) }
        Util.downloader = Downloader(this.applicationContext)

        //Insert actual fragments into shell containers
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()
        fT.add(R.id.fragmentContainer, nowPlayingFragment)
        fT.add(R.id.slideContainer, songFragment)
        fT.commit()

        //Check permissions
        checkPermissions()

        checkServerPrefs()
    }

    override fun onStart() {
        super.onStart()

        startService(Intent(this, MediaService::class.java))

        if (Settings.getBoolean(Settings.update)) {
            Updater(this).checkUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mediaController.transportControls.stop()
    }

    private fun checkServerPrefs() {
        if (Settings.getSetting(server) == Settings.getDefault(server)) {
            //We're connecting to petify
            homeLab = false
        } else {
            //We're on a home lab, disable all advanced functions
            homeLab = true
            nowPlayingFragment.hideStar()
        }
    }

    fun onClick(v: View) {
        MusicApplication.track(
            "Button Click",
            Util.generatePayload(arrayOf("button"), arrayOf(Util.idToString(v.id)))
        )
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
                if (pbState == PlaybackStateCompat.STATE_PAUSED or PlaybackStateCompat.STATE_PLAYING) {
                    if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                        mediaController.transportControls.pause()
                    } else {
                        mediaController.transportControls.play()
                    }
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
                val pbState = this.mediaController.playbackState?.state
                if (pbState == PlaybackStateCompat.STATE_PLAYING or PlaybackStateCompat.STATE_PAUSED) {
                    mediaController.transportControls.skipToPrevious()
                }
                return
            }
            R.id.btnStar -> {
                if (!homeLab) {
                    mediaController.sendCommand("likesong", null, null)
                } else {
                    report(getString(R.string.homelabError), true)
                }
                return
            }
            R.id.btnShuffle -> {
                nowPlayingFragment.shuffleMode()
                return
            }
            R.id.btnBack -> {
                val fM = this.supportFragmentManager
                fM.popBackStack()
                //fT.commit()
            }
        }
    }

    fun createSubFragment(url: String, name: String) {
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()

        fM.saveFragmentInstanceState(songFragment)

        subSongFragment = SubSongFragment.create(url, name)
        fT.replace(R.id.slideContainer, subSongFragment)
        fT.addToBackStack(null)
        fT.commit()
    }

    fun createDetailFragment(song: Song) {
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()

        fM.saveFragmentInstanceState(songFragment)

        detailFragment = SongDetailFragment.create(song)
        fT.replace(R.id.slideContainer, detailFragment)
        fT.addToBackStack("subSong")
        fT.commit()
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
        when {
            inSettings -> {
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

                report("Settings changes require an app restart to take effect", true)
            }
            else -> //Else let the system deal with the back button
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

        val check: Boolean = check(permissions)

        if (!check) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                permissionsResult
            )
        }
    }

    private fun check(perms: Array<String>): Boolean {
        var result = true
        for (perm in perms) {
            if (ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_DENIED)
                result = false
        }
        return result
    }

    fun report(text: String, urgent: Boolean = false) {
        if (Settings.getBoolean(aggressiveReporting) || urgent) {
            Snackbar.make(
                this.findViewById(R.id.fragmentContainer),
                text as CharSequence,
                Snackbar.LENGTH_SHORT
            )
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
                            report("This app will not work without permissions", false)
                            exitProcess(-1)
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

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {
        super.onCreateContextMenu(songFragment.buildContext(menu, v, menuInfo), v, menuInfo)
    }

    override fun onContextItemSelected(item: MenuItem?): Boolean {
        return songFragment.onContextItemSelected(item)
    }

}
