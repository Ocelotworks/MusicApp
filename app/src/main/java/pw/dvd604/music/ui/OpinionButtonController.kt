package pw.dvd604.music.ui

import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.widget.ImageView
import pw.dvd604.music.R


class OpinionButtonController(
    private val like: ImageView,
    private val centre: ImageView,
    private val dislike: ImageView,
    val callback: ((state: Int) -> Unit),
    private val duration: Long = 1000
) : View.OnClickListener {
    var isShown: Boolean = false
    var state: Drawable = centre.drawable
    var stateInt: Int = 0
    var openable: Boolean = true

    init {
        like.setOnClickListener(this)
        centre.setOnClickListener(this)
        dislike.setOnClickListener(this)

        like.visibility = View.INVISIBLE
        dislike.visibility = View.INVISIBLE
    }

    override fun onClick(view: View?) {
        if (!openable) return
        when (view?.id) {
            like.id -> {
                centre.setImageDrawable(like.drawable)
                state = like.drawable
                fadeOut(like)
                fadeOut(dislike)
                stateInt = 1

                isShown = false
            }
            centre.id -> {
                centre.setImageResource(R.drawable.baseline_thumbs_up_down_white_36)
                if (isShown) {
                    state = centre.drawable
                    fadeOut(like)
                    fadeOut(dislike)
                    stateInt = 0
                } else {
                    centre.setImageResource(R.drawable.baseline_thumbs_up_down_white_36)
                    fadeIn(like)
                    fadeIn(dislike)
                }

                isShown = !isShown
            }
            dislike.id -> {
                centre.setImageDrawable(dislike.drawable)
                state = dislike.drawable
                fadeOut(like)
                fadeOut(dislike)
                stateInt = -1

                isShown = false
            }
        }
        callback(stateInt)
    }

    fun resetState() {
        stateInt = 0
        centre.setImageResource(R.drawable.baseline_thumbs_up_down_white_36)
        state = centre.drawable
        isShown = false
    }

    private fun fadeOut(img: ImageView) {
        val fadeOut: Animation = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = duration
        fadeOut.setAnimationListener(object : AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                img.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        })
        img.startAnimation(fadeOut)
    }

    private fun fadeIn(img: ImageView) {
        val fadeOut: Animation = AlphaAnimation(0f, 1f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = duration
        fadeOut.setAnimationListener(object : AnimationListener {
            override fun onAnimationEnd(animation: Animation) {
                img.visibility = View.VISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationStart(animation: Animation) {}
        })
        img.startAnimation(fadeOut)
    }
}