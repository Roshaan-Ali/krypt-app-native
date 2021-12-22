package com.pyra.krpytapplication.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateDecelerateInterpolator

object AnimationHelper {


    fun enterRevelAnimation(revelView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cx = revelView.measuredWidth
            val cy = revelView.measuredHeight
            // get the final radius for the clipping circle
            val finalRadius = Math.max(revelView.width, revelView.height)
            // create the animator for this view (the start radius is zero)
            val anim = ViewAnimationUtils.createCircularReveal(
                revelView,
                cx,
                cy,
                0f,
                finalRadius.toFloat()
            )
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    revelView.visibility = View.VISIBLE
                }
            })
            anim!!.interpolator = AccelerateDecelerateInterpolator()
            anim.duration = 300
            revelView.visibility = View.VISIBLE
            anim.start()
        } else {
            revelView.visibility = View.VISIBLE
        }
    }

    fun exitRevelAnimation(
        revelView: View
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val cx = revelView.measuredWidth
            val cy = revelView.measuredHeight
            // get the initial radius for the clipping circle
            val initialRadius = revelView.width
            // create the animation (the final radius is zero)
            val anim = ViewAnimationUtils.createCircularReveal(
                revelView,
                cx,
                cy,
                initialRadius.toFloat(),
                0f
            )
            anim.interpolator = AccelerateDecelerateInterpolator()
            anim.duration = 300
            // make the view invisible when the animation is done
            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    revelView.visibility = View.GONE
                }
            })
            anim.start()
        } else {
            revelView.visibility = View.GONE
        }
    }
}