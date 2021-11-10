package com.davidmiguel.linechart.formatter

import android.graphics.RectF
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * Formatter that adds a % sign after the number.
 */
class PercentYAxisValueFormatter : YAxisValueFormatter {

    private val formatter: NumberFormat = DecimalFormat("#.##'%'")

    override fun getFormattedValue(value: Float, dataBounds: RectF, gridYDivisions: Int): String {
        return formatter.format(value.toDouble())
    }
}
