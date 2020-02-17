package pw.dvd604.music.data.adapter

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.FileOutputStream


class SaveTarget(private val context: Context, private val path: String) :
    CustomTarget<Bitmap>(300, 300) {
    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        val outputStream: FileOutputStream

        Log.e("Test", context.filesDir.path)
        try {
            outputStream = context.openFileOutput(path, MODE_PRIVATE)
            resource.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    override fun onLoadCleared(placeholder: Drawable?) {

    }
}