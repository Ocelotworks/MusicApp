package pw.dvd604.music.data.room.dao

import androidx.room.Delete
import androidx.room.Insert

interface BaseDao<T> {
    @Insert
    fun insertAll(vararg objs: T)

    @Delete
    fun delete(obj: T)

    fun count(): Int

    @Insert
    fun insert(dataObject: T)

    fun getAll(): List<T>
}