package pw.dvd604.music.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.android.volley.Response
import kotlinx.android.synthetic.main.fragment_playing.*
import org.json.JSONObject
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.BitmapAsync
import pw.dvd604.music.util.HTTP
import pw.dvd604.music.util.MediaController
import pw.dvd604.music.util.Util


class NowPlayingFragment : Fragment() {

    private var tempPlayState : Boolean = false
    private var tempStarState : Boolean = false
    private var tempShuffleState : Boolean = false
    private var http : HTTP? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_playing, container, false)
        http = HTTP(context)
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

    fun setSong(song: Song) {
        BitmapAsync(this).execute("https://unacceptableuse.com/petify/album/" + song.album)
        songName.text = song.name
        songAuthor.text = song.author
        http?.getReq(HTTP.songInfo(song.id), SongInfoListener(this))


        val serviceIntent = Intent(this.context, MediaController::class.java)
        serviceIntent.action = MediaController.playIntentCode
        serviceIntent.putExtra("url", Util.songToUrl(song))
        serviceIntent.putExtra("song", song)
        ContextCompat.startForegroundService(this.context!!, serviceIntent)
    }

    class SongInfoListener(private val nowPlayingFragment: NowPlayingFragment) : Response.Listener<String> {
        override fun onResponse(response: String?) {
            val json = JSONObject(response)

            nowPlayingFragment.songDuration.text = Util.prettyTime(json.getInt("duration"))
            nowPlayingFragment.songProgress.max = json.getInt("duration")
            nowPlayingFragment.songProgress.progress = 0
        }
    }

}