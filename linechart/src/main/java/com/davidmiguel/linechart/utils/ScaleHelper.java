package com.davidmiguel.linechart.utils;

import android.graphics.RectF;

import com.davidmiguel.linechart.LineChartAdapter;

import androidx.annotation.NonNull;

/**
 * Helper class for handling scaling logic.
 */
@SuppressWarnings("FieldCanBeLocal")
public class ScaleHelper {

    // Width and height of the view
    private final float width;
    private final float height;
    // Scale factor
    private final float xScale;
    private final float yScale;
    // Translation value
    private final float xTranslation;
    private final float yTranslation;

    public ScaleHelper(@NonNull LineChartAdapter adapter,
                       @NonNull RectF contentRect,
                       float lineWidth, boolean fill) {
        // Subtract lineWidth to offset for 1/2 of the line bleeding out of the content box on either
        // side of the view
        final float lineWidthOffset = fill ? 0 : lineWidth;
        this.width = contentRect.width() - lineWidthOffset;
        this.height = contentRect.height() - lineWidthOffset;

        // Get data bounds from adapter
        RectF bounds = adapter.getDataBounds();

        // If data is a line (which technically has no size), expand bounds to center the data
        bounds.inset(bounds.width() == 0 ? -1 : 0, bounds.height() == 0 ? -1 : 0);

        final float minX = bounds.left;
        final float maxX = bounds.right;
        final float minY = bounds.top == 0 ? 0 : (float) (bounds.top - bounds.height() * 0.1);
        final float maxY = (float) (bounds.bottom + bounds.height() * 0.1);
        final float leftPadding = contentRect.left;
        final float topPadding = contentRect.top;

        // xScale will compress or expand the min and max x values to be just inside the view
        this.xScale = width / (maxX - minX);
        // xTranslation will move the x points back between 0 - width
        this.xTranslation = leftPadding - (minX * xScale) + (lineWidthOffset / 2);
        // yScale will compress or expand the min and max y values to be just inside the view
        this.yScale = height / (maxY - minY);
        // yTranslation will move the y points back between 0 - height
        this.yTranslation = minY * yScale + topPadding + (lineWidthOffset / 2);
    }

    /**
     * Given the 'raw' X value, scale it to fit within our view.
     */
    public float getX(float rawX) {
        return rawX * xScale + xTranslation;
    }

    /**
     * Given the 'raw' Y value, scale it to fit within our view. This method also 'flips' the
     * value to be ready for drawing.
     */
    public float getY(float rawY) {
        return height - (rawY * yScale) + yTranslation;
    }

    public float getRawX(float scaledX) {
        return (scaledX - xTranslation) / xScale;
    }

    public float getRawY(float scaledY) {
        return (height + yTranslation - scaledY) / yScale;
    }
}
