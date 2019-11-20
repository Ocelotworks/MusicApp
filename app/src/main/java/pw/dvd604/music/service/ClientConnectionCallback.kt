package pw.dvd604.music.service

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import pw.dvd604.music.MainActivity

class ClientConnectionCallback(private val activity: MainActivity) :
    MediaBrowserCompat.ConnectionCallback() {
    override fun onConnected() {

        // Get the token for the MediaSession
        activity.mediaBrowser.sessionToken.also { token ->

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(
                activity,
                token
            )

            // Save the controller
            MediaControllerCompat.setMediaController(
                activity,
                mediaController
            )
        }

        // Finish building the UI
        buildTransportControls()
    }

    private fun buildTransportControls() {
        MediaControllerCompat.getMediaController(activity)
            .registerCallback(activity.controllerCallback)
    }

    override fun onConnectionSuspended() {
        // The Service has crashed. Disable transport controls until it automatically reconnects
    }

    override fun onConnectionFailed() {
        // The Service has refused our connection
    }
}

class ControllerCallback(private val activity: MainActivity) : MediaControllerCompat.Callback() {

}