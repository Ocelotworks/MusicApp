package pw.dvd604.music

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_playing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import pw.dvd604.music.data.Song
import pw.dvd604.music.dialog.ViewDialog
import pw.dvd604.music.util.HTTP


class MainActivity : AppCompatActivity(), SlidingUpPanelLayout.PanelSlideListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sliding_layout.addPanelSlideListener(this)

        GlobalScope.launch {
            if (getApp().db.songDao().count() == 0) {
                val dialog = ViewDialog(this@MainActivity)
                dialog.showDialog()

                HTTP(this@MainActivity).getReq(
                    "http://unacceptableuse.com:3000/api/v2/song",
                    Response.Listener { res ->

                        Log.e("Build", "Started")
                        val array = JSONArray(res)
                        val songList = ArrayList<Song>()

                        for (i in 0 until array.length()) {
                            val songObject = Song.parse(array.getJSONObject(i))
                            Log.e("Build", "At $i")

                            songList.add(songObject)
                        }

                        Log.e("Build", "finished")

                        getApp().db.songDao().insertAll(*songList.toTypedArray())
                        dialog.hideDialog()
                    })
            } else {
                Log.e("Build", "Have ${getApp().db.songDao().count()} songs in db")
            }
        }
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
