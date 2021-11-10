package com.davidmiguel.linechart

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.ColorInt
import com.davidmiguel.linechart.animation.LineChartAnimator
import com.davidmiguel.linechart.formatter.YAxisValueFormatter
import com.davidmiguel.linechart.touch.OnScrubListener

interface LineChart {

    /**
     * Sets the backing [LineChartAdapter] to generate the points to be graphed.
     */
    var adapter: LineChartAdapter

    /**
     * Sets the color of the line.
     */
    @get:ColorInt
    var lineColor: Int

    /**
     * Sets the width in pixels of the line's stroke.
     */
    var lineWidth: Float

    /**
     * Sets the corner radius in pixels to use when rounding the line's segments.
     * Passing 0 indicates that corners should not be rounded.
     */
    var cornerRadius: Float

    /**
     * Sets how the chart has to be filled if fill is enabled.
     */
    @get:LineChartFillType
    var fillType: Int

    /**
     * Sets the color of the filling.
     */
    @get:ColorInt
    var fillColor: Int

    /**
     * Sets the color of the base line.
     */
    @get:ColorInt
    var gridLineColor: Int

    /**
     * Sets the width in pixels of the grid line's stroke.
     */
    var gridLineWidth: Float

    /**
     * Sets the number of grid divisions in X axis.
     */
    var gridXDivisions: Int

    /**
     * Sets the number of grid divisions in Y axis.
     */
    var gridYDivisions: Int

    /**
     * Sets formatter used to format labels in Y axis.
     * If null is passed, the default formatter will be used.
     */
    var yAxisValueFormatter: YAxisValueFormatter

    /**
     * Sets the color of the base line.
     */
    @get:ColorInt
    var baseLineColor: Int

    /**
     * Sets the width in pixels of the base line's stroke.
     */
    var baseLineWidth: Float

    /**
     * Sets whether or not to show the zero line.
     */
    var isZeroLineEnabled: Boolean

    /**
     * Sets the color of the zero line.
     */
    @get:ColorInt
    var zeroLineColor: Int

    /**
     * Sets the width in pixels of the zero line's stroke.
     */
    var zeroLineWidth: Float

    /**
     * Sets whether or not to enable scrubbing on this view.
     */
    var isScrubEnabled: Boolean

    /**
     * Sets the color of the scrub line.
     */
    @get:ColorInt
    var scrubLineColor: Int

    /**
     * Set the width in pixels of the scrub line's stroke.
     */
    var scrubLineWidth: Float

    /**
     * Sets a [OnScrubListener] to be notified of the user's scrubbing gestures.
     */
    var scrubListener: OnScrubListener?

    /**
     * Animator class to animate the Line Chart.
     */
    var lineChartAnimator: LineChartAnimator?

    /**
     * Sets the path to animate in onDraw, used by animators.
     */
    var animationPath: Path?

    val drawingArea: RectF

    /**
     * Returns a copy of current graphic X points.
     */
    val xPoints: List<Float>

    /**
     * Returns a copy of current graphic Y points.
     */
    val yPoints: List<Float>
}