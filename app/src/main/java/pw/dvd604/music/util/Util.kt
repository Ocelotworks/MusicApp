package pw.dvd604.music.util

class Util {

    companion object {

        /**Creates a human readable string representing media duration
         * @param seconds The length of media in seconds in a non-nullable Int
         * @return String**/
        fun prettyTime(seconds: Int): String {
            val mins: Int = (seconds % 3600 / 60)
            val secs: Int = seconds % 60

            return "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
        }

        fun prettyTime(seconds: Long): String {
            val mins: Int = (seconds % 3600 / 60).toInt()
            val secs: Int = (seconds % 60).toInt()

            return "${if (mins < 10) "0" else ""}$mins:${if (secs < 10) "0" else ""}$secs"
        }
    }
}