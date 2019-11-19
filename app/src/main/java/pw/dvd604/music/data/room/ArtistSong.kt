package pw.dvd604.music.data.room

import pw.dvd604.music.data.CardData

class ArtistSong {
    var songID: String = ""
    var artistTitle: String = ""
    var songTitle = ""

    fun toCardData(): CardData {
        return CardData(title = songTitle, id = songID, type = "", url = "", subtext = artistTitle)
    }
}