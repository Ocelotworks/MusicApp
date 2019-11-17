package pw.dvd604.music.data

import androidx.room.PrimaryKey

open class CardData(
    var title: String,
    @PrimaryKey var id: String,
    var type: String,
    var url: String
)