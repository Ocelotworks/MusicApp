package pw.dvd604.music.util

import pw.dvd604.music.MainActivity

class ControllerHandler(private val activity: MainActivity) {
    fun play() {
        activity.mediaController.transportControls.play()
    }

    fun play(id: String) {
        activity.mediaController.transportControls.playFromMediaId(id, null)
    }
}