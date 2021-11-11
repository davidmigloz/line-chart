package com.davidmiguel.linechart.model

import android.graphics.Paint
import android.graphics.RectF

/**
 * Encapsulates data to draw a label.
 */
data class Label(
    val background: RectF,
    val textX: Float,
    val textY: Float,
    val text: String,
    val labelBackgroundPaint: Paint,
    val labelTextPaint: Paint,
)
