package pw.dvd604.music.dialog

import android.app.Activity
import android.app.Dialog
import android.view.Window
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.loading_dialog.*
import pw.dvd604.music.R


class ViewDialog(private var activity: Activity) {
    private lateinit var dialog: Dialog
    private var dialogHideListener: DialogHideListener? = null

    fun showDialog(s: String = "") {

        dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.loading_dialog)

        Glide.with(activity)
            .asGif()
            .load(R.drawable.loading)
            .placeholder(R.drawable.loading)
            .centerCrop()
            .into(dialog.loadingImageview)

        dialog.loadingText.text = s

        dialog.show()
    }

    fun hideDialog() {
        if (!dialog.isShowing)
            return

        dialogHideListener?.onHide()
        dialog.dismiss()
    }

    fun setListener(dialogHideListener: DialogHideListener) {
        this.dialogHideListener = dialogHideListener
    }

}