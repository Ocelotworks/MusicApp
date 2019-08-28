package pw.dvd604.music.util.download

import android.os.AsyncTask
import pw.dvd604.music.adapter.data.Song
import pw.dvd604.music.util.Util
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection

class DownloaderAsync(
    val song: Song,
    val callback: (song: Song, progress: Int) -> Unit,
    val completeCallback: (song: Song) -> Unit
) :
    AsyncTask<Void, Int, Void>() {

    override fun doInBackground(vararg params: Void?): Void? {
        //Open the connection to the song
        val url = URL(Util.songToUrl(song))
        val connection: URLConnection = url.openConnection()
        connection.connect()

        //get the full size, and open streams to both file and server
        val fileLength = connection.contentLength

        val downStream = BufferedInputStream(url.openStream(), 8192)
        val outStream = FileOutputStream(Util.songToPath(song))

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

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        callback(song, values[0]!!)
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        Util.log(this, "Done downloading")
        completeCallback(song)
    }

}