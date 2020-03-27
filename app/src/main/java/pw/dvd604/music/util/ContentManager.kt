package pw.dvd604.music.util

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import pw.dvd604.music.BuildConfig
import pw.dvd604.music.MusicApplication
import pw.dvd604.music.data.*
import pw.dvd604.music.data.storage.DatabaseContract
import pw.dvd604.music.data.storage.Table
import pw.dvd604.music.ui.dialog.ViewDialog


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
    lateinit var playlistArray: JSONArray

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
                                    HTTP(context).getReq(
                                        "${BuildConfig.defaultURL}playlist?include-songs=true",
                                        Response.Listener { res ->
                                            playlistArray = JSONArray(res)
                                            beginParse(dialog)
                                        })
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
                        val opinionValue = ContentValues().apply {
                            put(DatabaseContract.Opinion.COLUMN_NAME_OPINION, 0)
                            put("id", song.id)
                        }

                        insert(DatabaseContract.Song, tableValues)
                        insert(DatabaseContract.Opinion, opinionValue)

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

                    for (i in 0 until playlistArray.length()) {
                        ui { dialog.setText("$i / ${playlistArray.length()} Playlists") }
                        val data = playlistArray.getJSONObject(i)
                        val playlist = Playlist.parse(data)

                        insert(DatabaseContract.Playlist, playlist.toValues())

                        val songList = data.getJSONArray("songs")
                        for (j in 0 until songList.length()) {
                            val songData = songList.getJSONObject(j)

                            val id = songData.getString("id")
                            val values = ContentValues().apply {
                                this.put(
                                    DatabaseContract.PlaylistSongs.COLUMN_NAME_PLAYLIST_ID,
                                    playlist.id
                                )
                                this.put(DatabaseContract.PlaylistSongs.COLUMN_NAME_SONG_ID, id)
                            }
                            insert(DatabaseContract.PlaylistSongs, values)
                        }
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

    fun getSongsWithArtists(): ArrayList<CardData> {
        val list = ArrayList<CardData>(0)

        try {

            val sql = if (Settings.getBoolean(Settings.blacklist)) {
                "SELECT ${DatabaseContract.Song.TABLE_NAME}.id, ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} INNER JOIN ${DatabaseContract.Opinion.TABLE_NAME} ON ${DatabaseContract.Opinion.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.id WHERE ${DatabaseContract.Opinion.COLUMN_NAME_OPINION} <> -1 ORDER BY ${DatabaseContract.Artist.COLUMN_NAME_NAME} ASC"
            } else {
                "SELECT ${DatabaseContract.Song.TABLE_NAME}.id, ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} ORDER BY ${DatabaseContract.Artist.COLUMN_NAME_NAME} ASC"
            }
            val cursor = app.readableDatabase.rawQuery(
                sql,
                null,
                null
            )
            with(cursor) {
                while (moveToNext()) {
                    val data = CardData(
                        id = getString(getColumnIndexOrThrow("id")),
                        title = getString(
                            getColumnIndexOrThrow(
                                DatabaseContract.Song.COLUMN_NAME_TITLE
                            )
                        ),
                        type = "song",
                        url = "",
                        subtext = getString(
                            getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME)
                        )
                    )

                    list.add(data)
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("", "", e)
        }

        return list
    }

    fun getSongsFromArtist(artistId: String): ArrayList<CardData> {
        val list = ArrayList<CardData>(0)

        try {
            val cursor = app.readableDatabase.rawQuery(
                "SELECT ${DatabaseContract.Song.TABLE_NAME}.id, ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} WHERE ${DatabaseContract.Artist.TABLE_NAME}.id = ? ORDER BY ${DatabaseContract.Artist.COLUMN_NAME_NAME} ASC",
                arrayOf(artistId),
                null
            )
            with(cursor) {
                while (moveToNext()) {
                    val data = CardData(
                        id = getString(getColumnIndexOrThrow("id")),
                        title = getString(
                            getColumnIndexOrThrow(
                                DatabaseContract.Song.COLUMN_NAME_TITLE
                            )
                        ),
                        type = "song",
                        url = "",
                        subtext = getString(
                            getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME)
                        )
                    )

                    list.add(data)
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("", "", e)
        }

        return list
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

    fun getSongsFromAlbum(id: String): ArrayList<CardData> {
        val list = ArrayList<CardData>(0)

        try {
            val cursor = app.readableDatabase.rawQuery(
                "SELECT ${DatabaseContract.Song.TABLE_NAME}.id, ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} WHERE ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ALBUM} = ? ORDER BY ${DatabaseContract.Artist.COLUMN_NAME_NAME} ASC",
                arrayOf(id),
                null
            )
            with(cursor) {
                while (moveToNext()) {
                    val data = CardData(
                        id = getString(getColumnIndexOrThrow("id")),
                        title = getString(
                            getColumnIndexOrThrow(
                                DatabaseContract.Song.COLUMN_NAME_TITLE
                            )
                        ),
                        type = "song",
                        url = "",
                        subtext = getString(
                            getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME)
                        )
                    )

                    list.add(data)
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("", "", e)
        }

        return list
    }

    fun getPlaylists(): ArrayList<CardData> {
        val list = ArrayList<CardData>(0)

        try {
            val cursor = app.readableDatabase.rawQuery(
                "SELECT * FROM ${DatabaseContract.Playlist.TABLE_NAME} ORDER BY ${DatabaseContract.Playlist.COLUMN_NAME_NAME} ASC",
                null,
                null
            )
            with(cursor) {
                while (moveToNext()) {
                    val data = CardData(
                        id = getString(getColumnIndexOrThrow("id")),
                        title = getString(
                            getColumnIndexOrThrow(
                                DatabaseContract.Playlist.COLUMN_NAME_NAME
                            )
                        ),
                        type = "playlist",
                        url = "playlist"
                    )

                    list.add(data)
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("", "", e)
        }

        return list
    }

    fun getPlaylistContents(id: String): ArrayList<CardData> {
        val list = ArrayList<CardData>(0)

        try {
            val cursor = app.readableDatabase.rawQuery(
                "SELECT ${DatabaseContract.Song.TABLE_NAME}.id, ${DatabaseContract.Song.COLUMN_NAME_TITLE}, ${DatabaseContract.Artist.COLUMN_NAME_NAME} FROM ${DatabaseContract.Song.TABLE_NAME} INNER JOIN ${DatabaseContract.Artist.TABLE_NAME} ON ${DatabaseContract.Artist.TABLE_NAME}.id = ${DatabaseContract.Song.TABLE_NAME}.${DatabaseContract.Song.COLUMN_NAME_ARTIST} INNER JOIN ${DatabaseContract.PlaylistSongs.TABLE_NAME} ON ${DatabaseContract.PlaylistSongs.TABLE_NAME}.${DatabaseContract.PlaylistSongs.COLUMN_NAME_SONG_ID} = ${DatabaseContract.Song.TABLE_NAME}.id WHERE ${DatabaseContract.PlaylistSongs.TABLE_NAME}.${DatabaseContract.PlaylistSongs.COLUMN_NAME_PLAYLIST_ID} = ?",
                arrayOf(id),
                null
            )

            with(cursor) {
                while (moveToNext()) {
                    val data = CardData(
                        id = getString(getColumnIndexOrThrow("id")),
                        title = getString(
                            getColumnIndexOrThrow(
                                DatabaseContract.Song.COLUMN_NAME_TITLE
                            )
                        ),
                        type = "song",
                        url = "",
                        subtext = getString(
                            getColumnIndexOrThrow(DatabaseContract.Artist.COLUMN_NAME_NAME)
                        )
                    )

                    list.add(data)
                }
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("", "", e)
        }
        return list
    }
}