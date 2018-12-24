package com.davidmiguel.linechart.utils;

import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.davidmiguel.linechart.LineChartAdapter;

import static com.davidmiguel.linechart.utils.Utils.lcm;
import static com.davidmiguel.linechart.utils.Utils.round;

/**
 * Helper class for handling scaling logic.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class ScaleHelper {

    // Width and height of the view
    private float width;
    private float height;
    // Min data value
    private float minY;
    private float maxY;
    private float minX;
    private float maxX;
    // Scale factor
    private double xScale;
    private double yScale;
    // Translation value
    private double xTranslation;
    private double yTranslation;

    public ScaleHelper(@NonNull LineChartAdapter adapter,
                       @NonNull RectF drawingArea, int numLabels) {
        // Get data
        width = drawingArea.width();
        height = drawingArea.height();
        RectF dataBounds = adapter.getDataBounds();
        // Calculate sizes
        calcMinMaxX(dataBounds);
        calcMinMaxY(dataBounds, numLabels, 0.05);
        calculateConversionFactors(drawingArea);
    }

    /**
     * Given a raw X value, it scales it to fit within our drawing area.
     */
    public float getX(float rawX) {
        return (float) (rawX * xScale + xTranslation);
    }

    /**
     * Given a raw Y value, it scales it to fit within our drawing area. This method also 'flips'
     * the value to be ready for drawing.
     */
    public float getY(float rawY) {
        return (float) (height - (rawY * yScale) + yTranslation);
    }

    /**
     * Given a scaled X value, it converts it back to the original raw value.
     */
    public float getRawX(float scaledX) {
        return (float) ((scaledX - xTranslation) / xScale);
    }

    /**
     * Given a scaled Y value, it converts it back to the original raw value.
     */
    public float getRawY(float scaledY) {
        return (float) ((height + yTranslation - scaledY) / yScale);
    }

    /**
     * Calculates minimum and maximum X values to represent.
     */
    private void calcMinMaxX(RectF bounds) {
        if (bounds.width() == 0) {
            // If vertical line -> expand vertical bonds
            minX = bounds.left - 1;
            maxX = bounds.right + 1;
        } else {
            minX = bounds.left;
            maxX = bounds.right;
        }
    }

    /**
     * Calculates minimum and maximum Y values to represent.
     */
    @SuppressWarnings("SameParameterValue")
    private void calcMinMaxY(RectF bounds, int numLabels, double coefficientExpansion) {
        // Expand bonds
        float minYExpanded;
        float maxYExpanded;
        float halfLabels = numLabels / 2F;
        if (bounds.height() == 0) {
            // If it's a horizontal line, expand vertical bonds
            if (bounds.bottom == 0) { // Horizontal 0 line -> expand just the top
                minYExpanded = 0;
                maxYExpanded = numLabels;
            } else if (bounds.bottom > 0) { // Positive horizontal line -> expand both without going negative
                minYExpanded = bounds.top - halfLabels;
                if (minYExpanded < 0) {
                    minYExpanded = 0;
                }
                maxYExpanded = bounds.bottom + halfLabels;
            } else { // Negative horizontal line -> expand both without going positive
                minYExpanded = bounds.top - halfLabels;
                maxYExpanded = bounds.bottom + halfLabels;
                if (maxYExpanded > 0) {
                    maxYExpanded = 0;
                }
            }
        } else {
            // If it's not an horizontal line, use the coefficient of expansion
            minYExpanded = bounds.top == 0 ? 0 : (float) (bounds.top - bounds.height() * coefficientExpansion);
            maxYExpanded = (float) (bounds.bottom + bounds.height() * coefficientExpansion);
        }
        float interval = Math.abs(maxYExpanded - minYExpanded);
        // Determine label granularity
        float granularity;
        if (interval >= 5) {
            granularity = 5;
        } else if (interval >= 2.5) {
            granularity = 1;
        } else {
            granularity = 0.5F;
        }
        // Round min and max to the closest step
        float minYRounded = round(minYExpanded, granularity, false);
        float maxYRounded = round(maxYExpanded, granularity, true);
        float roundedInterval = Math.abs(maxYRounded - minYRounded);
        // Find the smallest interval that contains the data and it's divisible by the number of labels
        float divisibleInterval = lcm(roundedInterval, granularity);
        int step = (int) (divisibleInterval / numLabels);
        float divisibleRoundedInterval;
        if (roundedInterval > 10) {
            divisibleRoundedInterval = round(step, granularity, true) * numLabels;
        } else { // Allow smaller granularity for values smaller than 10
            divisibleRoundedInterval = (float) step * numLabels;
        }
        // Calculate how much we have to expand the original rounded interval
        float increment = Math.abs(divisibleRoundedInterval - roundedInterval);
        float expandMax;
        float expandMin;
        if (increment > granularity && minYRounded != 0) {
            // If we can divide the increment in two parts we expand bottom and top
            expandMax = expandMin = increment / 2F;
        } else {
            // Expand top
            expandMax = increment;
            expandMin = 0;
        }
        // Calculate final min and max values
        minY = minYRounded - expandMin;
        maxY = maxYRounded + expandMax;
    }

    /**
     * Calculate the conversion factors to convert between raw and scaled values.
     */
    private void calculateConversionFactors(@NonNull RectF drawingArea) {
        // Padding
        final float leftPadding = drawingArea.left;
        final float topPadding = drawingArea.top;
        // xScale will compress or expand the min and max x values to be just inside the view
        this.xScale = width / (maxX - minX);
        // xTranslation will move the x points back between 0 - width
        this.xTranslation = leftPadding - (minX * xScale);
        // yScale will compress or expand the min and max y values to be just inside the view
        this.yScale = height / (maxY - minY);
        // yTranslation will move the y points back between 0 - height
        this.yTranslation = minY * yScale + topPadding;
    }
}
