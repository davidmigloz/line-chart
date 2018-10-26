package com.davidmiguel.linechart;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Holds the fill type constants to be used with getFillType() and
 * setFillType(int).
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({
        LineChartFillType.NONE,
        LineChartFillType.UP,
        LineChartFillType.DOWN,
        LineChartFillType.TOWARD_ZERO,
})
public @interface LineChartFillType {
    /**
     * Fill type constant for having no fill on the graph
     */
    int NONE = 0;

    /**
     * Fill type constant for always filling the area above the sparkline.
     */
    int UP = 1;

    /**
     * Fill type constant for always filling the area below the sparkline
     */
    int DOWN = 2;

    /**
     * Fill type constant for filling toward zero. This will fill downward if your sparkline is
     * positive, or upward if your sparkline is negative. If your sparkline intersects zero,
     * each segment will still color toward zero.
     */
    int TOWARD_ZERO = 3;
}
