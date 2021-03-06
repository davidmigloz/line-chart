package com.davidmiguel.linechart.formatter;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Formatter that adds a € sign after the number.
 */
public class EuroYAxisValueFormatter implements YAxisValueFormatter {

    private NumberFormat formatter;

    public EuroYAxisValueFormatter() {
        formatter =  new DecimalFormat( "#.##€");
    }

    @NonNull
    @Override
    public String getFormattedValue(float value, @NonNull RectF dataBounds, int gridYDivisions) {
        return formatter.format(value);
    }
}
