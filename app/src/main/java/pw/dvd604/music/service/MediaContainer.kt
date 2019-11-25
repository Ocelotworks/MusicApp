package pw.dvd604.music.service

import android.media.MediaPlayer
import android.net.Uri
import pw.dvd604.music.data.ArtistSong
import pw.dvd604.music.data.Song

class MediaContainer(val service: MediaPlaybackService) : MediaPlayer.OnErrorListener,
    MediaPlayer.OnPreparedListener,
    MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener {
    companion object {
        lateinit var player: MediaPlayer
    }

    init {
        player = MediaPlayer().apply {
            setOnPreparedListener(this@MediaContainer)
            setOnErrorListener(this@MediaContainer)
            setOnCompletionListener(this@MediaContainer)
            setOnSeekCompleteListener(this@MediaContainer)
        }
    }

    fun play(id: String?) {

    }

    fun play(uri: Uri?) {
        player.setDataSource(uri.toString())
        player.prepare()

        play()
    }

    fun play(song: Song) {

    }

    fun play(artistSong: ArtistSong) {

    }

    fun prepare(id: String?) {

    }

    fun prepare(uri: Uri?) {

    }

    fun prepare(song: Song) {

    }

    fun prepare(artistSong: ArtistSong) {

    }

    fun play() {
        player.start()
        service.mNotificationBuilder.build()
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {

    }

    override fun onCompletion(mp: MediaPlayer?) {

    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }
}