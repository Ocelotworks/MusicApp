package pw.dvd604.music.service.downloader

import android.os.AsyncTask
import android.os.Environment
import android.util.Log
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import kotlin.reflect.KFunction1

class DownloaderAsync(
    private val callback: KFunction1<@ParameterName(name = "value") Array<out Int?>, Unit>,
    private val completeCallback: (() -> Unit)?
) :
    AsyncTask<ArrayList<String>, Int, Void>() {

    var failed: Int = 0
    var progress: Int = 0

    override fun doInBackground(vararg params: ArrayList<String>?): Void? {

        //Open the connection to the media

        params[0]?.forEach {
            try {
                progress++
                val url = URL(it)

                val connection: URLConnection = url.openConnection()
                connection.connect()

                //get the full size, and open streams to both file and server
                val fileLength = connection.contentLength

                val downStream = BufferedInputStream(url.openStream(), 8192)

                val urlsplit = it.split("/")
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

                    outStream.write(data, 0, bytesRead)
                    data = ByteArray(8192)
                } while (bytesRead != -1)

                //Tidy up streams
                outStream.flush()

                outStream.close()
                downStream.close()
                publishProgress(failed, progress)
            } catch (e: Exception) {
                Log.e("Downloader", "", e)
                failed++
            }
        }


        return null
    }


    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        callback(values)
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        completeCallback?.let { it() }
    }

}