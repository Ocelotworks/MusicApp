package pw.dvd604.music.util

import android.os.AsyncTask

class DownloaderAsync : AsyncTask<String, Int, Boolean>() {

    override fun doInBackground(vararg params: String?): Boolean {
        return true
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
    }

}