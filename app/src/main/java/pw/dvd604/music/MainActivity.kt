package pw.dvd604.music

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.dvd604.music.data.room.dao.BaseDao
import pw.dvd604.music.fragment.ListFragment
import pw.dvd604.music.fragment.ListLayout
import pw.dvd604.music.fragment.SettingsFragment
import pw.dvd604.music.service.ClientConnectionCallback
import pw.dvd604.music.service.ControllerCallback
import pw.dvd604.music.service.MediaPlaybackService
import pw.dvd604.music.util.ContentManager
import pw.dvd604.music.util.ControllerHandler

private const val NUM_PAGES = 4

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {

    private lateinit var mContentManager: ContentManager
    lateinit var mediaBrowser: MediaBrowserCompat
    val controllerCallback = ControllerCallback(this)
    val controllerHandler = ControllerHandler(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        mContentManager = ContentManager(this.applicationContext, this) {
            GlobalScope.launch(Dispatchers.Main) {
                try {
                    setupUI()
                } catch (e: Exception) {
                    Log.e("Test", "", e)
                }
            }
        }
        mContentManager.buildDatabase()
    }

    public override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
    }

    public override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    public override fun onStop() {
        super.onStop()
        // (see "stay in sync with the MediaSession")
        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }


    private fun setupUI() {
        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)
        sliding_layout.addPanelSlideListener(this)

        (tabDots as TabLayout).setupWithViewPager(pager)

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService::class.java),
            ClientConnectionCallback(this),
            null // optional Bundle
        )
    }

    override fun onPanelSlide(panel: View?, slideOffset: Float) {
        smallAlbumArt.alpha = 1 - slideOffset
        smallArtistText.alpha = 1 - slideOffset
        smallPausePlay.alpha = 1 - slideOffset
        smallSongTitle.alpha = 1 - slideOffset
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

    private fun getDao(position: Int): BaseDao<*> {
        return when (position) {
            0 -> {
                getApp().db.albumDao()
            }
            1 -> {
                getApp().db.artistDao()
            }
            2 -> {
                getApp().db.songDao()
            }
            else -> {
                getApp().db.songDao()
            }
        }
    }

    private fun getPagerLayout(position: Int): ListLayout {
        return when (position) {
            0 -> {
                ListLayout.GRID
            }
            1, 2 -> {
                ListLayout.LIST
            }
            else -> {
                ListLayout.GRID
            }
        }
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = NUM_PAGES

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
                    "Settings"
                }
                else -> {
                    "Unknown?"
                }
            }
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0, 1, 2 -> {
                    ListFragment(
                        getDao(position),
                        getPageTitle(position).toString(),
                        getPagerLayout(position),
                        getApp().db
                    )
                }
                3 -> {
                    SettingsFragment()
                }
                else -> {
                    SettingsFragment()
                }
            }
        }

    }
}
