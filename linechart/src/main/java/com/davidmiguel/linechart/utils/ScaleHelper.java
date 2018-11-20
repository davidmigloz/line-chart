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
                       @NonNull RectF drawingArea, int numLabels) {
        // Drawing size
        this.width = drawingArea.width();
        this.height = drawingArea.height();

        // Get data bounds from adapter
        RectF bounds = adapter.getDataBounds();

        // If it's a horizontal line, expand vertical bonds
        if (bounds.height() == 0) {
            if (bounds.bottom == 0) {
                bounds.bottom += numLabels;
            } else if (bounds.bottom > 0) {
                bounds.top = bounds.top - 1 >= 0 ? bounds.top - 1 : 0;
                bounds.bottom++;
            } else {
                bounds.inset(0, -1);
            }
        }

        // If it's a vertical line, expand horizontal bonds
        if (bounds.width() == 0) {
            bounds.inset(-1, 0);
        }

        final float minX = bounds.left;
        final float maxX = bounds.right;
        final float minY = bounds.top == 0 ? 0 : (float) (bounds.top - bounds.height() * 0.1);
        final float maxY = (float) (bounds.bottom + bounds.height() * 0.1);
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
