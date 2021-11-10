package com.davidmiguel.linechart.formatter

import android.graphics.RectF

/**
 * Interface to format values shown in the Y axis of the chart.
 */
fun interface YAxisValueFormatter {
    /**
     * Called when a value has to be formatted before being drawn.
     * For performance reasons, avoid excessive calculations and memory allocations inside
     * this method.
     *
     * @param value the value to be formatted.
     * @return formatted value.
     */
    fun getFormattedValue(value: Float, dataBounds: RectF, gridYDivisions: Int): String
}
