package pw.dvd604.music.util

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.Album
import pw.dvd604.music.data.Artist
import pw.dvd604.music.data.Song
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.data.storage.Table
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

    lateinit var songArray: JSONArray
    lateinit var artistArray: JSONArray
    lateinit var albumArray: JSONArray

    fun insert(table: Table, values: ContentValues) {
        app.database.insertWithOnConflict(
            table.TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun buildDatabase() {
        //Here we're building the local database from the info we can get from the server
        //We're creating a loading dialog, but not showing it
        //And then in a co-routine we're building song, artist and album DB tables, along with creating the relations between them
        val dialog = ViewDialog(activity)

        GlobalScope.launch {
            try {
                //https://unacceptableuse.com/petifyv3/api/v2/
                HTTP(context).getReq("${BuildConfig.defaultURL}song", Response.Listener { res ->
                    songArray = JSONArray(res)
                    HTTP(context).getReq(
                        "${BuildConfig.defaultURL}artist",
                        Response.Listener { res ->
                            artistArray = JSONArray(res)
                            HTTP(context).getReq(
                                "${BuildConfig.defaultURL}album",
                                Response.Listener { res ->
                                    albumArray = JSONArray(res)
                                    beginParse(dialog)
                                })
                        })
                })
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun ui(call: () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            call()
        }
    }

    private fun beginParse(dialog: ViewDialog) {
        Thread {
            try {
                if (DatabaseUtils.queryNumEntries(
                        app.readableDatabase,
                        DatabaseContract.Song.TABLE_NAME
                    ) < songArray.length()
                ) {
                    ui { dialog.showDialog("Please wait") }
                    for (i in 0 until songArray.length()) {
                        ui { dialog.setText("$i / ${songArray.length()} Songs") }
                        val data = songArray.getJSONObject(i)
                        val song = Song.parse(data)

                        val tableValues: ContentValues = song.toValues()

                        insert(DatabaseContract.Song, tableValues)

                        val songGenreRelation = ContentValues().apply {
                            put(DatabaseContract.SongsGenres.COLUMN_NAME_GENRE_ID, song.genreID)
                            put(DatabaseContract.SongsGenres.COLUMN_NAME_SONG_ID, song.id)
                        }

                        insert(DatabaseContract.SongsGenres, songGenreRelation)
                    }

                    for (i in 0 until artistArray.length()) {
                        ui { dialog.setText("$i / ${artistArray.length()} Artists") }
                        val data = artistArray.getJSONObject(i)
                        val artist = Artist.parse(data)

                        val tableValues: ContentValues = artist.toValues()

                        insert(DatabaseContract.Artist, tableValues)
                    }

                    for (i in 0 until albumArray.length()) {
                        ui { dialog.setText("$i / ${albumArray.length()} Albums") }
                        val data = albumArray.getJSONObject(i)
                        val album = Album.parse(data)

                        insert(DatabaseContract.Album, album.toValues())
                    }
                    dialog.hideDialog()
                    ui { doneBuild() }
                } else {
                    dialog.hideDialog()
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }.start()
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