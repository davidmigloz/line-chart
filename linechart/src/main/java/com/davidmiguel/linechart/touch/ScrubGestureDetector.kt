package com.davidmiguel.linechart.touch

import android.annotation.SuppressLint
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs

private const val LONG_PRESS_TIMEOUT_MS = 50L

/**
 * Exposes simple methods for detecting scrub events.
 */
class ScrubGestureDetector(
    private val scrubListener: ScrubListener,
    private val handler: Handler, private val touchSlop: Float
) : OnTouchListener {

    /**
     * If scrub is enable or not
     */
    var enabled = false

    /**
     * X coordinate where the user pressed down.
     */
    private var downX = 0f

    /**
     * Y coordinate where the user pressed down.
     */
    private var downY = 0f

    private val longPressRunnable = Runnable { scrubListener.onScrubbed(downX, downY) }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return if (!enabled) false
        else when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onPressedDown(event)
            MotionEvent.ACTION_MOVE -> onMoved(event)
            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> onReleased()
            else -> false
        }
    }

    /**
     * Invoked when the user presses down.
     *
     * @param event motion event.
     * @return if the touch event was consumed or not.
     */
    private fun onPressedDown(event: MotionEvent): Boolean {
        // Store where the user pressed down
        downX = event.x
        downY = event.y
        // If the user doesn't release the finger before the threshold, onScrubbed is called
        handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS)
        return true
    }

    /**
     * Invoked when the user moves his finger.
     *
     * @param event motion event.
     * @return if the touch event was consumed or not.
     */
    private fun onMoved(event: MotionEvent): Boolean {
        if (isLongPress(event.eventTime, event.downTime)) {
            // onScrubbed is called with the new position
            handler.removeCallbacks(longPressRunnable)
            scrubListener.onScrubbed(event.x, event.y)
        } else if (isOutsideTouchSlop(downX, event.x, downY, event.y)) {
            // If MOVE event exceeds tap slop and is not considered long-press,
            // the series of events are ignored
            handler.removeCallbacks(longPressRunnable)
            return false
        }
        return true
    }

    /**
     * Invoked when the user releases his finger.
     *
     * @return if the touch event was consumed or not.
     */
    private fun onReleased(): Boolean {
        handler.removeCallbacks(longPressRunnable)
        scrubListener.onScrubEnded()
        return true
    }

    /**
     * Determines whether a touch event can be consider long touch or not.
     */
    private fun isLongPress(eventTime: Long, downTime: Long): Boolean {
        return eventTime - downTime >= LONG_PRESS_TIMEOUT_MS
    }

    /**
     * Determines whether a touch is outside the touch slop (distance a touch can wander before
     * we think the user is scrolling).
     */
    private fun isOutsideTouchSlop(startX: Float, endX: Float, startY: Float, endY: Float): Boolean {
        return abs(startX - endX) >= touchSlop || abs(startY - endY) >= touchSlop
    }

    interface ScrubListener {
        /**
         * Invoked when user scrubbed in the given position after the long press threshold.
         *
         * @param x X coordinate
         * @param y Y coordinate
         */
        fun onScrubbed(x: Float, y: Float)

        /**
         * Invoked when the user stops scrubling.
         */
        fun onScrubEnded()
    }
}
