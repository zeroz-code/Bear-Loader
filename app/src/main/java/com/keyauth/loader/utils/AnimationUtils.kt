package com.keyauth.loader.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce

object AnimationUtils {

    /**
     * Animate view with spring effect for modern feel
     */
    fun animateSpring(view: View, property: DynamicAnimation.ViewProperty, finalValue: Float) {
        SpringAnimation(view, property, finalValue).apply {
            spring.dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            spring.stiffness = SpringForce.STIFFNESS_LOW
            start()
        }
    }

    /**
     * Fade in animation with scale effect
     */
    fun fadeInWithScale(view: View, duration: Long = 600) {
        view.alpha = 0f
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.visibility = View.VISIBLE

        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * Fade out animation with scale effect
     */
    fun fadeOutWithScale(view: View, duration: Long = 300, onComplete: (() -> Unit)? = null) {
        view.animate()
            .alpha(0f)
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onComplete?.invoke()
                }
            })
            .start()
    }

    /**
     * Slide in from right animation
     */
    fun slideInFromRight(view: View, duration: Long = 400) {
        view.translationX = view.width.toFloat()
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
            .start()
    }

    /**
     * Slide out to left animation
     */
    fun slideOutToLeft(view: View, duration: Long = 300, onComplete: (() -> Unit)? = null) {
        view.animate()
            .translationX(-view.width.toFloat())
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                    onComplete?.invoke()
                }
            })
            .start()
    }

    /**
     * Button press animation with 3D effect
     */
    fun animateButtonPress(view: View) {
        val scaleDown = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f).apply {
            duration = 100
            interpolator = AccelerateDecelerateInterpolator()
        }
        val elevationDown = ObjectAnimator.ofFloat(view, "translationZ", view.translationZ, view.translationZ - 4f).apply {
            duration = 100
        }

        scaleDown.start()
        scaleDownY.start()
        elevationDown.start()

        // Scale back up
        scaleDown.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f).apply {
                    duration = 150
                    interpolator = OvershootInterpolator()
                    start()
                }
                ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f).apply {
                    duration = 150
                    interpolator = OvershootInterpolator()
                    start()
                }
                ObjectAnimator.ofFloat(view, "translationZ", view.translationZ - 4f, view.translationZ).apply {
                    duration = 150
                    start()
                }
            }
        })
    }

    /**
     * Pulse animation for attention
     */
    fun pulseAnimation(view: View, duration: Long = 1000) {
        val animator = ValueAnimator.ofFloat(1f, 1.1f, 1f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        
        animator.addUpdateListener { animation ->
            val scale = animation.animatedValue as Float
            view.scaleX = scale
            view.scaleY = scale
        }
        
        animator.start()
    }

    /**
     * Shake animation for errors
     */
    fun shakeAnimation(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationX", 0f, 25f, -25f, 25f, -25f, 15f, -15f, 6f, -6f, 0f)
        animator.duration = 600
        animator.start()
    }

    /**
     * Rotate animation for loading
     */
    fun rotateAnimation(view: View, duration: Long = 1000) {
        val animator = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f)
        animator.duration = duration
        animator.repeatCount = ValueAnimator.INFINITE
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
    }

    /**
     * Stagger animation for multiple views
     */
    fun staggerAnimation(views: List<View>, delay: Long = 100) {
        views.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 50f
            
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * delay)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }
}
