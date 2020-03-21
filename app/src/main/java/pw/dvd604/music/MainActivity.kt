package pw.dvd604.music

import android.content.ComponentName
import android.media.Rating
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.CardData
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.fragment.ListFragment
import pw.dvd604.music.fragment.ListLayout
import pw.dvd604.music.fragment.SettingsFragment
import pw.dvd604.music.service.ClientConnectionCallback
import pw.dvd604.music.service.ControllerCallback
import pw.dvd604.music.service.MediaPlaybackService
import pw.dvd604.music.ui.OpinionButtonController
import pw.dvd604.music.util.ContentManager
import pw.dvd604.music.util.ControllerHandler
import pw.dvd604.music.util.Settings

private const val NUM_PAGES = 5

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {

    lateinit var mContentManager: ContentManager
    lateinit var mediaBrowser: MediaBrowserCompat
    val controllerCallback = ControllerCallback(this)
    val controllerHandler = ControllerHandler(this)
    lateinit var opinionController: OpinionButtonController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI(true)
        mContentManager = ContentManager(this.applicationContext, this) {
            setupUI()
        }
        mContentManager.requestPermissions()
        mContentManager.buildDatabase()
    }

    public override fun onStart() {
        super.onStart()
        if (!mediaBrowser.isConnected) {
            mediaBrowser.connect()
        }
    }

    public override fun onResume() {
        super.onResume()
        //volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        //(see "stay in sync with the MediaSession")
        this.mediaController.transportControls.stop()
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    private fun setupUI(setupMediaBrowser: Boolean = false) {
        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        sliding_layout.addPanelSlideListener(this)
        opinionController =
            OpinionButtonController(btnLike, btnNeutral, btnDislike, ::onOpinionChange, 250)
        opinionController.openable = false

        (tabDots as TabLayout).setupWithViewPager(pager)

        if (setupMediaBrowser) {
            mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, MediaPlaybackService::class.java),
                ClientConnectionCallback(this),
                null // optional Bundle
            )
        }
    }

    private fun onOpinionChange(state: Int) {
        when (state) {
            1 -> {
                val rating = Rating.newThumbRating(true)
                this.mediaController.transportControls.setRating(rating)
            }
            0 -> {
                this.mediaController.transportControls.setRating(Rating.newUnratedRating(Rating.RATING_THUMB_UP_DOWN))
            }
            -1 -> {
                if (Settings.getBoolean(Settings.autoSkip)) {
                    this.mediaController.transportControls.skipToNext()
                    opinionController.resetState()
                }

                val rating = Rating.newThumbRating(false)
                this.mediaController.transportControls.setRating(rating)
            }
        }
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        smallAlbumArt.alpha = 1 - slideOffset
        smallArtistText.alpha = 1 - slideOffset
        smallPausePlay.alpha = 1 - slideOffset
        smallSongTitle.alpha = 1 - slideOffset
        songArt.alpha = slideOffset
    }

    override fun onPanelStateChanged(
        panel: View?,
        previousState: SlidingUpPanelLayout.PanelState?,
        newState: SlidingUpPanelLayout.PanelState?
    ) {
    }

    fun getApp(): MusicApplication {
        return this.application as MusicApplication
    }

    fun onClick(v: View) = when (v.id) {
        this.btnNext.id -> {
            this.mediaController.transportControls.skipToNext()
        }
        this.smallPausePlay.id,
        this.btnPause.id -> {
            if (this.mediaController.playbackState?.state == PlaybackStateCompat.STATE_PAUSED) {
                smallPausePlay.setImageResource(R.drawable.baseline_pause_white_48)
                btnPause.setImageResource(R.drawable.baseline_pause_white_48)
                this.mediaController.transportControls.play()
            } else {
                this.mediaController.transportControls.pause()
                smallPausePlay.setImageResource(R.drawable.baseline_play_arrow_white_48)
                btnPause.setImageResource(R.drawable.baseline_play_arrow_white_48)
            }
        }
        this.btnShuffle.id -> {
        }
        this.btnStar.id -> {

        }
        this.btnPrev.id -> {
            this.mediaController.transportControls.skipToPrevious()
        }
        else -> {
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = NUM_PAGES
        var mCurrentFragment: Fragment? = null

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            if (mCurrentFragment != obj) {
                mCurrentFragment = obj as Fragment
            }
            super.setPrimaryItem(container, position, obj)
        }

        private fun getPagerLayout(position: Int): ListLayout {
            return when (position) {
                0 -> {
                    ListLayout.GRID
                }
                1, 2, 3 -> {
                    ListLayout.LIST
                }
                else -> {
                    ListLayout.LIST
                }
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> {
                    "Albums"
                }
                1 -> {
                    "Artists"
                }
                2 -> {
                    "Songs"
                }
                3 -> {
                    "Playlists"
                }
                4 -> {
                    "Settings"
                }
                else -> {
                    "Unknown?"
                }
            }
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0, 1, 2, 3, 5 -> {
                    ListFragment(
                        getPagerLayout(position),
                        getDataCallback(position),
                        getOnClickAction(position)
                    )
                }
                4 -> {
                    SettingsFragment()
                }
                else -> {
                    SettingsFragment()
                }
            }
        }

        private fun getDataCallback(position: Int): (() -> ArrayList<CardData>)? {
            return when (position) {
                0 -> {
                    // "Albums"
                    ::getAlbumData
                }
                1 -> {
                    //  "Artists"
                    ::getArtistData
                }
                2 -> {
                    //"Songs"
                    ::getSongData
                }
                3 -> {
                    //"Playlist"
                    ::getPlaylistData
                }
                else -> {
                    ::createListData
                }
            }
        }

    }

    private fun getOnClickAction(position: Int): ((id: String) -> Unit)? {
        return when (position) {
            0, 1, 3 -> {
                {
                    val adapter = pager.adapter as ScreenSlidePagerAdapter
                    val fragment: ListFragment = adapter.mCurrentFragment as ListFragment
                    if (!fragment.isInSub) {
                        Log.e("Test", "Yes")
                        fragment.expandData(it)
                    } else {
                        controllerHandler.play(it)
                    }
                }
            }
            2 -> {
                { controllerHandler.play(it) }
            }
            else -> {
                {}
            }
        }
    }

    private fun getPlaylistData(): ArrayList<CardData> {
        return mContentManager.getPlaylists()
    }

    private fun getSongData(): ArrayList<CardData> {
        return mContentManager.getSongsWithArtists()
    }

    private fun getArtistData(): ArrayList<CardData> {
        return Artist.cursorToArray(
            getApp().readableDatabase.query(
                DatabaseContract.Artist.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "${DatabaseContract.Artist.COLUMN_NAME_NAME} ASC"
            )
        )
    }

    private fun getAlbumData(): ArrayList<CardData> {
        return Album.cursorToArray(
            getApp().readableDatabase.query(
                DatabaseContract.Album.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                "${DatabaseContract.Album.COLUMN_NAME_NAME} ASC"
            )
        ) as ArrayList<CardData>
    }

    private fun createListData(): ArrayList<CardData> {
        return ArrayList<CardData>(0)
    }

    override fun onBackPressed() {
        val adapter = pager.adapter as ScreenSlidePagerAdapter
        if ((adapter.mCurrentFragment as ListFragment).onBackPressed()) {
            super.onBackPressed()
        }
    }
}
