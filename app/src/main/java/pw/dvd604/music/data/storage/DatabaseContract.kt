package pw.dvd604.music.data.storage

interface Table {
    var DROP_TABLE: String
    var CREATE_TABLE: String
}

object DatabaseContract {

    var tables = arrayOf(
        Artist,
        Playlist,
        Song,
        Album,
        Genre,
        SongsAlbums,
        SongsArtists,
        SongsGenres,
        PlaylistSongs,
        AlbumsArtists
    )

    object Artist : Table {
        const val TABLE_NAME = "artists"
        const val COLUMN_NAME_NAME = "name"

        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_NAME TEXT NOT NULL" +
                ")"
    }

    object Playlist : Table {
        const val TABLE_NAME = "playlists"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_OWNER = "owner"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_NAME TEXT NOT NULL," +
                "$COLUMN_NAME_OWNER TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object Song : Table {
        const val TABLE_NAME = "songs"
        const val COLUMN_NAME_ARTIST = "artist"
        const val COLUMN_NAME_ALBUM = "album"
        const val COLUMN_NAME_GENRE = "genre"
        const val COLUMN_NAME_DURATION = "duration"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_HASH = "hash"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_ARTIST TEXT NOT NULL," +
                "$COLUMN_NAME_ALBUM TEXT," +
                "$COLUMN_NAME_GENRE TEXT," +
                "$COLUMN_NAME_DURATION INTEGER," +
                "$COLUMN_NAME_TITLE TEXT NOT NULL," +
                "$COLUMN_NAME_HASH TEXT)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object Album : Table {
        const val TABLE_NAME = "albums"
        const val COLUMN_NAME_ARTIST = "artist"
        const val COLUMN_NAME_IMAGE = "image"
        const val COLUMN_NAME_NAME = "name"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_ARTIST TEXT NOT NULL," +
                "$COLUMN_NAME_IMAGE TEXT," +
                "$COLUMN_NAME_NAME TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object Genre : Table {
        const val TABLE_NAME = "genres"
        const val COLUMN_NAME_IMAGE = "image"
        const val COLUMN_NAME_NAME = "name"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_IMAGE TEXT," +
                "$COLUMN_NAME_NAME TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object SongsArtists : Table {
        const val TABLE_NAME = "song_artists"
        const val COLUMN_NAME_SONG_ID = "song"
        const val COLUMN_NAME_ARTIST_ID = "artist"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_ARTIST_ID TEXT NOT NULL," +
                "$COLUMN_NAME_SONG_ID TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object SongsAlbums : Table {
        const val TABLE_NAME = "album_songs"
        const val COLUMN_NAME_SONG_ID = "song"
        const val COLUMN_NAME_ALBUM_ID = "album"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_SONG_ID TEXT NOT NULL," +
                "$COLUMN_NAME_ALBUM_ID TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object SongsGenres : Table {
        const val TABLE_NAME = "genre_songs"
        const val COLUMN_NAME_SONG_ID = "song"
        const val COLUMN_NAME_GENRE_ID = "genre"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_GENRE_ID TEXT NOT NULL," +
                "$COLUMN_NAME_SONG_ID TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object AlbumsArtists : Table {
        const val TABLE_NAME = "aritst_albums"
        const val COLUMN_NAME_ARTIST_ID = "artist"
        const val COLUMN_NAME_ALBUM_ID = "album"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_ALBUM_ID TEXT NOT NULL," +
                "$COLUMN_NAME_ARTIST_ID TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }

    object PlaylistSongs : Table {
        const val TABLE_NAME = "playlist_songs"
        const val COLUMN_NAME_PLAYLIST_ID = "playlist"
        const val COLUMN_NAME_SONG_ID = "song"

        override var CREATE_TABLE = "CREATE TABLE $TABLE_NAME (" +
                "id TEXT NOT NULL PRIMARY KEY," +
                "$COLUMN_NAME_PLAYLIST_ID TEXT NOT NULL," +
                "$COLUMN_NAME_SONG_ID TEXT NOT NULL)"
        override var DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}