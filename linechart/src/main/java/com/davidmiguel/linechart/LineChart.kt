package com.davidmiguel.linechart

import android.graphics.Path
import android.graphics.RectF
import androidx.annotation.ColorInt
import com.davidmiguel.linechart.animation.LineChartAnimator
import com.davidmiguel.linechart.formatter.YAxisValueFormatter
import com.davidmiguel.linechart.touch.OnScrubListener

interface LineChart {

    // Data

    /**
     * Sets the backing [LineChartAdapter] to generate the points to be graphed.
     */
    var adapter: LineChartAdapter

    /**
     * Rectangle where the chart is painted.
     */
    val drawingArea: RectF

    /**
     * Returns a copy of current graphic X points.
     */
    val xPoints: List<Float>

    /**
     * Returns a copy of current graphic Y points.
     */
    val yPoints: List<Float>

    // Styleable properties

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
     * Sets the color of the base line.
     */
    @get:ColorInt
    var baseLineColor: Int

    /**
     * Sets the width in pixels of the base line's stroke.
     */
    var baseLineWidth: Float

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
     * Sets the side margin of the labels.
     */
    var labelMargin: Float

    /**
     * Sets the text color of the labels.
     */
    @get:ColorInt
    var labelTextColor: Int

    /**
     * Sets the text size of the labels.
     */
    var labelTextSize: Float

    /**
     * Sets the background color of the labels.
     */
    @get:ColorInt
    var labelBackgroundColor: Int

    /**
     * Sets the background radius of the labels.
     */
    var labelBackgroundRadius: Float

    /**
     * Sets the background horizontal padding of the labels.
     */
    var labelBackgroundPaddingHorizontal: Float

    /**
     * Sets the background vertical padding of the labels.
     */
    var labelBackgroundPaddingVertical: Float

    /**
     * Sets the text color of the zero label.
     */
    @get:ColorInt
    var zeroLabelTextColor: Int

    /**
     * Sets the text size of the zero label.
     */
    var zeroLabelTextSize: Float

    /**
     * Sets the background color of the zero label.
     */
    @get:ColorInt
    var zeroLabelBackgroundColor: Int

    /**
     * Sets the color of the scrub line.
     */
    @get:ColorInt
    var scrubLineColor: Int

    /**
     * Set the width in pixels of the scrub line's stroke.
     */
    var scrubLineWidth: Float

    // How to draw

    /**
     * Sets formatter used to format labels in Y axis.
     * If null is passed, the default formatter will be used.
     */
    var yAxisValueFormatter: YAxisValueFormatter

    // What to draw

    /**
     * Sets the number of grid divisions in X axis.
     */
    var gridXDivisions: Int

    /**
     * Sets the number of grid divisions in Y axis.
     */
    var gridYDivisions: Int

    /**
     * Sets whether or not to show the zero line.
     */
    var isZeroLineEnabled: Boolean

    /**
     * Sets whether or not to enable scrubbing on this view.
     */
    var isScrubEnabled: Boolean

    // Touch

    /**
     * Sets a [OnScrubListener] to be notified of the user's scrubbing gestures.
     */
    var scrubListener: OnScrubListener?

    // Animation

    /**
     * Animator class to animate the Line Chart.
     */
    var lineChartAnimator: LineChartAnimator?

    /**
     * Sets the path to animate in onDraw, used by animators.
     */
    var animationPath: Path?
}
