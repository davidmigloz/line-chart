package com.davidmiguel.linechart.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

import com.davidmiguel.linechart.LineChartView;

import java.util.List;

/**
 * Animates each point vertically from the previous position to the current position.
 */
public class MorphLineChartAnimator extends Animator implements LineChartAnimator {

    private final ValueAnimator animator;
    private @Nullable
    List<Float> oldYPoints;
    private Path animationPath;

    public MorphLineChartAnimator() {
        animator = ValueAnimator.ofFloat(0, 1);
        animationPath = new Path();
    }

    @Nullable
    @Override
    public Animator getAnimation(final LineChartView lineChartView) {

        final List<Float> xPoints = lineChartView.getXPoints();
        final List<Float> yPoints = lineChartView.getYPoints();

        if (xPoints.isEmpty() || yPoints.isEmpty()) {
            return null;
        }

        animator.addUpdateListener(animation -> {

            float animatedValue = (float) animation.getAnimatedValue();

            animationPath.reset();

            float step;
            float y, oldY;
            int size = xPoints.size();
            for (int count = 0; count < size; count++) {

                // get oldY, can be 0 (zero) if current points are larger
                oldY = oldYPoints != null && oldYPoints.size() > count ? oldYPoints.get(count) : 0f;

                step = yPoints.get(count) - oldY;
                y = (step * animatedValue) + oldY;

                if (count == 0) {
                    animationPath.moveTo(xPoints.get(count), y);
                } else {
                    animationPath.lineTo(xPoints.get(count), y);
                }

            }

            // set the updated path for the animation
            lineChartView.setAnimationPath(animationPath);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                oldYPoints = yPoints;
            }
        });

        return animator;
    }

    @Override
    public long getStartDelay() {
        return animator.getStartDelay();
    }

    @Override
    public void setStartDelay(@IntRange(from = 0) long startDelay) {
        animator.setStartDelay(startDelay);
    }

    @Override
    public Animator setDuration(@IntRange(from = 0) long duration) {
        return animator;
    }

    @Override
    public long getDuration() {
        return animator.getDuration();
    }

    @Override
    public void setInterpolator(@Nullable TimeInterpolator timeInterpolator) {
        animator.setInterpolator(timeInterpolator);
    }

    @Override
    public boolean isRunning() {
        return animator.isRunning();
    }
}
