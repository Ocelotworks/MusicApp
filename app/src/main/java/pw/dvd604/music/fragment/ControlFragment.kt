package pw.dvd604.music.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_playing.*
import pw.dvd604.music.MainActivity
import pw.dvd604.music.R

class ControlFragment : Fragment(), SeekBar.OnSeekBarChangeListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_playing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        songProgress.setOnSeekBarChangeListener(this)
    }

    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {}

    override fun onStartTrackingTouch(p0: SeekBar?) {
        (this.activity as MainActivity).controllerCallback.stopAnimation()
    }

    override fun onStopTrackingTouch(seekbar: SeekBar?) {
        (this.activity as MainActivity).mediaController.transportControls.seekTo(seekbar?.progress!!.toLong())
    }

}