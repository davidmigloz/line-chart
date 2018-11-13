package com.davidmiguel.linechart;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;

import com.davidmiguel.linechart.animation.LineChartAnimator;
import com.davidmiguel.linechart.model.Label;
import com.davidmiguel.linechart.touch.OnScrubListener;
import com.davidmiguel.linechart.touch.ScrubGestureDetector;
import com.davidmiguel.linechart.utils.ScaleHelper;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.davidmiguel.linechart.utils.Utils.getBitmapFromVectorDrawable;
import static com.davidmiguel.linechart.utils.Utils.getNearestIndex;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class LineChartView extends View implements ScrubGestureDetector.ScrubListener, LineChart {

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
    private int gridLineColor;
    private float gridLineWidth;
    private int gridXDivisions;
    private int gridYDivisions;

    @ColorInt
    private int baseLineColor;
    private float baseLineWidth;

    private boolean scrubEnabled;
    @ColorInt
    private int scrubLineColor;
    private float scrubLineWidth;

    private float labelMargin;
    @ColorInt
    private int labelTextColor;
    private float labelTextSize;
    @ColorInt
    private int labelBackgroundColor;
    private float labelBackgroundRadius;
    private float labelBackgroundPaddingHorizontal;
    private float labelBackgroundPaddingVertical;

    // How to draw
    private Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint gridLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint baseLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint scrubLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint labelTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private int labelTextOriginalAlpha;
    private Paint labelBackgroundPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private int labelBackgroundOriginalAlpha;
    private NumberFormat labelFormatter = new DecimalFormat("€ #");

    // What to draw
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final Path gridLinePath = new Path();
    private final Path baseLinePath = new Path();
    private final Path scrubLinePath = new Path();
    private List<Float> gridLinesX;
    private List<Float> gridLinesY;
    private List<Label> labelsY;
    private boolean isScrubEnabled = false;
    private Bitmap scrubCursor = getBitmapFromVectorDrawable(getContext(), R.drawable.linechart_scrub_cursor);
    private float scrubCursorX;
    private float scrubCursorY;

    // Data
    @Nullable
    private LineChartAdapter adapter;
    private ScaleHelper scaleHelper;
    private List<Float> scaledXPoints; // Scaled x points
    private List<Float> scaledYPoints; // Scaled y points
    private final RectF drawingArea = new RectF();

    // Touch
    private ScrubGestureDetector scrubGestureDetector;
    @Nullable
    private OnScrubListener scrubListener;

    // Animation
    @Nullable
    private Animator pathAnimator;
    @Nullable
    private LineChartAnimator lineChartAnimator;
    ValueAnimator labelAlphaAnimator;

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
        try {
            lineColor = a.getColor(R.styleable.LineChartView_linechart_lineColor, 0);
            lineWidth = a.getDimension(R.styleable.LineChartView_linechart_lineWidth, 0);
            cornerRadius = a.getDimension(R.styleable.LineChartView_linechart_cornerRadius, 0);

            setFillType(a.getInt(R.styleable.LineChartView_linechart_fillType, LineChartFillType.NONE));
            fillColor = a.getColor(R.styleable.LineChartView_linechart_fillColor, 0);

            gridLineColor = a.getColor(R.styleable.LineChartView_linechart_gridLineColor, 0);
            gridLineWidth = a.getDimension(R.styleable.LineChartView_linechart_gridLineWidth, 0);
            gridXDivisions = a.getInteger(R.styleable.LineChartView_linechart_gridXDivisions, 0);
            gridYDivisions = a.getInteger(R.styleable.LineChartView_linechart_gridYDivisions, 0);

            baseLineColor = a.getColor(R.styleable.LineChartView_linechart_baseLineColor, 0);
            baseLineWidth = a.getDimension(R.styleable.LineChartView_linechart_baseLineWidth, 0);

            scrubEnabled = a.getBoolean(R.styleable.LineChartView_linechart_scrubEnabled, true);
            scrubLineColor = a.getColor(R.styleable.LineChartView_linechart_scrubLineColor, 0);
            scrubLineWidth = a.getDimension(R.styleable.LineChartView_linechart_scrubLineWidth, 0);

            labelMargin = a.getDimension(R.styleable.LineChartView_linechart_labelMargin, 0);
            labelTextColor = a.getColor(R.styleable.LineChartView_linechart_labelTextColor, 0);
            labelTextSize = a.getDimension(R.styleable.LineChartView_linechart_labelTextSize, 0);
            labelBackgroundColor = a.getColor(R.styleable.LineChartView_linechart_labelBackgroundColor, 0);
            labelBackgroundRadius = a.getDimension(R.styleable.LineChartView_linechart_labelBackgroundRadius, 0);
            labelBackgroundPaddingHorizontal = a.getDimension(R.styleable.LineChartView_linechart_labelBackgroundPaddingHorizontal, 0);
            labelBackgroundPaddingVertical = a.getDimension(R.styleable.LineChartView_linechart_labelBackgroundPaddingVertical, 0);
        } finally {
            a.recycle();
        }
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
        // Grid
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setColor(gridLineColor);
        gridLinePaint.setStrokeWidth(gridLineWidth);
        // Base line
        baseLinePaint.setStyle(Paint.Style.STROKE);
        baseLinePaint.setColor(baseLineColor);
        baseLinePaint.setStrokeWidth(baseLineWidth);
        // Scrub line
        scrubLinePaint.setStyle(Paint.Style.STROKE);
        scrubLinePaint.setStrokeWidth(scrubLineWidth);
        scrubLinePaint.setColor(scrubLineColor);
        scrubLinePaint.setStrokeCap(Paint.Cap.ROUND);
        // Labels
        labelTextPaint.setColor(labelTextColor);
        labelTextPaint.setTextSize(labelTextSize);
        labelTextPaint.setTextAlign(Paint.Align.LEFT);
        labelTextOriginalAlpha = labelTextPaint.getAlpha();
        labelBackgroundPaint.setColor(labelBackgroundColor);
        labelBackgroundPaint.setStyle(Paint.Style.FILL);
        labelBackgroundOriginalAlpha = labelBackgroundPaint.getAlpha();
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
        scaledXPoints = new ArrayList<>();
        scaledYPoints = new ArrayList<>();
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
        // Called when the view is first assigned a size, and again if the size changes for any reason
        // All calculations related to positions, dimensions, and any other values must be done here (not in onDraw)
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDrawingArea();
        populatePaths();
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
        calculateDrawingArea();
        populatePaths();
    }

    /**
     * Calculates the area where we can draw (essentially the bounding rect minus any padding).
     * The area is represented in a rectangle.
     */
    private void calculateDrawingArea() {
        drawingArea.set(getPaddingStart(), getPaddingTop(),
                getWidth() - getPaddingEnd(),
                getHeight() - getPaddingBottom());
    }

    /**
     * Populates the path to draw.
     */
    private void populatePaths() {
        if (adapter == null || getWidth() == 0 || getHeight() == 0) {
            return;
        }
        final int numPoints = adapter.getCount();
        // To draw anything, we need 2 or more points
        if (numPoints < 2) {
            clearData();
            return;
        }
        scaleHelper = new ScaleHelper(adapter, drawingArea, lineWidth, isFill());
        // Populate paths
        populateGrid();
        populateLine(numPoints);
        populateFilling(numPoints, linePath);
        populateBaseLine();
        populateLabels();
        invalidate();
    }

    /**
     * Populates grid path.
     */
    private void populateGrid() {
        gridLinePath.reset();
        final float gridLeft = drawingArea.left;
        final float gridBottom = drawingArea.bottom;
        final float gridTop = drawingArea.top;
        final float gridRight = drawingArea.right;
        float gridLineSpacing;
        // Grid X axis
        gridLinesX = new ArrayList<>(gridXDivisions);
        gridLineSpacing = (gridRight - gridLeft) / gridXDivisions;
        for (int i = 0; i < gridXDivisions; i++) {
            gridLinesX.add(gridLeft + i * gridLineSpacing);
            gridLinePath.moveTo(gridLinesX.get(i), gridTop);
            gridLinePath.lineTo(gridLinesX.get(i), gridBottom);
        }
        // Grid Y axis
        gridLinesY = new ArrayList<>(gridYDivisions);
        gridLineSpacing = (gridBottom - gridTop) / gridYDivisions;
        for (int i = 0; i < gridYDivisions; i++) {
            gridLinesY.add(gridTop + i * gridLineSpacing);
            gridLinePath.moveTo(gridLeft, gridLinesY.get(i));
            gridLinePath.lineTo(gridRight, gridLinesY.get(i));
        }
    }

    /**
     * Populates line of chart.
     */
    @SuppressWarnings("ConstantConditions")
    private void populateLine(int numPoints) {
        linePath.reset();
        scaledXPoints.clear();
        scaledYPoints.clear();
        for (int i = 0; i < numPoints; i++) {
            // Scale points relative to the drawing area
            final float x = scaleHelper.getX(adapter.getX(i));
            final float y = scaleHelper.getY(adapter.getY(i));
            scaledXPoints.add(x);
            scaledYPoints.add(y);
            // Populate line path
            if (i == 0) {
                linePath.moveTo(x, y);
            } else {
                linePath.lineTo(x, y);
            }
        }
    }

    /**
     * Populates the filling of the line if enabled (linePath should be already populated).
     */
    private void populateFilling(int numPoints, Path linePath) {
        fillPath.reset();
        final Float fillEdge = getFillEdge();
        if (fillEdge != null) {
            // Copy line path
            fillPath.addPath(linePath);
            // Line up or down to the fill edge
            final float lastX = scaleHelper.getX(numPoints - 1F);
            fillPath.lineTo(lastX, fillEdge);
            // Line straight left to far edge of the view
            fillPath.lineTo(getPaddingStart(), fillEdge);
            // Closes line back on the first point
            fillPath.close();
        }
    }

    /**
     * Populates the base line path if enabled.
     */
    @SuppressWarnings("ConstantConditions")
    private void populateBaseLine() {
        baseLinePath.reset();
        if (adapter.hasBaseLine()) {
            float scaledBaseLine = scaleHelper.getY(adapter.getBaseLine());
            baseLinePath.moveTo(0, scaledBaseLine);
            baseLinePath.lineTo(getWidth(), scaledBaseLine);
        }
    }

    /**
     * Populates labels.
     */
    private void populateLabels() {
        labelsY = new ArrayList<>(gridLinesY.size() - 1);
        String labelText;
        Rect labelTextBounds = new Rect();
        float labelTextX;
        float labelTextY;
        RectF background;
        float bgLeft = drawingArea.left + labelMargin;
        float bgTop;
        float bgRight;
        float bgBottom;
        for (int i = 1; i < gridLinesY.size(); i++) { // No label in the last grid line
            // Format text
            labelText = labelFormatter.format(scaleHelper.getRawY(gridLinesY.get(i)));
            // Calculate background
            labelTextPaint.getTextBounds(labelText, 0, labelText.length(), labelTextBounds);
            bgTop = gridLinesY.get(i) - labelTextBounds.height() / 2F - labelBackgroundPaddingVertical;
            bgRight = bgLeft + labelTextBounds.width() + labelBackgroundPaddingHorizontal * 2;
            bgBottom = bgTop + labelTextBounds.height() + labelBackgroundPaddingVertical * 2;
            background = new RectF(bgLeft, bgTop, bgRight, bgBottom);
            // Calculate text position
            labelTextX = bgLeft + labelBackgroundPaddingHorizontal - labelTextBounds.left;
            labelTextY = bgBottom - labelBackgroundPaddingVertical - labelTextBounds.bottom;
            labelsY.add(new Label(background, labelTextX, labelTextY, labelText));
        }
    }

    /**
     * Populates scrub cursor.
     */
    private void populateScrubCursor(float x) {
        x = resolveBoundedScrubLine(x);
        scrubCursorX = x;
        scrubCursorY = drawingArea.centerY();
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

    /**
     * Clears data to draw.
     */
    private void clearData() {
        scaleHelper = null;
        fillPath.reset();
        linePath.reset();
        gridLinePath.reset();
        baseLinePath.reset();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // No allocations here, Allocate objects during initialization, or between animations. Never make an allocation while an animation is running.
        // In addition to making onDraw() leaner, also make sure it's called as infrequently as possible. Most calls to onDraw() are the result of a call to invalidate(), so eliminate unnecessary calls to invalidate().
        super.onDraw(canvas);
        canvas.drawPath(gridLinePath, gridLinePaint);
        canvas.drawPath(baseLinePath, baseLinePaint);
        if (fillType != LineChartFillType.NONE) {
            canvas.drawPath(fillPath, fillPaint);
        }
        canvas.drawPath(linePath, linePaint);
        drawScrubCursor(canvas);
        drawLabels(canvas);

    }

    /**
     * Draws scrub cursor.
     */
    private void drawScrubCursor(Canvas canvas) {
        if (isScrubEnabled) {
            canvas.drawPath(scrubLinePath, scrubLinePaint);
            canvas.drawBitmap(scrubCursor, scrubCursorX - scrubCursor.getWidth() / 2F, scrubCursorY - scrubCursor.getHeight() / 2F, scrubLinePaint);
        }
    }

    /**
     * Draws chart labels in the canvas.
     */
    private void drawLabels(Canvas canvas) {
        if(labelsY == null) {
            return;
        }
        for (Label label : labelsY) {
            canvas.drawRoundRect(label.getBackground(), labelBackgroundRadius, labelBackgroundRadius, labelBackgroundPaint);
            canvas.drawText(label.getText(), label.getTextX(), label.getTextY(), labelTextPaint);
        }
    }

    @Override
    public void onScrubbed(float x, float y) {
        if (adapter == null || adapter.getCount() == 0) {
            return;
        }
        if (!isScrubEnabled) {
            hideLabels();
        }
        getParent().requestDisallowInterceptTouchEvent(true);
        int index = getNearestIndex(scaledXPoints, x);
        if (scrubListener != null) {
            scrubListener.onScrubbed(adapter.getItem(index));
        }
        populateScrubCursor(x);
        isScrubEnabled = true;
    }

    @Override
    public void onScrubEnded() {
        if(isScrubEnabled) {
            showLabels();
        }
        scrubLinePath.reset();
        if (scrubListener != null) {
            scrubListener.onScrubbed(null);
        }
        isScrubEnabled = false;
        invalidate();
    }

    /**
     * Decreases the alpha of the labels.
     */
    private void hideLabels() {
        animateAlphaLabels(1, 0.3F);
    }

    /**
     * Increases the alpha of the labels.
     */
    private void showLabels() {
        animateAlphaLabels(0.3F, 1);
    }

    /**
     * Animates the alpha of the labels.
     */
    private void animateAlphaLabels(float start, float end) {
        // If animation was running, take the actual value and cancel animation
        if(labelAlphaAnimator != null) {
            start = (float) labelAlphaAnimator.getAnimatedValue();
            labelAlphaAnimator.cancel();
        }
        labelAlphaAnimator = ValueAnimator.ofFloat(start, end);
        labelAlphaAnimator.addUpdateListener(animation -> {
            float index = (float) animation.getAnimatedValue();
            labelTextPaint.setAlpha((int) (labelTextOriginalAlpha * index));
            labelBackgroundPaint.setAlpha((int) (labelBackgroundOriginalAlpha * index));
            invalidate();
        });
        labelAlphaAnimator.start();
    }

    private final DataSetObserver dataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            populatePaths();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Getters & Setters                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////

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
        populatePaths();
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
            populatePaths();
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

    @Override
    public int getGridLineColor() {
        return gridLineColor;
    }

    @Override
    public void setGridLineColor(int gridLineColor) {
        this.gridLineColor = gridLineColor;
        gridLinePaint.setColor(gridLineColor);
        invalidate();
    }

    @Override
    public float getGridLineWidth() {
        return gridLineWidth;
    }

    @Override
    public void setGridLineWidth(float gridLineWidth) {
        this.gridLineWidth = gridLineWidth;
        gridLinePaint.setStrokeWidth(gridLineWidth);
        invalidate();
    }

    @Override
    public int getGridXDivisions() {
        return gridXDivisions;
    }

    @Override
    public void setGridXDivisions(int gridXDivisions) {
        this.gridXDivisions = gridXDivisions;
        invalidate();
    }

    @Override
    public int getGridYDivisions() {
        return gridYDivisions;
    }

    @Override
    public void setGridYDivisions(int gridYDivisions) {
        this.gridYDivisions = gridYDivisions;
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
        this.fillPath.reset();
        this.fillPath.addPath(animationPath);
        this.fillPath.rLineTo(0, 0);
        invalidate();
    }

    @NonNull
    @Override
    public List<Float> getXPoints() {
        return new ArrayList<>(scaledXPoints);
    }

    @NonNull
    @Override
    public List<Float> getYPoints() {
        return new ArrayList<>(scaledYPoints);
    }
}
