package com.davidmiguel.linechart.model;

import android.graphics.RectF;

/**
 * Encapsulates data to draw a label.
 */
public class Label {

    private RectF background;
    private float textX;
    private float textY;
    private String text;

    public Label(RectF background, float textX, float textY, String text) {
        this.background = background;
        this.textX = textX;
        this.textY = textY;
        this.text = text;
    }

    public RectF getBackground() {
        return background;
    }

    public float getTextX() {
        return textX;
    }

    public float getTextY() {
        return textY;
    }

    public String getText() {
        return text;
    }
}
