package pw.dvd604.music.data.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        DatabaseContract.tables.forEach {
            db.execSQL(it.CREATE_TABLE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        if (oldVersion == 11 && newVersion == 12) {
            val sql =
                "ALTER TABLE ${DatabaseContract.Opinion.TABLE_NAME} ADD ${DatabaseContract.Opinion.COLUMN_NAME_SENT} INTEGER"
            db.execSQL(sql)
            return
        }

        DatabaseContract.changedTables.forEach {
            db.execSQL(it.DROP_TABLE)
        }

        DatabaseContract.changedTables.forEach {
            db.execSQL(it.CREATE_TABLE)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 12
        const val DATABASE_NAME = "Neilify.db"
    }
}