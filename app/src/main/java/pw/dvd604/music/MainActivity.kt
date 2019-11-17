package pw.dvd604.music

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.fragment.ListFragment
import pw.dvd604.music.util.ContentManager


class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {

    private lateinit var mContentManager: ContentManager
    private lateinit var songListFragment: ListFragment

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

        songListFragment = ListFragment(getApp().db.albumDao())

        this.supportFragmentManager.beginTransaction().add(R.id.fragmentContainer, songListFragment)
            .commit()
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
}
