package pw.dvd604.music

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pw.dvd604.music.dialog.DialogHideListener
import pw.dvd604.music.util.ContentManager


class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener,
    DialogHideListener {
    override fun onHide() {
        GlobalScope.launch {
            val album = getApp().db.albumSongJoinDao()
                .getSongsForAlbum("000f7b4c-6b3c-4c36-948c-ac2ed7811dc9")

            album.forEach {
                Log.e("Test", it.title)
            }
        }
    }

    private lateinit var mContentManager: ContentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        mContentManager = ContentManager(this.applicationContext, this)
        mContentManager.buildDatabase()

    }

    private fun setupUI() {
        sliding_layout.addPanelSlideListener(this)
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

    private fun getApp(): MusicApplication {
        return this.application as MusicApplication
    }
}
