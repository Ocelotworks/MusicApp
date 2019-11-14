package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "artist")
data class Artist(
    @PrimaryKey var id: String = "",
    var title: String = "",
    @Ignore val songs: ArrayList<Song>? = null
) {
}