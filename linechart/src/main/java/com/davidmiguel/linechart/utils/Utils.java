package com.davidmiguel.linechart.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class Utils {

    private Utils() {
        // Utility class
    }

    /**
     * Returns the nearest index into the list of data for the given x coordinate.
     */
    public static int getNearestIndex(List<Float> points, float x) {
        int index = Collections.binarySearch(points, x);

        // if binary search returns positive, we had an exact match, return that index
        if (index >= 0) return index;

        // otherwise, calculate the binary search's specified insertion index
        index = -1 - index;

        // if we're inserting at 0, then our guaranteed nearest index is 0
        if (index == 0) return index;

        // if we're inserting at the very end, then our guaranteed nearest index is the final one
        if (index == points.size()) return --index;

        // otherwise we need to check which of our two neighbors we're closer to
        final float deltaUp = points.get(index) - x;
        final float deltaDown = x - points.get(index - 1);
        if (deltaUp > deltaDown) {
            // if the below neighbor is closer, decrement our index
            index--;
        }

        return index;
    }

    /**
     * Decodes drawable into a bitmap.
     */
    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable == null) {
            throw new IllegalArgumentException("Invalid drawable");
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Least common multiple (LCM).
     */
    public static float lcm(float a, float b) {
        return a * (b / gcd(a, b));
    }

    /**
     * Greatest Common Divisor (GCD).
     */
    public static float gcd(float a, float b) {
        while (b > 0) {
            float temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    /**
     * Rounds number to the nearest multiple of the given multipleOf.
     *
     * @param number     number to round.
     * @param multipleOf multiple used in the rounding.
     * @param roundUp    if true ceil rounding if false floor rounding.
     */
    public static float round(float number, float multipleOf, boolean roundUp) {
        if (roundUp) {
            return (float) (Math.ceil((number / multipleOf)) * multipleOf);
        } else {
            return (float) Math.floor((number / multipleOf)) * multipleOf;
        }
    }
}
