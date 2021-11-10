package com.davidmiguel.linechart.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor

@Suppress("MemberVisibilityCanBePrivate")
object Utils {

    /**
     * Returns the nearest index into the list of data for the given x coordinate.
     */
    fun getNearestIndex(points: List<Float>, x: Float): Int {
        var index = Collections.binarySearch(points, x)

        // if binary search returns positive, we had an exact match, return that index
        if (index >= 0) return index

        // otherwise, calculate the binary search's specified insertion index
        index = -1 - index

        // if we're inserting at 0, then our guaranteed nearest index is 0
        if (index == 0) return index

        // if we're inserting at the very end, then our guaranteed nearest index is the final one
        if (index == points.size) return --index

        // otherwise we need to check which of our two neighbors we're closer to
        val deltaUp = points[index] - x
        val deltaDown = x - points[index - 1]
        if (deltaUp > deltaDown) {
            // if the below neighbor is closer, decrement our index
            index--
        }
        return index
    }

    /**
     * Decodes drawable into a bitmap.
     */
    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        var drawable = ContextCompat.getDrawable(context, drawableId) ?: error("Invalid drawable")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = DrawableCompat.wrap(drawable).mutate()
        }
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Least common multiple (LCM).
     */
    fun lcm(a: Float, b: Float): Float {
        return a * (b / gcd(a, b))
    }

    /**
     * Greatest Common Divisor (GCD).
     */
    fun gcd(n1: Float, n2: Float): Float {
        var a = n1
        var b = n2
        while (b > 0) {
            val temp = b
            b = a % b // % is remainder
            a = temp
        }
        return a
    }

    /**
     * Rounds number to the nearest multiple of the given multipleOf.
     *
     * @param number     number to round.
     * @param multipleOf multiple used in the rounding.
     * @param roundUp    if true ceil rounding if false floor rounding.
     */
    fun round(number: Float, multipleOf: Float, roundUp: Boolean): Float {
        return if (roundUp) {
            (ceil((number / multipleOf).toDouble()) * multipleOf).toFloat()
        } else {
            floor((number / multipleOf).toDouble()).toFloat() * multipleOf
        }
    }
}
