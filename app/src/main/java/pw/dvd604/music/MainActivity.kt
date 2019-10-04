package pw.dvd604.music

import android.Manifest
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_songs.*
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.fragment.*
import pw.dvd604.music.util.Settings
import pw.dvd604.music.util.Settings.Companion.aggressiveReporting
import pw.dvd604.music.util.Settings.Companion.server
import pw.dvd604.music.util.SongList
import pw.dvd604.music.util.Util
import pw.dvd604.music.util.download.Downloader
import pw.dvd604.music.util.network.FilterMapRequest
import pw.dvd604.music.util.network.HTTP
import pw.dvd604.music.util.network.SongListRequest
import pw.dvd604.music.util.update.Updater
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {

    private var inSettings: Boolean = false
    private var inQueue: Boolean = false
    private var nowPlayingFragment: NowPlayingFragment = NowPlayingFragment()
    var songFragment: SongFragment = SongFragment()
    private lateinit var subSongFragment: SubSongFragment
    private lateinit var detailFragment: SongDetailFragment
    private var settingsFragment = SettingsFragment()
    private var queueFragment = QueueFragment()
    private val permissionsResult: Int = 1
    private var homeLab: Boolean = false
    private lateinit var http: HTTP


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Settings.getSetting(server).let { HTTP.setup(it) }
        Util.downloader = Downloader(this.applicationContext)

        //Insert actual fragments into shell containers
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()
        fT.add(R.id.fragmentContainer, nowPlayingFragment)
        fT.add(R.id.slideContainer, songFragment)
        fT.commit()

        http = HTTP(this)

        SongList.callback = songFragment::setSongs

        //Check permissions
        checkPermissions()

        checkServerPrefs()

        Thread {
            populateSongList()
            populateFilterMaps()

            try {
                settingsFragment.onSharedPreferenceChanged(Settings.prefs, Settings.blacklist)
            } catch (e: Exception) {
            }
        }.start()
    }

    override fun onStart() {
        super.onStart()

        sliding_layout.setScrollableView(mediaList)
        if (Settings.getBoolean(Settings.update)) {
            Updater(this).checkUpdate()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mediaController?.transportControls?.stop()
    }

    private fun populateFilterMaps() {
        for (type in MediaType.getNonSong()) {
            val fileContents = Util.readFromFile(this, "${Util.dataTypeToString(type)}List")

            if (fileContents != null) {
                FilterMapRequest(::setFilter, type).onResponse(fileContents)
            }

            http.getReq(HTTP.getAllMedia(type), FilterMapRequest(::setFilter, type, ::writeFilter))
        }
    }

    private fun writeFilter(response: String, mediaType: MediaType) {
        Util.writeToFile(this, "${Util.dataTypeToString(mediaType)}List", response)
    }

    private fun setFilter(arrayList: ArrayList<Media>, mediaType: MediaType, broken: Boolean) {
        if (!broken) {
            SongList.generateMaps(arrayList)
        } else {
            Util.deleteFile(this, "${Util.dataTypeToString(mediaType)}List")
        }

        SongList.applyFilter()
    }

    private fun populateSongList() {
        SongList.isBuilding = true
        val fileContents = Util.readFromFile(this, "mediaList")

        if (fileContents != null) {
            SongListRequest(::setSongs).onResponse(fileContents)
        }

        http.getReq(
            HTTP.getSong(),
            SongListRequest(::setSongs, ::writeSongs)
        )
    }

    private fun writeSongs(response: String?) {
        Util.writeToFile(this, "mediaList", response!!)
    }

    private fun setSongs(media: ArrayList<Media>) {
        SongList.setSongsAndNotify(media)
    }

    private fun checkServerPrefs() {
        if (Settings.getSetting(server) == Settings.getDefault(server)) {
            //We're connecting to petify - chances of this not being the case are slim
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
        if (this.mediaController == null) {
            this.report("An error occurred, please try again")
            MusicApplication.track("error", "MediaController is null?")
            return
        }

        val pbState = this.mediaController.playbackState?.state

        when (v.id) {
            R.id.btnQueue -> {

                return
            }
            R.id.btnPause -> {
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
                if (pbState == PlaybackStateCompat.STATE_PLAYING or PlaybackStateCompat.STATE_PAUSED) {
                    mediaController.transportControls.skipToNext()
                } else {
                    mediaController.transportControls.prepareFromUri(
                        SongList.songList.random().toUri(),
                        null
                    )
                }
                return
            }
            R.id.btnPrev -> {
                if (pbState == PlaybackStateCompat.STATE_PLAYING or PlaybackStateCompat.STATE_PAUSED) {
                    mediaController.transportControls.skipToPrevious()
                }
                return
            }
            R.id.btnStar -> {
                if (!homeLab) {
                    mediaController.sendCommand("likesong", null, null)
                    Util.report("Liked media!", this, true)
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

    fun createDetailFragment(media: Media) {
        val fM = this.supportFragmentManager
        val fT = fM.beginTransaction()

        fM.saveFragmentInstanceState(songFragment)

        detailFragment = SongDetailFragment.create(media)
        fT.replace(R.id.slideContainer, detailFragment)
        fT.addToBackStack("subSong")
        fT.commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //Create our options menu
        menuInflater.inflate(R.menu.menu_bar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (inSettings || inQueue) {
            return super.onOptionsItemSelected(item)
        }

        return when (item.itemId) {
            R.id.actionSettings -> {
                // User chose the "Settings" item, show the app settings UI...
                //By replacing the now playing fragment with the settings fragment
                //and hiding the menu item
                val fM = this.supportFragmentManager
                val fT = fM.beginTransaction()
                fT.replace(R.id.fragmentContainer, settingsFragment)
                fT.commit()
                inSettings = true

                supportActionBar?.let {
                    it.title = "Settings"
                }
                true
            }
            R.id.actionQueue -> {
                val fM = this.supportFragmentManager
                val fT = fM.beginTransaction()
                fT.replace(R.id.fragmentContainer, queueFragment)
                fT.commit()
                inQueue = true

                supportActionBar?.let {
                    it.title = "Song Queue"
                }
                true
            }
            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                super.onOptionsItemSelected(item)
            }
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

                supportActionBar?.let {
                    it.title = resources.getString(R.string.app_name)
                }

                report("Settings changes require an app restart to take effect", true)
            }
            inQueue -> {
                val fM = this.supportFragmentManager
                val fT = fM.beginTransaction()

                fT.replace(R.id.fragmentContainer, nowPlayingFragment)
                fT.commit()
                inQueue = false

                supportActionBar?.let {
                    it.title = resources.getString(R.string.app_name)
                }
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
