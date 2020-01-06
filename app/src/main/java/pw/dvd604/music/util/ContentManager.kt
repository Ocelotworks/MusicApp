package pw.dvd604.music.util

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.dialog.ViewDialog


class ContentManager(
    private val context: Context,
    private val activity: Activity,
    private val doneBuild: () -> Unit
) {

    private var app: MusicApplication

    var permissions = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    init {
        if (context !is Application) {
            throw Exception("Content Manager requires Application Context")
        }

        app = context as MusicApplication
    }

    fun buildDatabase() {
        //Here we're building the local database from the info we can get from the server
        //We're creating a loading dialog, but not showing it
        //And then in a co-routine we're building song, artist and album DB tables, along with creating the relations between them
        val dialog = ViewDialog(activity)

        dialog.showDialog("Building tables")
        GlobalScope.launch {
            try {
                //https://unacceptableuse.com/petifyv3/api/v2/
                HTTP(context).getReq("${BuildConfig.defaultURL}song", Response.Listener { res ->

                    val array = JSONArray(res)

                    Thread {
                        for (i in 0 until array.length()) {
                            val data = array.getJSONObject(i)
                            val song = Song.parse(data)

                            val tableValues: ContentValues = song.toValues()

                            app.database.insertWithOnConflict(
                                DatabaseContract.Song.TABLE_NAME,
                                null,
                                tableValues,
                                SQLiteDatabase.CONFLICT_REPLACE
                            )
                        }

                        dialog.hideDialog()
                    }.start()
                })

                HTTP(context).getReq("${BuildConfig.defaultURL}artist", Response.Listener { res ->
                    val array = JSONArray(res)

                    for (i in 0 until array.length()) {
                        val data = array.getJSONObject(i)
                        val artist = Artist.parse(data)
                    }
                })

                HTTP(context).getReq("${BuildConfig.defaultURL}album", Response.Listener { res ->
                    val array = JSONArray(res)

                    for (i in 0 until array.length()) {
                        val data = array.getJSONObject(i)
                        val album = Album.parse(data)
                    }
                })

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    fun requestPermissions(): Boolean {
        var result: Int
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(activity, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, listPermissionsNeeded.toTypedArray(), 100)
            return false
        }
        return true
    }
}