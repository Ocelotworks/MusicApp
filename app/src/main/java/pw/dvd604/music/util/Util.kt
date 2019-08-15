package pw.dvd604.music.util

class Util{
    companion object{
        fun prettyTime(seconds : Int) : String{
            val mins : Int = (seconds % 3600 / 60)
            val secs : Int = seconds % 60
            return "$mins:$secs"
        }
    }
}
