package pw.dvd604.music.service

import android.media.MediaPlayer
import android.net.Uri
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.ArtistSong
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.storage.DatabaseContract

class MediaContainer(private val service: MediaPlaybackService) : MediaPlayer.OnErrorListener,
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
        if (id == null)
            return

        player.reset()
        player.setDataSource("https://unacceptableuse.com/petify/song/$id")
        player.prepare()

        play()
    }

    fun play(uri: Uri?) {
        player.reset()
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
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onPrepared(mp: MediaPlayer?) {

    }

    override fun onCompletion(mp: MediaPlayer?) {
        skip(1)
    }

    override fun onSeekComplete(mp: MediaPlayer?) {

    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        player.reset()
    }

    fun skip(i: Int) {
        if (i > 0) {
            val cursor = (service.applicationContext as MusicApplication).readableDatabase.rawQuery(
                "SELECT id FROM ${DatabaseContract.Song.TABLE_NAME} ORDER BY RANDOM() LIMIT 1",
                null,
                null
            )

            with(cursor) {
                while (moveToNext()) {

                    val id = getString(
                        getColumnIndexOrThrow("id")
                    )

                    service.mediaSession?.controller?.transportControls?.playFromMediaId(
                        id, null
                    )

                    service.mNotificationBuilder.build(id)
                }
                cursor.close()
            }
        }
    }

    fun currentPosition(): Long {
        return player.currentPosition.toLong()
    }
}