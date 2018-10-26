package com.davidmiguel.linechart;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;

import com.davidmiguel.linechart.animation.LineChartAnimator;
import com.davidmiguel.linechart.touch.OnScrubListener;
import com.davidmiguel.linechart.touch.ScrubGestureDetector;
import com.davidmiguel.linechart.utils.ScaleHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class LineChartView extends View implements LineChart, ScrubGestureDetector.ScrubListener {

    // Styleable properties
    @ColorInt
    private int lineColor;
    private float lineWidth;
    private float cornerRadius;

    @LineChartFillType
    private int fillType = LineChartFillType.NONE;
    @ColorInt
    private int fillColor;

    @ColorInt
    private int baseLineColor;
    private float baseLineWidth;

    private boolean scrubEnabled;
    @ColorInt
    private int scrubLineColor;
    private float scrubLineWidth;

    // Canvas
    private Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint baseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint scrubLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path renderPath = new Path();
    private final Path linePath = new Path();
    private final Path baseLinePath = new Path();
    private final Path scrubLinePath = new Path();
    private ScaleHelper scaleHelper;
    private ScrubGestureDetector scrubGestureDetector;
    @Nullable
    private OnScrubListener scrubListener;
    @Nullable
    private Animator pathAnimator;
    @Nullable
    private LineChartAnimator lineChartAnimator;

    // Data
    private List<Float> xPoints;
    private List<Float> yPoints;
    private final RectF contentRect = new RectF();
    @Nullable
    private LineChartAdapter adapter;

    public LineChartView(Context context) {
        super(context);
        init(context, null, R.attr.linechart_LineChartViewStyle, R.style.linechart_LineChartView);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.linechart_LineChartViewStyle, R.style.linechart_LineChartView);
    }

    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, R.style.linechart_LineChartView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public LineChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Initialises the view.
     */
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        processAttributes(context, attrs, defStyleAttr, defStyleRes);
        configPaintStyles();
        configGestureDetector(context);
        configDataStructure();
        configEditMode();
    }

    /**
     * Reads the xml attributes and configures the view based on them.
     */
    private void processAttributes(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LineChartView, defStyleAttr, defStyleRes);

        lineColor = a.getColor(R.styleable.LineChartView_linechart_lineColor, 0);
        lineWidth = a.getDimension(R.styleable.LineChartView_linechart_lineWidth, 0);
        cornerRadius = a.getDimension(R.styleable.LineChartView_linechart_cornerRadius, 0);

        setFillType(a.getInt(R.styleable.LineChartView_linechart_fillType, LineChartFillType.NONE));
        fillColor = a.getColor(R.styleable.LineChartView_linechart_fillColor, 0);

        baseLineColor = a.getColor(R.styleable.LineChartView_linechart_baseLineColor, 0);
        baseLineWidth = a.getDimension(R.styleable.LineChartView_linechart_baseLineWidth, 0);

        scrubEnabled = a.getBoolean(R.styleable.LineChartView_linechart_scrubEnabled, true);
        scrubLineColor = a.getColor(R.styleable.LineChartView_linechart_scrubLineColor, baseLineColor);
        scrubLineWidth = a.getDimension(R.styleable.LineChartView_linechart_scrubLineWidth, lineWidth);
        a.recycle();
    }

    /**
     * Configures how the different elements of the chart have to be painted.
     */
    private void configPaintStyles() {
        // Line
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(lineColor);
        linePaint.setStrokeWidth(lineWidth);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        if (cornerRadius != 0) {
            linePaint.setPathEffect(new CornerPathEffect(cornerRadius));
        }
        // Fill
        fillPaint.set(linePaint);
        fillPaint.setColor(fillColor);
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setStrokeWidth(0);
        // Base line
        baseLinePaint.setStyle(Paint.Style.STROKE);
        baseLinePaint.setColor(baseLineColor);
        baseLinePaint.setStrokeWidth(baseLineWidth);
        // Scrub line
        scrubLinePaint.setStyle(Paint.Style.STROKE);
        scrubLinePaint.setStrokeWidth(scrubLineWidth);
        scrubLinePaint.setColor(scrubLineColor);
        scrubLinePaint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * Configures the gesture detector to listen to user touches.
     */
    private void configGestureDetector(@NonNull Context context) {
        scrubGestureDetector = new ScrubGestureDetector(this,
                new Handler(Looper.getMainLooper()),
                ViewConfiguration.get(context).getScaledTouchSlop());
        scrubGestureDetector.setEnabled(scrubEnabled);
        setOnTouchListener(scrubGestureDetector);
    }

    /**
     * Configures the data structure where the chart data will be stored.
     */
    private void configDataStructure() {
        xPoints = new ArrayList<>();
        yPoints = new ArrayList<>();
    }

    /**
     * Configures the mock preview displayed in the layout editor.
     */
    private void configEditMode() {
        if (!isInEditMode()) {
            return;
        }
        setAdapter(new LineChartAdapter() {
            private final float[] yData = new float[]{68, 22, 31, 57, 35, 79, 86, 47, 34, 55, 80, 72, 99, 66, 47, 42, 56, 64, 66, 80, 97, 10, 43, 12, 25, 71, 47, 73, 49, 36};

            @Override
            public int getCount() {
                return yData.length;
            }

            @NonNull
            @Override
            public Object getItem(int index) {
                return yData[index];
            }

            @Override
            public float getY(int index) {
                return yData[index];
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateContentRect();
        populatePath();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        updateContentRect();
        populatePath();
    }

    /**
     * Gets the rect representing the 'content area' of the view. This is essentially the bounding
     * rect minus any padding.
     */
    private void updateContentRect() {
        contentRect.set(getPaddingStart(), getPaddingTop(),
                getWidth() - getPaddingEnd(),
                getHeight() - getPaddingBottom());
    }

    /**
     * Populates the path with points.
     */
    private void populatePath() {
        if (adapter == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }

        final int adapterCount = adapter.getCount();

        // To draw anything, we need 2 or more points
        if (adapterCount < 2) {
            clearData();
            return;
        }

        scaleHelper = new ScaleHelper(adapter, contentRect, lineWidth, isFill());

        xPoints.clear();
        yPoints.clear();

        // Make our main graph path
        linePath.reset();
        for (int i = 0; i < adapterCount; i++) {
            final float x = scaleHelper.getX(adapter.getX(i));
            final float y = scaleHelper.getY(adapter.getY(i));

            // points to render graphic
            // get points to animate
            xPoints.add(x);
            yPoints.add(y);

            if (i == 0) {
                linePath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
            }
        }

        // if we're filling the graph in, close the path's circuit
        final Float fillEdge = getFillEdge();
        if (fillEdge != null) {
            final float lastX = scaleHelper.getX(adapter.getCount() - 1);
            // line up or down to the fill edge
            linePath.lineTo(lastX, fillEdge);
            // line straight left to far edge of the view
            linePath.lineTo(getPaddingStart(), fillEdge);
            // closes line back on the first point
            linePath.close();
        }

        // Make our base line path
        baseLinePath.reset();
        if (adapter.hasBaseLine()) {
            float scaledBaseLine = scaleHelper.getY(adapter.getBaseLine());
            baseLinePath.moveTo(0, scaledBaseLine);
            baseLinePath.lineTo(getWidth(), scaledBaseLine);
        }

        renderPath.reset();
        renderPath.addPath(linePath);

        invalidate();
    }

    /**
     * Clears data to draw.
     */
    private void clearData() {
        scaleHelper = null;
        renderPath.reset();
        linePath.reset();
        baseLinePath.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPath(baseLinePath, baseLinePaint);
        if (fillType != LineChartFillType.NONE) {
            canvas.drawPath(renderPath, fillPaint);
        }
        canvas.drawPath(renderPath, linePaint);
        canvas.drawPath(scrubLinePath, scrubLinePaint);
    }

    @Nullable
    @Override
    public LineChartAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void setAdapter(@Nullable LineChartAdapter adapter) {
        if (this.adapter != null) {
            this.adapter.unregisterDataSetObserver(dataSetObserver);
        }
        this.adapter = adapter;
        if (this.adapter != null) {
            this.adapter.registerDataSetObserver(dataSetObserver);
        }
        populatePath();
    }

    @ColorInt
    @Override
    public int getLineColor() {
        return lineColor;
    }

    @Override
    public void setLineColor(@ColorInt int lineColor) {
        this.lineColor = lineColor;
        linePaint.setColor(lineColor);
        invalidate();
    }

    @Override
    public float getLineWidth() {
        return lineWidth;
    }

    @Override
    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
        linePaint.setStrokeWidth(lineWidth);
        invalidate();
    }

    @Override
    public float getCornerRadius() {
        return cornerRadius;
    }

    @Override
    public void setCornerRadius(float cornerRadius) {
        this.cornerRadius = cornerRadius;
        if (cornerRadius != 0) {
            linePaint.setPathEffect(new CornerPathEffect(cornerRadius));
            fillPaint.setPathEffect(new CornerPathEffect(cornerRadius));
        } else {
            linePaint.setPathEffect(null);
            fillPaint.setPathEffect(null);
        }
        invalidate();
    }

    @LineChartFillType
    @Override
    public int getFillType() {
        return fillType;
    }

    @Override
    public void setFillType(int fillType) {
        if (this.fillType != fillType) {
            this.fillType = fillType;
            populatePath();
        }
    }

    /**
     * Return whether or not this line chart should fill the area underneath.
     */
    private boolean isFill() {
        switch (fillType) {
            case LineChartFillType.NONE:
                return false;
            case LineChartFillType.UP:
            case LineChartFillType.DOWN:
            case LineChartFillType.TOWARD_ZERO:
                return true;
            default:
                throw new IllegalStateException(
                        String.format(Locale.US, "Unknown fill-type: %d", fillType)
                );
        }
    }

    /**
     * Returns the fill edge relative to the fill type.
     */
    @Nullable
    private Float getFillEdge() {
        switch (fillType) {
            case LineChartFillType.NONE:
                return null;
            case LineChartFillType.UP:
                return (float) getPaddingTop();
            case LineChartFillType.DOWN:
                return (float) getHeight() - getPaddingBottom();
            case LineChartFillType.TOWARD_ZERO:
                float zero = scaleHelper.getY(0F);
                float bottom = (float) getHeight() - getPaddingBottom();
                return Math.min(zero, bottom);
            default:
                throw new IllegalStateException(
                        String.format(Locale.US, "Unknown fill-type: %d", fillType)
                );
        }
    }

    @ColorInt
    @Override
    public int getFillColor() {
        return fillColor;
    }

    @Override
    public void setFillColor(@ColorInt int fillColor) {
        this.fillColor = fillColor;
        fillPaint.setColor(fillColor);
        invalidate();
    }

    @ColorInt
    @Override
    public int getBaseLineColor() {
        return baseLineColor;
    }

    @Override
    public void setBaseLineColor(@ColorInt int baseLineColor) {
        this.baseLineColor = baseLineColor;
        baseLinePaint.setColor(baseLineColor);
        invalidate();
    }

    @Override
    public float getBaseLineWidth() {
        return baseLineWidth;
    }

    @Override
    public void setBaseLineWidth(float baseLineWidth) {
        this.baseLineWidth = baseLineWidth;
        baseLinePaint.setStrokeWidth(baseLineWidth);
        invalidate();
    }

    public boolean isScrubEnabled() {
        return scrubEnabled;
    }

    public void setScrubEnabled(boolean scrubbingEnabled) {
        this.scrubEnabled = scrubbingEnabled;
        scrubGestureDetector.setEnabled(scrubbingEnabled);
        invalidate();
    }

    @ColorInt
    @Override
    public int getScrubLineColor() {
        return scrubLineColor;
    }

    @Override
    public void setScrubLineColor(@ColorInt int scrubLineColor) {
        this.scrubLineColor = scrubLineColor;
        scrubLinePaint.setColor(scrubLineColor);
        invalidate();
    }

    @Override
    public float getScrubLineWidth() {
        return scrubLineWidth;
    }

    @Override
    public void setScrubLineWidth(float scrubLineWidth) {
        this.scrubLineWidth = scrubLineWidth;
        scrubLinePaint.setStrokeWidth(scrubLineWidth);
        invalidate();
    }

    @Nullable
    @Override
    public OnScrubListener getScrubListener() {
        return scrubListener;
    }

    @Override
    public void setScrubListener(@Nullable OnScrubListener scrubListener) {
        this.scrubListener = scrubListener;
    }

    @Nullable
    @Override
    public LineChartAnimator getLineChartAnimator() {
        return lineChartAnimator;
    }

    @Override
    public void setLineChartAnimator(@Nullable LineChartAnimator lineChartAnimator) {
        this.lineChartAnimator = lineChartAnimator;
    }

    @Override
    public void setAnimationPath(@NonNull Path animationPath) {
        this.renderPath.reset();
        this.renderPath.addPath(animationPath);
        this.renderPath.rLineTo(0, 0);
        invalidate();
    }

    @NonNull
    @Override
    public List<Float> getXPoints() {
        return new ArrayList<>(xPoints);
    }

    @NonNull
    @Override
    public List<Float> getYPoints() {
        return new ArrayList<>(yPoints);
    }

    @Override
    public void onScrubbed(float x, float y) {
        if (adapter == null || adapter.getCount() == 0) {
            return;
        }
        if (scrubListener != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
            int index = getNearestIndex(xPoints, x);
            if (scrubListener != null) {
                scrubListener.onScrubbed(adapter.getItem(index));
            }
        }
        paintScrubLine(x);
    }

    /**
     * Returns the nearest index (into {@link #adapter}'s data) for the given x coordinate.
     */
    private int getNearestIndex(List<Float> points, float x) {
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
     * Paints scrub line.
     */
    private void paintScrubLine(float x) {
        x = resolveBoundedScrubLine(x);
        scrubLinePath.reset();
        scrubLinePath.moveTo(x, getPaddingTop());
        scrubLinePath.lineTo(x, getHeight() - getPaddingBottom());
        invalidate();
    }

    /**
     * Bounds the x coordinate of a scrub within the bounding rect minus padding and line width.
     */
    private float resolveBoundedScrubLine(float x) {
        float scrubLineOffset = scrubLineWidth / 2;

        float leftBound = getPaddingStart() + scrubLineOffset;
        if (x < leftBound) {
            return leftBound;
        }

        float rightBound = getWidth() - getPaddingEnd() - scrubLineOffset;
        if (x > rightBound) {
            return rightBound;
        }

        return x;
    }

    @Override
    public void onScrubEnded() {
        scrubLinePath.reset();
        if (scrubListener != null) {
            scrubListener.onScrubbed(null);
        }
        invalidate();
    }

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            populatePath();
            if (lineChartAnimator != null) {
                doPathAnimation();
            }
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            clearData();
        }
    };

    /**
     * Performs the path animation.
     */
    private void doPathAnimation() {
        if (pathAnimator != null) {
            pathAnimator.cancel();
        }

        pathAnimator = getAnimator();

        if (pathAnimator != null) {
            pathAnimator.start();
        }
    }

    /**
     * Gets the Animator from the LineChartAnimator.
     */
    @Nullable
    private Animator getAnimator() {
        return lineChartAnimator != null ? lineChartAnimator.getAnimation(this) : null;
    }
}
