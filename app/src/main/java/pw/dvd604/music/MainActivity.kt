package pw.dvd604.music

import android.content.ComponentName
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
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
            OpinionButtonController(btnLike, btnNeutral, btnDislike, ::onOpinionChange, 500)
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

            }
            0 -> {

            }
            -1 -> {
                if (Settings.getBoolean(Settings.autoSkip)) {
                    this.mediaController.transportControls.skipToNext()
                }
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
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = NUM_PAGES

        private fun getPagerLayout(position: Int): ListLayout {
            return when (position) {
                0 -> {
                    ListLayout.GRID
                }
                1, 2, 3 -> {
                    ListLayout.LIST
                }
                else -> {
                    ListLayout.GRID
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
                    "Playlist"
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
                0, 1, 2, 3 -> {
                    ListFragment(
                        getPageTitle(position).toString(),
                        getPagerLayout(position)
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

    }
}
