package com.davidmiguel.linechart.formatter;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Formats the value with two decimals if needed.
 */
public class DefaultYAxisValueFormatter implements YAxisValueFormatter {

    private NumberFormat formatter;

    public DefaultYAxisValueFormatter() {
        formatter =  new DecimalFormat( "#.##");
    }

    @NonNull
    @Override
    public String getFormattedValue(float value, @NonNull RectF dataBounds, int gridYDivisions) {
        return formatter.format(value);
    }
}
