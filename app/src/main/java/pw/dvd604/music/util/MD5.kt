package pw.dvd604.music.util

import android.text.TextUtils
import android.util.Log
import java.io.*
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class MD5 {
    companion object {
        private const val tag = "MD5"

        fun checkMD5(md5: String, updateFile: File?): Boolean {
            if (TextUtils.isEmpty(md5) || updateFile == null) {
                Log.e(tag, "MD5 string empty or updateFile null")
                return false
            }

            val calculatedDigest = calculateMD5(updateFile)
            if (calculatedDigest == null) {
                Log.e(tag, "calculatedDigest null")
                return false
            }

            Log.v(tag, "Calculated digest: $calculatedDigest")
            Log.v(tag, "Provided digest: $md5")

            return calculatedDigest.equals(md5, ignoreCase = true)
        }

        private fun calculateMD5(updateFile: File): String? {
            val digest: MessageDigest
            try {
                digest = MessageDigest.getInstance("MD5")
            } catch (e: NoSuchAlgorithmException) {
                Log.e(tag, "Exception while getting digest", e)
                return null
            }

            val `is`: InputStream
            try {
                `is` = FileInputStream(updateFile)
            } catch (e: FileNotFoundException) {
                Log.e(tag, "Exception while getting FileInputStream", e)
                return null
            }

            val buffer = ByteArray(8192)
            var read: Int
            try {

                do {
                    read = `is`.read(buffer)
                    if (read > 0)
                        digest.update(buffer, 0, read)
                } while (read > 0)

                val md5sum = digest.digest()
                val bigInt = BigInteger(1, md5sum)
                var output = bigInt.toString(16)
                // Fill to 32 chars
                output = String.format("%32s", output).replace(' ', '0')
                return output
            } catch (e: IOException) {
                throw RuntimeException("Unable to process file for MD5", e)
            } finally {
                try {
                    `is`.close()
                } catch (e: IOException) {
                    Log.e(tag, "Exception on closing MD5 input stream", e)
                }

            }
        }
    }
}