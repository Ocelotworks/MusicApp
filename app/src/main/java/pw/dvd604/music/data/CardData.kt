package pw.dvd604.music.data

open class CardData(
    var title: String,
    var iID: String,
    var type: String = "album",
    open var url: String = "https://unacceptableuse.com/petify/$type/$iID"
)