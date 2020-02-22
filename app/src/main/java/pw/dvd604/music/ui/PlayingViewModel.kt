package pw.dvd604.music.ui

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlayingViewModel : ViewModel() {
    var song = MutableLiveData<String>()
    var artist = MutableLiveData<String>()
    var art = MutableLiveData<Bitmap>()
    var maxProgress = MutableLiveData<Int>()
    var currentProgress = MutableLiveData<Int>()
}

