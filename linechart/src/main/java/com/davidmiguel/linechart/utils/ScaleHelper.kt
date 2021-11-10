package com.davidmiguel.linechart.utils

import com.davidmiguel.linechart.LineChartAdapter
import android.graphics.RectF
import kotlin.math.abs

/**
 * Helper class for handling scaling logic.
 */
@Suppress("unused", "SameParameterValue")
class ScaleHelper(
    adapter: LineChartAdapter,
    drawingArea: RectF,
    numLabels: Int
) {

    // Width and height of the view
    private val width = drawingArea.width()
    private val height = drawingArea.height()
    // Min data value
    private var minY = 0f
    private var maxY = 0f
    private var minX = 0f
    private var maxX = 0f
    // Scale factor
    private var xScale = 0.0
    private var yScale = 0.0
    // Translation value
    private var xTranslation = 0.0
    private var yTranslation = 0.0

    init {
        // Get data
        val dataBounds = adapter.getDataBounds()
        // Calculate sizes
        calcMinMaxX(dataBounds)
        calcMinMaxY(dataBounds, numLabels, 0.05)
        calculateConversionFactors(drawingArea)
    }

    /**
     * Given a raw X value, it scales it to fit within our drawing area.
     */
    fun getX(rawX: Float): Float = (rawX * xScale + xTranslation).toFloat()

    /**
     * Given a raw Y value, it scales it to fit within our drawing area. This method also 'flips'
     * the value to be ready for drawing.
     */
    fun getY(rawY: Float): Float = (height - rawY * yScale + yTranslation).toFloat()

    /**
     * Given a scaled X value, it converts it back to the original raw value.
     */
    fun getRawX(scaledX: Float): Float = ((scaledX - xTranslation) / xScale).toFloat()

    /**
     * Given a scaled Y value, it converts it back to the original raw value.
     */
    fun getRawY(scaledY: Float): Float = ((height + yTranslation - scaledY) / yScale).toFloat()

    /**
     * Calculates minimum and maximum X values to represent.
     */
    private fun calcMinMaxX(bounds: RectF) {
        if (bounds.width() == 0f) {
            // If vertical line -> expand vertical bonds
            minX = bounds.left - 1
            maxX = bounds.right + 1
        } else {
            minX = bounds.left
            maxX = bounds.right
        }
    }

    /**
     * Calculates minimum and maximum Y values to represent.
     */
    private fun calcMinMaxY(bounds: RectF, numLabels: Int, coefficientExpansion: Double) {
        // Expand bonds
        var minYExpanded: Float
        var maxYExpanded: Float
        val halfLabels = numLabels / 2f
        if (bounds.height() == 0f) {
            // If it's a horizontal line, expand vertical bonds
            if (bounds.bottom == 0f) { // Horizontal 0 line -> expand just the top
                minYExpanded = 0f
                maxYExpanded = numLabels.toFloat()
            } else if (bounds.bottom > 0) { // Positive horizontal line -> expand both without going negative
                minYExpanded = bounds.top - halfLabels
                if (minYExpanded < 0) {
                    minYExpanded = 0f
                }
                maxYExpanded = bounds.bottom + halfLabels
            } else { // Negative horizontal line -> expand both without going positive
                minYExpanded = bounds.top - halfLabels
                maxYExpanded = bounds.bottom + halfLabels
                if (maxYExpanded > 0) {
                    maxYExpanded = 0f
                }
            }
        } else {
            // If it's not an horizontal line, use the coefficient of expansion
            minYExpanded = if (bounds.top == 0f) 0f else (bounds.top - bounds.height() * coefficientExpansion).toFloat()
            maxYExpanded = (bounds.bottom + bounds.height() * coefficientExpansion).toFloat()
        }
        val interval = abs(maxYExpanded - minYExpanded)
        // Determine label granularity
        val granularity = when {
            interval >= 5 -> 5f
            interval >= 2.5 -> 1f
            else -> 0.5f
        }
        // Round min and max to the closest step
        val minYRounded = Utils.round(minYExpanded, granularity, false)
        val maxYRounded = Utils.round(maxYExpanded, granularity, true)
        val roundedInterval = abs(maxYRounded - minYRounded)
        // Find the smallest interval that contains the data and it's divisible by the number of labels
        val divisibleInterval = Utils.lcm(roundedInterval, granularity)
        val step = (divisibleInterval / numLabels).toInt()
        val divisibleRoundedInterval = if (roundedInterval > 10) {
            Utils.round(step.toFloat(), granularity, true) * numLabels
        } else { // Allow smaller granularity for values smaller than 10
            step.toFloat() * numLabels
        }
        // Calculate how much we have to expand the original rounded interval
        val increment = abs(divisibleRoundedInterval - roundedInterval)
        val expandMax: Float
        val expandMin: Float
        if (increment > granularity && minYRounded != 0f) {
            // If we can divide the increment in two parts we expand bottom and top
            expandMin = increment / 2f
            expandMax = expandMin
        } else {
            // Expand top
            expandMax = increment
            expandMin = 0f
        }
        // Calculate final min and max values
        minY = minYRounded - expandMin
        maxY = maxYRounded + expandMax
    }

    /**
     * Calculate the conversion factors to convert between raw and scaled values.
     */
    private fun calculateConversionFactors(drawingArea: RectF) {
        // Padding
        val leftPadding = drawingArea.left
        val topPadding = drawingArea.top
        // xScale will compress or expand the min and max x values to be just inside the view
        xScale = (width / (maxX - minX)).toDouble()
        // xTranslation will move the x points back between 0 - width
        xTranslation = leftPadding - minX * xScale
        // yScale will compress or expand the min and max y values to be just inside the view
        yScale = (height / (maxY - minY)).toDouble()
        // yTranslation will move the y points back between 0 - height
        yTranslation = minY * yScale + topPadding
    }
}
