package com.davidmiguel.linechart.formatter;

import android.graphics.RectF;

import androidx.annotation.NonNull;

/**
 * Interface to format values shown in the Y axis of the chart.
 */
public interface YAxisValueFormatter {

    /**
     * Called when a value has to be formatted before being drawn.
     * For performance reasons, avoid excessive calculations and memory allocations inside
     * this method.
     *
     * @param value           the value to be formatted.
     * @return formatted value.
     */
    @NonNull
    String getFormattedValue(float value, @NonNull RectF dataBounds, int gridYDivisions);

}
