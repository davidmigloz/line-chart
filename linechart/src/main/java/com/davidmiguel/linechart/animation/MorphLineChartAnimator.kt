package com.davidmiguel.linechart.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.graphics.Path
import androidx.annotation.IntRange
import com.davidmiguel.linechart.LineChartView

/**
 * Animates each point vertically from the previous position to the current position.
 */
class MorphLineChartAnimator : Animator(), LineChartAnimator {

    private val animator = ValueAnimator.ofFloat(0f, 1f)
    private val animationPath = Path()
    private var oldYPoints: List<Float>? = null

    override fun getAnimation(lineChartView: LineChartView): Animator? {
        val xPoints = lineChartView.xPoints
        val yPoints = lineChartView.yPoints
        if (xPoints.isEmpty() || yPoints.isEmpty()) return null
        animator.addUpdateListener { animation: ValueAnimator ->
            val animatedValue = animation.animatedValue as Float

            animationPath.reset()

            var step: Float
            var y: Float
            var oldY: Float
            val size = xPoints.size
            for (count in 0 until size) {

                // get oldY, can be 0 (zero) if current points are larger
                oldY = oldYPoints?.let { it[count * it.size / size] } ?: lineChartView.drawingArea.bottom
                step = yPoints[count] - oldY
                y = step * animatedValue + oldY
                if (count == 0) {
                    animationPath.moveTo(xPoints[count], y)
                } else {
                    animationPath.lineTo(xPoints[count], y)
                }
            }

            // set the updated path for the animation
            lineChartView.animationPath = animationPath
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                oldYPoints = yPoints
            }
        })
        return animator
    }

    override fun getStartDelay() = animator.startDelay

    override fun setStartDelay(@IntRange(from = 0) startDelay: Long) {
        animator.startDelay = startDelay
    }

    override fun setDuration(@IntRange(from = 0) duration: Long): Animator = animator

    override fun getDuration() = animator.duration

    override fun setInterpolator(timeInterpolator: TimeInterpolator?) {
        animator.interpolator = timeInterpolator
    }

    override fun isRunning() = animator.isRunning
}
