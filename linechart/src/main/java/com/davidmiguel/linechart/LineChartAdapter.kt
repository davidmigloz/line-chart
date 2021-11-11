package com.davidmiguel.linechart

import android.database.DataSetObservable
import android.database.DataSetObserver
import android.graphics.RectF
import androidx.annotation.VisibleForTesting

/**
 * A simple adapter class - evenly distributes your points along the x axis, does not draw a base
 * line, and has support for registering/notifying [DataSetObserver]s when data is changed.
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class LineChartAdapter {

    private val observable = DataSetObservable()
    private var dataBounds: RectF? = null

    /**
     * @return the number of points to be drawn.
     */
    abstract val count: Int

    /**
     * @return the object at the given index.
     */
    abstract fun getItem(index: Int): Any

    /**
     * @return the float representation of the X value of the point at the given index.
     */
    fun getX(index: Int): Float = index.toFloat()

    /**
     * @return the float representation of the Y value of the point at the given index.
     */
    abstract fun getY(index: Int): Float

    /**
     * Gets the float representation of the boundaries of the entire dataset. By default, this will
     * be the min and max of the actual data points in the adapter. This can be overridden for
     * custom behavior. When overriding, make sure to set RectF's values such that:
     *
     *
     *  * left = the minimum X value
     *  * top = the minimum Y value
     *  * right = the maximum X value
     *  * bottom = the maximum Y value
     *
     *
     * @return a RectF of the bounds desired around this adapter's data.
     */
    fun getDataBounds(): RectF {
        return dataBounds ?: run {
            val count = count
            var minY = if (hasBaseLine) baseLine!! else Float.MAX_VALUE
            var maxY = if (hasBaseLine) minY else -Float.MAX_VALUE
            var minX = Float.MAX_VALUE
            var maxX = -Float.MAX_VALUE
            for (i in 0 until count) {
                val x = getX(i)
                minX = minX.coerceAtMost(x)
                maxX = maxX.coerceAtLeast(x)
                val y = getY(i)
                minY = minY.coerceAtMost(y)
                maxY = maxY.coerceAtLeast(y)
            }
            createRectF(minX, minY, maxX, maxY).apply { dataBounds = this }
        }
    }

    /**
     * Hook for unit tests
     */
    @VisibleForTesting
    fun createRectF(left: Float, top: Float, right: Float, bottom: Float): RectF {
        return RectF(left, top, right, bottom)
    }

    /**
     * @return true if you wish to draw a "base line" - a horizontal line across the graph used
     * to compare the rest of the graph's points against.
     */
    val hasBaseLine: Boolean
        get() = baseLine != null

    /**
     * @return the float representation of the Y value of the desired baseLine.
     */
    var baseLine: Float? = null

    /**
     * Notifies the attached observers that the underlying data has been changed and any View
     * reflecting the data set should refresh itself.
     */
    fun notifyDataSetChanged() {
        dataBounds = null
        observable.notifyChanged()
    }

    /**
     * Notifies the attached observers that the underlying data is no longer valid or available.
     * Once invoked this adapter is no longer valid and should not report further data set
     * changes.
     */
    fun notifyDataSetInvalidated() {
        dataBounds = null
        observable.notifyInvalidated()
    }

    /**
     * Register a [DataSetObserver] to listen for updates to this adapter's data.
     *
     * @param observer the observer to register.
     */
    fun registerDataSetObserver(observer: DataSetObserver) {
        observable.registerObserver(observer)
    }

    /**
     * Unregister a [DataSetObserver] from updates to this adapter's data.
     *
     * @param observer the observer to unregister.
     */
    fun unregisterDataSetObserver(observer: DataSetObserver) {
        observable.unregisterObserver(observer)
    }
}
