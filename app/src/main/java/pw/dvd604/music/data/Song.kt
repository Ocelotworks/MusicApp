package pw.dvd604.music.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "song")
data class Song(
    @PrimaryKey var id: String = "",
    var title: String = ""
) {

}