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
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.room.AlbumSongJoin
import pw.dvd604.music.data.room.ArtistSongJoin
import pw.dvd604.music.data.room.dao.AlbumSongJoinDao
import pw.dvd604.music.data.room.dao.ArtistSongJoinDao
import pw.dvd604.music.data.room.dao.BaseDao
import pw.dvd604.music.data.room.dao.SongDao
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
                build(app.db.songDao() as BaseDao<Any>, Song.Companion::parse, "song", dialog)
                build(app.db.artistDao() as BaseDao<Any>, Artist.Companion::parse, "artist", dialog)
                build(app.db.albumDao() as BaseDao<Any>, Album.Companion::parse, "album", dialog)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun buildRelations(
        artistSongJoinDao: ArtistSongJoinDao,
        albumSongJoinDao: AlbumSongJoinDao,
        dialog: ViewDialog?
    ) {

        HTTP(context).getReq(
            "${BuildConfig.defaultURL}song",
            Response.Listener { res ->
                val array = JSONArray(res)
                dialog?.showDialog("Building table relations")
                GlobalScope.launch {
                    try {
                        if (artistSongJoinDao.count() < array.length()) {
                            for (i in 0 until array.length()) {
                                val json = array.getJSONObject(i)
                                try {
                                    val artistJoin = ArtistSongJoin(
                                        songID = json.getString("id"),
                                        artistID = json.getString("artistID")
                                    )

                                    val albumJoin = AlbumSongJoin(
                                        songID = json.getString("id"),
                                        albumID = json.getString("albumID")
                                    )

                                    artistSongJoinDao.insert(artistJoin)
                                    albumSongJoinDao.insert(albumJoin)
                                } catch (ignored: java.lang.Exception) {
                                    Log.e(
                                        "BRelations",
                                        "Error from build song relations from ${json}: ${ignored.message}"
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("Error", "", e)
                    }
                    doneBuild()
                    dialog?.hideDialog()
                }
            })
    }

    private fun build(
        dao: BaseDao<Any>,
        parse: (obj: JSONObject) -> Any,
        type: String,
        dialog: ViewDialog? = null
    ) {
        if (dao.count() > 0) return

        HTTP(context).getReq(
            "${BuildConfig.defaultURL}$type",
            Response.Listener { res ->
                val array = JSONArray(res)

                if (dao is SongDao)
                    dialog?.showDialog("Building core tables")

                GlobalScope.launch {
                    try {
                        if (array.length() > dao.count()) {
                            for (i in 0 until array.length()) {
                                GlobalScope.launch {
                                    try {
                                        val dataObject = parse(array.getJSONObject(i))

                                        dao.insert(dataObject)
                                    } catch (e: Exception) {
                                        Log.e("Error", "", e)
                                    }
                                }
                            }
                        }

                        if (dao is SongDao) {
                            buildRelations(
                                app.db.artistSongJoinDao(),
                                app.db.albumSongJoinDao(),
                                dialog
                            )
                        }
                        dialog?.hideDialog()
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