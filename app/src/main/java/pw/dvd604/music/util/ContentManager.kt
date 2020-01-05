package pw.dvd604.music.util

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.dialog.ViewDialog
import java.io.File


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

        if (!File(Settings.storage).exists()) {
            File(Settings.storage).mkdirs()
            Log.e("File", "True")
        } else {
            Log.e("File", "false")
        }
    }

    fun buildDatabase() {
        //Here we're building the local database from the info we can get from the server
        //We're creating a loading dialog, but not showing it
        //And then in a co-routine we're building song, artist and album DB tables, along with creating the relations between them
        val dialog = ViewDialog(activity)

        GlobalScope.launch {
            try {

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun buildRelations(
        dialog: ViewDialog?
    ) {

        HTTP(context).getReq(
            "${BuildConfig.defaultURL}song",
            Response.Listener { res ->
                val array = JSONArray(res)
                dialog?.showDialog("Building table relations")
                GlobalScope.launch {
                    try {

                    } catch (e: Exception) {
                        Log.e("Error", "", e)
                    }
                    doneBuild()
                    dialog?.hideDialog()
                }
            })
    }

    private fun build(
        parse: (obj: JSONObject) -> Any,
        type: String,
        dialog: ViewDialog? = null
    ) {
        HTTP(context).getReq(
            "${BuildConfig.defaultURL}$type",
            Response.Listener { res ->
                val array = JSONArray(res)

                    dialog?.showDialog("Building core tables")

                GlobalScope.launch {
                    try {

                    } catch (e: Exception) {
                        Log.e("Error", "", e)
                    }
                }
            })
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