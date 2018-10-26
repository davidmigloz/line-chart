package com.davidmiguel.linechart.touch;

import android.support.annotation.Nullable;

/**
 * Listener for a user scrubbing (dragging their finger along) the graph.
 */
public interface OnScrubListener {

    /**
     * Indicates the user is currently scrubbing over the given value.
     * A null value indicates that the user has stopped scrubbing.
     */
    void onScrubbed(@Nullable Object value);
}
