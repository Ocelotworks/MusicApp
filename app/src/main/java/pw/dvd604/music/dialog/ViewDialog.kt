package pw.dvd604.music.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget
import kotlinx.android.synthetic.main.loading_dialog.*
import pw.dvd604.music.R


class ViewDialog(private var activity: Activity) {
    private lateinit var dialog: Dialog

    fun showDialog() {

        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)

        val imageViewTarget = GlideDrawableImageViewTarget(dialog.loadingImageview)
        Glide.with(activity)
            .load<Any>(R.drawable.loading)
            .placeholder(R.drawable.loading)
            .centerCrop()
            .crossFade()
            .into(imageViewTarget)

        dialog.show()
    }

    fun hideDialog() {
        dialog.dismiss()
    }

}