package pw.dvd604.music.data

import org.json.JSONObject

class Artist(
    id: String = "",
    title: String = "",
    image: String = ""
) : CardData(title, id, "artist", "https://unacceptableuse.com/petify/artist/") {
    companion object {
        fun parse(obj: JSONObject): Artist {
            val artist = Artist()
            artist.id = obj.getString("id")
            artist.title = obj.getString("name")
            return artist
        }
    }
}