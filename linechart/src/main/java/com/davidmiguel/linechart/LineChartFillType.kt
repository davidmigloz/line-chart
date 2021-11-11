package com.davidmiguel.linechart

import androidx.annotation.IntDef
import com.davidmiguel.linechart.LineChartFillType.Companion.DOWN
import com.davidmiguel.linechart.LineChartFillType.Companion.NONE
import com.davidmiguel.linechart.LineChartFillType.Companion.TOWARDS_ZERO
import com.davidmiguel.linechart.LineChartFillType.Companion.UP

/**
 * Holds the fill type constants to be used with getFillType() and
 * setFillType(int).
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(NONE, UP, DOWN, TOWARDS_ZERO)
annotation class LineChartFillType {
    companion object {
        /**
         * Fill type constant for having no fill on the graph
         */
        const val NONE = 0

        /**
         * Fill type constant for always filling the area above the sparkline.
         */
        const val UP = 1

        /**
         * Fill type constant for always filling the area below the sparkline
         */
        const val DOWN = 2

        /**
         * Fill type constant for filling toward zero. This will fill downward if your sparkline is
         * positive, or upward if your sparkline is negative. If your sparkline intersects zero,
         * each segment will still color toward zero.
         */
        const val TOWARDS_ZERO = 3
    }
}
