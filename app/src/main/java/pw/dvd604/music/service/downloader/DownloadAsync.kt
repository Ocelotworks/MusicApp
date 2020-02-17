package pw.dvd604.music.service.downloader

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection

class DownloaderAsync(
    private val callback: (() -> Unit)?,
    private val completeCallback: (() -> Unit)?
) :
    AsyncTask<String, Int, Void>() {

    var failed: Boolean = false

    override fun doInBackground(vararg params: String?): Void? {
        try {
            //Open the connection to the media

            val url = URL(params[0])

            val connection: URLConnection = url.openConnection()
            connection.connect()

            //get the full size, and open streams to both file and server
            val fileLength = connection.contentLength

            val downStream = BufferedInputStream(url.openStream(), 8192)

            val urlsplit = params[0]?.split("/")
            val file = File(
                "${Environment.getExternalStorageDirectory()}/petify/${urlsplit?.get(urlsplit.size - 1)}"
            )

            if (file.exists()) {
                return null
            }

            Log.e("Downloader", file.absolutePath)

            val path = File(
                "${Environment.getExternalStorageDirectory()}/petify/"
            )
            path.mkdirs()
            file.createNewFile()

            val outStream = FileOutputStream(
                file
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
            Log.e("Downloader", "", e)
            failed = true
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        callback?.let { it() }
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        completeCallback?.let { it() }
    }

}