package com.davidmiguel.linechart.touch

/**
 * Listener for a user scrubbing (dragging their finger along) the graph.
 */
fun interface OnScrubListener {

    /**
     * Indicates the user is currently scrubbing over the given value.
     * A null value indicates that the user has stopped scrubbing.
     */
    fun onScrubbed(value: Any?)
}
