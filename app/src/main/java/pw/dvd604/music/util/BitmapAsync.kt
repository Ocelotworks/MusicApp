package pw.dvd604.music.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import pw.dvd604.music.MainActivity
import pw.dvd604.music.fragment.NowPlayingFragment
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class BitmapAsync(private val nowPlayingFragment: NowPlayingFragment?) : AsyncTask<String, Void, Bitmap>() {

    override fun doInBackground(vararg url: String?): Bitmap? {
        try {
            val url = URL(url[0])
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            return null
        }
    }

    override fun onPostExecute(bmp : Bitmap?) {
        if(bmp == null){
            val activity : MainActivity = nowPlayingFragment?.activity as MainActivity
            activity.report("Failed to connect to server")
        }
        nowPlayingFragment?.postImage(bmp)
    }

}