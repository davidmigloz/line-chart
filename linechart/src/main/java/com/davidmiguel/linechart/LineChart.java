package com.davidmiguel.linechart;

import android.graphics.Path;

import com.davidmiguel.linechart.animation.LineChartAnimator;
import com.davidmiguel.linechart.touch.OnScrubListener;

import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public interface LineChart {

    /**
     * Get the backing {@link LineChartAdapter}.
     */
    @Nullable
    LineChartAdapter getAdapter();

    /**
     * Sets the backing {@link LineChartAdapter} to generate the points to be graphed.
     */
    void setAdapter(@Nullable LineChartAdapter adapter);

    /**
     * Gets the color of the line.
     */
    @ColorInt
    int getLineColor();

    /**
     * Sets the color of the line.
     */
    void setLineColor(@ColorInt int lineColor);

    /**
     * Gets the width in pixels of the line's stroke.
     */
    float getLineWidth();

    /**
     * Sets the width in pixels of the line's stroke.
     */
    void setLineWidth(float lineWidth);

    /**
     * Gets the corner radius in pixels used when rounding the line's segments.
     */
    float getCornerRadius();

    /**
     * Sets the corner radius in pixels to use when rounding the line's segments.
     * Passing 0 indicates that corners should not be rounded.
     */
    void setCornerRadius(float cornerRadius);

    /**
     * Gets how the chart has to be filled if fill is enabled.
     */
    @LineChartFillType
    int getFillType();

    /**
     * Sets how the chart has to be filled if fill is enabled.
     */
    void setFillType(@LineChartFillType int fillType);

    /**
     * Gets the color of the filling.
     */
    @ColorInt
    int getFillColor();

    /**
     * Sets the color of the filling.
     */
    void setFillColor(@ColorInt int fillColor);

    /**
     * Gets the color of the grid line.
     */
    @ColorInt
    int getGridLineColor();

    /**
     * Sets the color of the base line.
     */
    void setGridLineColor(@ColorInt int gridLineColor);

    /**
     * Gets the width in pixels of the grid line's stroke.
     */
    float getGridLineWidth();

    /**
     * Sets the width in pixels of the grid line's stroke.
     */
    void setGridLineWidth(float gridLineWidth);

    /**
     * Gets the number of grid divisions in X axis.
     */
    int getGridXDivisions();

    /**
     * Sets the number of grid divisions in X axis.
     */
    void setGridXDivisions(int gridXDivisions);

    /**
     * Gets the number of grid divisions in Y axis.
     */
    int getGridYDivisions();

    /**
     * Sets the number of grid divisions in Y axis.
     */
    void setGridYDivisions(int gridYDivisions);

    /**
     * Gets the color of the base line.
     */
    @ColorInt
    int getBaseLineColor();

    /**
     * Sets the color of the base line.
     */
    void setBaseLineColor(@ColorInt int baseLineColor);

    /**
     * Gets the width in pixels of the base line's stroke.
     */
    float getBaseLineWidth();

    /**
     * Sets the width in pixels of the base line's stroke.
     */
    void setBaseLineWidth(float baseLineWidth);

    /**
     * Returns true if scrubbing is enabled on this view.
     */
    boolean isScrubEnabled();

    /**
     * Sets whether or not to enable scrubbing on this view.
     */
    void setScrubEnabled(boolean scrubbingEnabled);

    /**
     * Gets the color of the scrub line.
     */
    @ColorInt
    int getScrubLineColor();

    /**
     * Sets the color of the scrub line.
     */
    void setScrubLineColor(@ColorInt int scrubLineColor);

    /**
     * Gets the width in pixels of the scrub line's stroke.
     */
    float getScrubLineWidth();

    /**
     * Set the width in pixels of the scrub line's stroke.
     */
    void setScrubLineWidth(float scrubLineWidth);

    /**
     * Gets the current {@link OnScrubListener}.
     */
    @Nullable
    OnScrubListener getScrubListener();

    /**
     * Sets a {@link OnScrubListener} to be notified of the user's scrubbing gestures.
     */
    void setScrubListener(@Nullable OnScrubListener scrubListener);

    /**
     * Gets the Animator class used to animate the Line Chart.
     */
    @Nullable
    LineChartAnimator getLineChartAnimator();

    /**
     * Animator class to animate the Line Chart.
     */
    void setLineChartAnimator(@Nullable LineChartAnimator lineChartAnimator);

    /**
     * Sets the path to animate in onDraw, used by animators.
     */
    void setAnimationPath(@NonNull Path animationPath);

    /**
     * Returns a copy of current graphic X points.
     */
    @NonNull
    List<Float> getXPoints();

    /**
     * Returns a copy of current graphic Y points.
     */
    @NonNull
    List<Float> getYPoints();
}
