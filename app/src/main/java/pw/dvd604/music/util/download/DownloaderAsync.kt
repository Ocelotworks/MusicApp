package pw.dvd604.music.util.download

import android.os.AsyncTask
import pw.dvd604.music.adapter.data.Media
import pw.dvd604.music.adapter.data.MediaType
import pw.dvd604.music.util.Util
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection

class DownloaderAsync(
    val media: Media,
    private val callback: ((media: Media, progress: Int) -> Unit)?,
    private val completeCallback: ((media: Media) -> Unit)?,
    private val type: MediaType = MediaType.SONG
) :
    AsyncTask<Void, Int, Void>() {

    var failed: Boolean = false

    override fun doInBackground(vararg params: Void?): Void? {
        try {
            //Open the connection to the media
            if (type == MediaType.SONG) {
                if (File(media.toPath()).exists()) return null
            }

            val url = if (type == MediaType.SONG) {
                URL(media.toUrl())
            } else {
                URL(media.toAlbumUrl())
            }

            val connection: URLConnection = url.openConnection()
            connection.connect()

            //get the full size, and open streams to both file and server
            val fileLength = connection.contentLength

            val downStream = BufferedInputStream(url.openStream(), 8192)

            val outStream = FileOutputStream(
                if (type == MediaType.SONG) {
                    media.toPath()
                } else {
                    media.toAlbumPath()
                }
            )

            var data = ByteArray(8192)
            var downloadedBytes = 0

            //Write the stream to the file from the server
            do {
                val bytesRead = downStream.read(data)

                if (bytesRead == -1) break

                downloadedBytes += bytesRead

                if (fileLength != -1)
                    publishProgress((downloadedBytes / fileLength) * 100)

                outStream.write(data, 0, bytesRead)
                data = ByteArray(8192)
            } while (bytesRead != -1)

            //Tidy up streams
            outStream.flush()

            outStream.close()
            downStream.close()
        } catch (e: Exception) {
            Util.log(this, e.localizedMessage)
            failed = true
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        callback?.let { it(media, values[0]!!) }
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        Util.log(this, "Done downloading, failed: $failed")
        completeCallback?.let { it(media) }
    }

}