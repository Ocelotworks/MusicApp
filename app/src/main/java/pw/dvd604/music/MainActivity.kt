package pw.dvd604.music

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.data.room.dao.BaseDao
import pw.dvd604.music.fragment.ListFragment
import pw.dvd604.music.fragment.ListLayout
import pw.dvd604.music.util.ContentManager

private const val NUM_PAGES = 3

class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {

    private lateinit var mContentManager: ContentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        mContentManager = ContentManager(this.applicationContext, this)
        mContentManager.requestPermissions()
        mContentManager.buildDatabase()
    }

    private fun setupUI() {
        sliding_layout.addPanelSlideListener(this)
        pager.adapter = ScreenSlidePagerAdapter(supportFragmentManager)

        //this.supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, songListFragment)
        //    .commit()
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
                getApp().db.albumDao()
            }
        }
    }

    private fun getPagerTitle(position: Int): String {
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
            else -> {
                "Unknown?"
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

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment =
            ListFragment(
                getDao(position),
                getPagerTitle(position),
                getPagerLayout(position),
                getApp().db.artistSongJoinDao()
            )
    }
}
