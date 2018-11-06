package com.davidmiguel.linechart.animation;

import android.animation.Animator;

import com.davidmiguel.linechart.LineChartView;

import androidx.annotation.Nullable;

/**
 * This interface is for animate SparkView when it changes
 */
public interface LineChartAnimator {

    /**
     * Returns an Animator that performs the desired animation.
     * Must call LineChart#setAnimationPath for each animation frame.
     *
     * @param lineChartView The LineChart instance
     */
    @Nullable
    Animator getAnimation(final LineChartView lineChartView);
}
