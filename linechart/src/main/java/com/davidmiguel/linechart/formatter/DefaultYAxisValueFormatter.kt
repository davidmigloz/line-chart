package com.davidmiguel.linechart.formatter

import android.graphics.RectF
import java.text.DecimalFormat
import java.text.NumberFormat

/**
 * Formats the value with two decimals if needed.
 */
class DefaultYAxisValueFormatter : YAxisValueFormatter {

    private val formatter: NumberFormat = DecimalFormat("#.##")

    override fun getFormattedValue(value: Float, dataBounds: RectF, gridYDivisions: Int): String {
        return formatter.format(value.toDouble()).replace("^-(?=0(,0*)?$)".toRegex(), "") // Remove - sign if 0
    }
}
