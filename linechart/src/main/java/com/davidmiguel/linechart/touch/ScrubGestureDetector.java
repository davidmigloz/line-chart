/**
 * Copyright (C) 2016 Robinhood Markets, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.davidmiguel.linechart.touch;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * Exposes simple methods for detecting scrub events.
 */
public class ScrubGestureDetector implements View.OnTouchListener {

    static final long LONG_PRESS_TIMEOUT_MS = 50;

    private final ScrubListener scrubListener;
    private final float touchSlop;
    private final Handler handler;

    /**
     * If scrub is enable or not
     */
    private boolean enabled;
    /**
     * X coordinate where the user pressed down.
     */
    private float downX;
    /**
     * Y coordinate where the user pressed down.
     */
    private float downY;

    public ScrubGestureDetector(@NonNull ScrubListener scrubListener,
                                @NonNull Handler handler, float touchSlop) {
        this.scrubListener = scrubListener;
        this.handler = handler;
        this.touchSlop = touchSlop;
    }

    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            scrubListener.onScrubbed(downX, downY);
        }
    };

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (!enabled) {
            return false;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return onPressedDown(event);
            case MotionEvent.ACTION_MOVE:
                return onMoved(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                return onReleased();
            default:
                return false;
        }
    }

    /**
     * Invoked when the user presses down.
     *
     * @param event motion event.
     * @return if the touch event was consumed or not.
     */
    private boolean onPressedDown(@NonNull MotionEvent event) {
        // Store where the user pressed down
        downX = event.getX();
        downY = event.getY();
        // If the user doesn't release the finger before the threshold, onScrubbed is called
        handler.postDelayed(longPressRunnable, LONG_PRESS_TIMEOUT_MS);
        return true;
    }

    /**
     * Invoked when the user moves his finger.
     *
     * @param event motion event.
     * @return if the touch event was consumed or not.
     */
    private boolean onMoved(@NonNull MotionEvent event) {
        if (isLongPress(event.getEventTime(), event.getDownTime())) {
            // onScrubbed is called with the new position
            handler.removeCallbacks(longPressRunnable);
            scrubListener.onScrubbed(event.getX(), event.getY());
        } else if (isOutsideTouchSlop(downX, event.getX(), downY, event.getY())) {
            // If MOVE event exceeds tap slop and is not considered long-press,
            // the series of events are ignored
            handler.removeCallbacks(longPressRunnable);
            return false;
        }
        return true;
    }

    /**
     * Invoked when the user releases his finger.
     *
     * @return if the touch event was consumed or not.
     */
    private boolean onReleased() {
        handler.removeCallbacks(longPressRunnable);
        scrubListener.onScrubEnded();
        return true;
    }

    /**
     * Determines whether a touch event can be consider long touch or not.
     */
    private boolean isLongPress(float eventTime, float downTime) {
        return eventTime - downTime >= LONG_PRESS_TIMEOUT_MS;
    }

    /**
     * Determines whether a touch is outside the touch slop (distance a touch can wander before
     * we think the user is scrolling).
     */
    private boolean isOutsideTouchSlop(float startX, float endX, float startY, float endY) {
        return Math.abs(startX - endX) >= touchSlop || Math.abs(startY - endY) >= touchSlop;
    }

    public interface ScrubListener {

        /**
         * Invoked when user scrubbed in the given position after the long press threshold.
         *
         * @param x X coordinate
         * @param y Y coordinate
         */
        void onScrubbed(float x, float y);


        /**
         * Invoked when the user stops scrubling.
         */
        void onScrubEnded();
    }
}

