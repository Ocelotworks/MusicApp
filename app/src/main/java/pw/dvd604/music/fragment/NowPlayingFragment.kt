package pw.dvd604.music.fragment

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import pw.dvd604.music.R
import pw.dvd604.music.fragment.helper.BitmapAsync


class NowPlayingFragment : Fragment() {

    private var tempPlayState : Boolean = false
    private var tempStarState : Boolean = false
    private var tempShuffleState : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_playing, container, false)

        BitmapAsync(this).execute("https://unacceptableuse.com/petify/album/7ad4c877-45d4-423c-8cf7-757f4969f02b")

        return v
    }

    fun changePausePlay(change : Boolean = true) {
        this.view?.let {
            val image: ImageView = it.findViewById(R.id.btnPause)
            if (!tempPlayState) {
                image.setImageResource(R.drawable.baseline_play_arrow_white_48)
            } else {
                image.setImageResource(R.drawable.baseline_pause_white_48)
            }
            if (!change) return
            tempPlayState = !tempPlayState
            return
        }
    }

    fun postImage(bmp : Bitmap?) {
        this.view?.findViewById<ImageView>(R.id.songArt)?.setImageBitmap(bmp)
    }

    fun nextSong() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun prevSong() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun starSong() {
        this.view?.let{
            val img : ImageView = it.findViewById(R.id.btnStar)
            val colour : Int = resources.getColor(R.color.colorAccent, null)
            if(!tempStarState) {
                img.setColorFilter(colour)
            } else {
                img.clearColorFilter()
            }
            tempStarState = !tempStarState
        }
    }

    fun toggleShuffle() {
        this.view?.let{
            val img : ImageView = it.findViewById(R.id.btnShuffle)
            val colour : Int = resources.getColor(R.color.colorAccent, null)
            if(!tempShuffleState) {
                img.setColorFilter(colour)
            } else {
                img.clearColorFilter()
            }
            tempShuffleState = !tempShuffleState
        }
    }

    fun hideStar() {
        this.view?.let{
            it.findViewById<ImageView>(R.id.btnStar).visibility = View.INVISIBLE
        }
    }

}