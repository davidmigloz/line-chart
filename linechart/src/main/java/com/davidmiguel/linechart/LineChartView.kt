package com.davidmiguel.linechart

import android.animation.Animator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.database.DataSetObserver
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.ViewConfiguration
import androidx.annotation.ColorInt
import com.davidmiguel.linechart.animation.LineChartAnimator
import com.davidmiguel.linechart.formatter.DefaultYAxisValueFormatter
import com.davidmiguel.linechart.formatter.YAxisValueFormatter
import com.davidmiguel.linechart.model.Label
import com.davidmiguel.linechart.touch.OnScrubListener
import com.davidmiguel.linechart.touch.ScrubGestureDetector
import com.davidmiguel.linechart.touch.ScrubGestureDetector.ScrubListener
import com.davidmiguel.linechart.utils.ScaleHelper
import com.davidmiguel.linechart.utils.Utils.getBitmapFromVectorDrawable
import com.davidmiguel.linechart.utils.Utils.getNearestIndex
import java.util.*

@Suppress("unused", "MemberVisibilityCanBePrivate")
class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.linechart_LineChartViewStyle,
    defStyleRes: Int = R.style.linechart_LineChartView,
) : View(context, attrs, defStyleAttr, defStyleRes), ScrubListener, LineChart {

    // Data
    private lateinit var _adapter: LineChartAdapter
    override var adapter: LineChartAdapter
        get() = _adapter
        set(value) {
            if (::_adapter.isInitialized) _adapter.unregisterDataSetObserver(dataSetObserver)
            value.registerDataSetObserver(dataSetObserver)
            _adapter = value
            populatePaths()
        }
    private lateinit var scaleHelper: ScaleHelper
    override val drawingArea = RectF()
    private var scaledXPoints = mutableListOf<Float>()
    private var scaledYPoints = mutableListOf<Float>()
    override val xPoints: List<Float>
        get() = ArrayList(scaledXPoints)
    override val yPoints: List<Float>
        get() = ArrayList(scaledYPoints)

    // Styleable properties
    @ColorInt
    override var lineColor = 0
        set(value) {
            field = value
            linePaint.color = value
            invalidate()
        }
    override var lineWidth = 0f
        set(value) {
            field = value
            linePaint.strokeWidth = value
            invalidate()
        }
    override var cornerRadius = 0f
        set(value) {
            field = value
            if (value != 0f) {
                linePaint.pathEffect = CornerPathEffect(value)
                fillPaint.pathEffect = CornerPathEffect(value)
            } else {
                linePaint.pathEffect = null
                fillPaint.pathEffect = null
            }
            invalidate()
        }

    @LineChartFillType
    override var fillType = LineChartFillType.NONE
        set(value) {
            if (fillType != value) {
                field = value
                populatePaths()
            }
        }

    @ColorInt
    override var fillColor = 0
        set(value) {
            field = value
            fillPaint.color = value
            invalidate()
        }

    @ColorInt
    override var gridLineColor = 0
        set(value) {
            field = value
            gridLinePaint.color = value
            invalidate()
        }
    override var gridLineWidth = 0f
        set(value) {
            field = value
            gridLinePaint.strokeWidth = value
            invalidate()
        }

    @ColorInt
    override var baseLineColor = 0
        set(value) {
            field = value
            baseLinePaint.color = value
            invalidate()
        }
    override var baseLineWidth = 0f
        set(value) {
            field = value
            baseLinePaint.strokeWidth = value
            invalidate()
        }

    @ColorInt
    override var zeroLineColor = 0
        set(value) {
            field = value
            zeroLinePaint.color = value
            invalidate()
        }
    override var zeroLineWidth = 0f
        set(value) {
            field = value
            zeroLinePaint.strokeWidth = value
            invalidate()
        }
    override var labelMargin = 0f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    override var labelTextColor = 0
        set(value) {
            field = value
            labelTextPaint.color = value
            invalidate()
        }
    override var labelTextSize = 0f
        set(value) {
            field = value
            labelTextPaint.textSize = value
            invalidate()
        }

    @ColorInt
    override var labelBackgroundColor = 0
        set(value) {
            field = value
            labelBackgroundPaint.color = value
            invalidate()
        }
    override var labelBackgroundRadius = 0f
        set(value) {
            field = value
            invalidate()
        }
    override var labelBackgroundPaddingHorizontal = 0f
        set(value) {
            field = value
            invalidate()
        }
    override var labelBackgroundPaddingVertical = 0f
        set(value) {
            field = value
            invalidate()
        }

    @ColorInt
    override var zeroLabelTextColor = 0
        set(value) {
            field = value
            zeroLabelTextPaint.color = value
            invalidate()
        }
    override var zeroLabelTextSize = 0f
        set(value) {
            field = value
            zeroLabelTextPaint.textSize = value
            invalidate()
        }

    @ColorInt
    override var zeroLabelBackgroundColor = 0
        set(value) {
            field = value
            zeroLabelBackgroundPaint.color = value
            invalidate()
        }

    @ColorInt
    override var scrubLineColor = 0
        set(value) {
            field = value
            scrubLinePaint.color = scrubLineColor
            invalidate()
        }
    override var scrubLineWidth = 0f
        set(value) {
            field = value
            scrubLinePaint.strokeWidth = scrubLineWidth
            invalidate()
        }

    // How to draw
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val baseLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val zeroLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var labelTextOriginalAlpha = 0
    private val labelBackgroundPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var labelBackgroundOriginalAlpha = 0
    private val zeroLabelTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var zeroLabelTextOriginalAlpha = 0
    private val zeroLabelBackgroundPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
    private var zeroLabelBackgroundOriginalAlpha = 0
    override var yAxisValueFormatter: YAxisValueFormatter = DefaultYAxisValueFormatter()
    private val scrubLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // What to draw
    private val linePath = Path()
    private val fillPath = Path()
    override var gridXDivisions = 0
        set(value) {
            field = value
            invalidate()
        }
    override var gridYDivisions = 0
        set(value) {
            field = value
            invalidate()
        }
    private var gridLinesX = mutableListOf<Float>()
    private var gridLinesY = mutableListOf<Float>()
    private val gridLinePath = Path()
    private val baseLinePath = Path()
    override var isZeroLineEnabled = true
        set(value) {
            field = value
            invalidate()
        }
    private val zeroLinePath = Path()
    private var labelsY = mutableListOf<Label>()
    override var isScrubEnabled = true
        set(value) {
            field = value
            scrubGestureDetector.enabled = value
            invalidate()
        }
    private val scrubLinePath = Path()
    private var scrubCursorImg: Bitmap? = null
    private var scrubCursorCurrentPos: PointF? = null
    private var scrubCursorTargetPos: PointF? = null
    private var scrubAnimator = ValueAnimator()

    // Touch
    private var scrubGestureDetector = ScrubGestureDetector(
        this,
        Handler(Looper.getMainLooper()),
        ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    )
    override var scrubListener: OnScrubListener? = null

    // Animation
    private val mainHandler = Handler(Looper.getMainLooper())
    override var lineChartAnimator: LineChartAnimator? = null
    private var pathAnimator: Animator? = null
    override var animationPath: Path?
        get() = linePath
        set(value) {
            val path = value ?: return
            linePath.reset()
            linePath.addPath(path)
            linePath.rLineTo(0f, 0f)
            populateFilling(adapter.count, linePath)
            invalidate()
        }
    var labelAlphaAnimator: ValueAnimator? = null
    var zeroLabelAlphaAnimator: ValueAnimator? = null

    init {
        processAttributes(context, attrs, defStyleAttr, defStyleRes)
        configPaintStyles()
        configGestureDetector()
        configEditMode()
    }

    /**
     * Reads the xml attributes and configures the view based on them.
     */
    private fun processAttributes(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LineChartView, defStyleAttr, defStyleRes)
        try {
            // Styleable properties
            lineColor = a.getColor(R.styleable.LineChartView_linechart_lineColor, 0)
            lineWidth = a.getDimension(R.styleable.LineChartView_linechart_lineWidth, 0f)
            cornerRadius = a.getDimension(R.styleable.LineChartView_linechart_cornerRadius, 0f)
            fillType = a.getInt(R.styleable.LineChartView_linechart_fillType, LineChartFillType.NONE)
            fillColor = a.getColor(R.styleable.LineChartView_linechart_fillColor, 0)
            gridLineColor = a.getColor(R.styleable.LineChartView_linechart_gridLineColor, 0)
            gridLineWidth = a.getDimension(R.styleable.LineChartView_linechart_gridLineWidth, 0f)
            baseLineColor = a.getColor(R.styleable.LineChartView_linechart_baseLineColor, 0)
            baseLineWidth = a.getDimension(R.styleable.LineChartView_linechart_baseLineWidth, 0f)
            zeroLineColor = a.getColor(R.styleable.LineChartView_linechart_zeroLineColor, 0)
            zeroLineWidth = a.getDimension(R.styleable.LineChartView_linechart_zeroLineWidth, 0f)
            labelMargin = a.getDimension(R.styleable.LineChartView_linechart_labelMargin, 0f)
            labelTextColor = a.getColor(R.styleable.LineChartView_linechart_labelTextColor, 0)
            labelTextSize = a.getDimension(R.styleable.LineChartView_linechart_labelTextSize, 0f)
            labelBackgroundColor = a.getColor(R.styleable.LineChartView_linechart_labelBackgroundColor, 0)
            labelBackgroundRadius = a.getDimension(R.styleable.LineChartView_linechart_labelBackgroundRadius, 0f)
            labelBackgroundPaddingHorizontal = a.getDimension(R.styleable.LineChartView_linechart_labelBackgroundPaddingHorizontal, 0f)
            labelBackgroundPaddingVertical = a.getDimension(R.styleable.LineChartView_linechart_labelBackgroundPaddingVertical, 0f)
            zeroLabelTextColor = a.getColor(R.styleable.LineChartView_linechart_zeroLabelTextColor, 0)
            zeroLabelTextSize = a.getDimension(R.styleable.LineChartView_linechart_zeroLabelTextSize, 0f)
            zeroLabelBackgroundColor = a.getColor(R.styleable.LineChartView_linechart_zeroLabelBackgroundColor, 0)
            scrubLineColor = a.getColor(R.styleable.LineChartView_linechart_scrubLineColor, 0)
            scrubLineWidth = a.getDimension(R.styleable.LineChartView_linechart_scrubLineWidth, 0f)
            // What to draw
            gridXDivisions = a.getInteger(R.styleable.LineChartView_linechart_gridXDivisions, 0)
            gridYDivisions = a.getInteger(R.styleable.LineChartView_linechart_gridYDivisions, 0)
            isZeroLineEnabled = a.getBoolean(R.styleable.LineChartView_linechart_zeroLineEnabled, true)
            isScrubEnabled = a.getBoolean(R.styleable.LineChartView_linechart_scrubEnabled, true)
        } finally {
            a.recycle()
        }
    }

    /**
     * Configures how the different elements of the chart have to be painted.
     */
    private fun configPaintStyles() {
        // Line
        linePaint.apply {
            style = Paint.Style.STROKE
            color = lineColor
            strokeWidth = lineWidth
            strokeCap = Paint.Cap.ROUND
            if (cornerRadius != 0f) {
                pathEffect = CornerPathEffect(cornerRadius)
            }
        }
        // Fill
        fillPaint.apply {
            set(linePaint)
            color = fillColor
            style = Paint.Style.FILL
            strokeWidth = 0f
        }
        // Grid
        gridLinePaint.apply {
            style = Paint.Style.STROKE
            color = gridLineColor
            strokeWidth = gridLineWidth
        }
        // Baseline
        baseLinePaint.apply {
            style = Paint.Style.STROKE
            color = baseLineColor
            strokeWidth = baseLineWidth
        }
        // Zero line
        zeroLinePaint.apply {
            style = Paint.Style.STROKE
            color = zeroLineColor
            strokeWidth = zeroLineWidth
            pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
        }
        // Labels
        labelTextPaint.apply {
            color = labelTextColor
            textSize = labelTextSize
            textAlign = Paint.Align.LEFT
        }
        labelTextOriginalAlpha = labelTextPaint.alpha
        labelBackgroundPaint.apply {
            color = labelBackgroundColor
            style = Paint.Style.FILL
        }
        labelBackgroundOriginalAlpha = labelBackgroundPaint.alpha
        // Zero label
        zeroLabelTextPaint.apply {
            color = zeroLabelTextColor
            textSize = zeroLabelTextSize
            textAlign = Paint.Align.LEFT
        }
        zeroLabelTextOriginalAlpha = zeroLabelTextPaint.alpha
        zeroLabelBackgroundPaint.apply {
            color = zeroLabelBackgroundColor
            style = Paint.Style.FILL
        }
        zeroLabelBackgroundOriginalAlpha = zeroLabelBackgroundPaint.alpha
        // Scrub line
        scrubLinePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = scrubLineWidth
            color = scrubLineColor
            strokeCap = Paint.Cap.ROUND
        }
    }

    /**
     * Configures the gesture detector to listen to user touches.
     */
    private fun configGestureDetector() {
        scrubGestureDetector.enabled = isScrubEnabled
        setOnTouchListener(scrubGestureDetector)
    }

    /**
     * Configures the mock preview displayed in the layout editor.
     */
    private fun configEditMode() {
        if (!isInEditMode) return
        adapter = object : LineChartAdapter() {
            private val yData = floatArrayOf(68f, 22f, 31f, 57f, 35f, 79f, 86f, 47f, 34f, 55f, 80f, 72f, 99f, 66f, 47f, 42f, 56f, 64f, 66f, 80f, 97f, 10f, 43f, 12f, 25f, 71f, 47f, 73f, 49f, 36f)

            override val count: Int
                get() = yData.size

            override fun getItem(index: Int): Any {
                return yData[index]
            }

            override fun getY(index: Int): Float {
                return yData[index]
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Called when the view is first assigned a size, and again if the size changes for any reason
        // All calculations related to positions, dimensions, and any other values must be done here (not in onDraw)
        super.onSizeChanged(w, h, oldw, oldh)
        calculateDrawingArea()
        populatePaths()
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        calculateDrawingArea()
        populatePaths()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        scrubCursorImg?.recycle()
    }

    /**
     * Calculates the area where we can draw (essentially the bounding rect minus any padding).
     * The area is represented in a rectangle.
     */
    private fun calculateDrawingArea() {
        drawingArea[paddingStart.toFloat(), paddingTop.toFloat(), (
                width - paddingEnd).toFloat()] = (
                height - paddingBottom).toFloat()
    }

    /**
     * Populates paths to draw.
     */
    private fun populatePaths() {
        if (width == 0 || height == 0) return
        val numPoints = adapter.count
        // To draw anything, we need 2 or more points
        if (numPoints < 2) {
            clearData()
            return
        }
        scaleHelper = ScaleHelper(adapter, drawingArea, gridYDivisions)
        scrubCursorImg = getBitmapFromVectorDrawable(context, R.drawable.linechart_scrub_cursor)
        // Populate paths
        populateLine(numPoints)
        populateFilling(numPoints, linePath)
        populateGrid()
        populateBaseLine()
        populateZeroLine()
        populateLabels()
        resetScrubCursor()
        lineChartAnimator?.run { doPathAnimation() }
        invalidate()
    }

    private fun populateLine(numPoints: Int) {
        linePath.reset()
        scaledXPoints.clear()
        scaledYPoints.clear()
        for (i in 0 until numPoints) {
            // Scale points relative to the drawing area
            val x = scaleHelper.getX(adapter.getX(i))
            val y = scaleHelper.getY(adapter.getY(i))
            scaledXPoints.add(x)
            scaledYPoints.add(y)
            // Populate line path
            if (i == 0) {
                linePath.moveTo(x, y)
            } else {
                linePath.lineTo(x, y)
            }
        }
    }

    private fun populateFilling(numPoints: Int, linePath: Path) {
        fillPath.reset()
        getFillEdge()?.let { fillEdge ->
            // Copy line path
            fillPath.addPath(linePath)
            // Line up or down to the fill edge
            val lastX = scaleHelper.getX(numPoints - 1f)
            fillPath.lineTo(lastX, fillEdge)
            // Line straight left to far edge of the view
            fillPath.lineTo(paddingStart.toFloat(), fillEdge)
            // Closes line back on the first point
            fillPath.close()
        }
    }

    /**
     * Returns the fill edge relative to the fill type.
     */
    private fun getFillEdge(): Float? {
        return when (fillType) {
            LineChartFillType.NONE -> null
            LineChartFillType.UP -> paddingTop.toFloat()
            LineChartFillType.DOWN -> height.toFloat() - paddingBottom
            LineChartFillType.TOWARD_ZERO -> {
                val zero = scaleHelper.getY(0f)
                val bottom = height.toFloat() - paddingBottom
                zero.coerceAtMost(bottom)
            }
            else -> error("Unknown fill-type: $fillType")
        }
    }

    /**
     * Return whether or not this line chart should fill the area underneath.
     */
    private fun isFill(): Boolean {
        return when (fillType) {
            LineChartFillType.NONE -> false
            LineChartFillType.UP, LineChartFillType.DOWN,
            LineChartFillType.TOWARD_ZERO -> true
            else -> error("Unknown fill-type: $fillType")
        }
    }

    private fun populateGrid() {
        gridLinePath.reset()
        val gridLeft = drawingArea.left
        val gridBottom = drawingArea.bottom
        val gridTop = drawingArea.top
        val gridRight = drawingArea.right
        // Grid X axis
        gridLinesX = ArrayList(gridXDivisions)
        var gridLineSpacing = (gridRight - gridLeft) / gridXDivisions
        for (i in 0 until gridXDivisions) {
            gridLinesX.add(gridLeft + i * gridLineSpacing)
            gridLinePath.moveTo(gridLinesX[i], gridTop)
            gridLinePath.lineTo(gridLinesX[i], gridBottom)
        }
        // Grid Y axis
        gridLinesY = ArrayList(gridYDivisions)
        gridLineSpacing = (gridBottom - gridTop) / gridYDivisions
        for (i in 0 until gridYDivisions) {
            gridLinesY.add(gridTop + i * gridLineSpacing)
            gridLinePath.moveTo(gridLeft, gridLinesY[i])
            gridLinePath.lineTo(gridRight, gridLinesY[i])
        }
    }

    private fun populateBaseLine() {
        baseLinePath.reset()
        if (adapter.hasBaseLine) {
            val baseLineXStart = drawingArea.left
            val baseLineXEnd = drawingArea.right
            val baselineY = scaleHelper.getY(adapter.baseLine!!)
            baseLinePath.moveTo(baseLineXStart, baselineY)
            baseLinePath.lineTo(baseLineXEnd, baselineY)
        }
    }

    private fun populateZeroLine() {
        zeroLinePath.reset()
        if (shouldDrawZeroLine()) {
            val zeroStart = drawingArea.left
            val zeroEnd = drawingArea.right
            val zeroY = scaleHelper.getY(0f)
            zeroLinePath.moveTo(zeroEnd, zeroY)
            zeroLinePath.lineTo(zeroStart, zeroY)
        }
    }

    private fun shouldDrawZeroLine() = isZeroLineEnabled && adapter.getDataBounds().top < 0

    private fun populateLabels() {
        labelsY = ArrayList(gridLinesY.size - 1)
        var labelText: String
        val labelTextBounds = Rect()
        var labelTextX: Float
        var labelTextY: Float
        var background: RectF
        var bgLeft = drawingArea.left + labelMargin
        var bgTop: Float
        var bgRight: Float
        var bgBottom: Float
        // Populate grid labels
        for (i in 1 until gridLinesY.size) { // No label in the last grid line
            // Format text
            labelText = yAxisValueFormatter.getFormattedValue(scaleHelper.getRawY(gridLinesY[i]), adapter.getDataBounds(), gridYDivisions)
            // Calculate background
            labelTextPaint.getTextBounds(labelText, 0, labelText.length, labelTextBounds)
            bgTop = gridLinesY[i] - labelTextBounds.height() / 2f - labelBackgroundPaddingVertical
            bgRight = bgLeft + labelTextBounds.width() + labelBackgroundPaddingHorizontal * 2
            bgBottom = bgTop + labelTextBounds.height() + labelBackgroundPaddingVertical * 2
            background = RectF(bgLeft, bgTop, bgRight, bgBottom)
            // Calculate text position
            labelTextX = bgLeft + labelBackgroundPaddingHorizontal - labelTextBounds.left
            labelTextY = bgBottom - labelBackgroundPaddingVertical - labelTextBounds.bottom
            labelsY.add(Label(background, labelTextX, labelTextY, labelText, labelBackgroundPaint, labelTextPaint))
        }
        // Add zero label
        if (shouldDrawZeroLine()) {
            labelText = yAxisValueFormatter.getFormattedValue(0f, adapter.getDataBounds(), gridYDivisions)
            zeroLabelTextPaint.getTextBounds(labelText, 0, labelText.length, labelTextBounds)
            bgTop = scaleHelper.getY(0f) - labelTextBounds.height() / 2f - labelBackgroundPaddingVertical
            bgRight = drawingArea.right - labelMargin
            bgBottom = bgTop + labelTextBounds.height() + labelBackgroundPaddingVertical * 2
            bgLeft = bgRight - labelTextBounds.width() - labelBackgroundPaddingHorizontal * 2
            background = RectF(bgLeft, bgTop, bgRight, bgBottom)
            labelTextX = bgLeft + labelBackgroundPaddingHorizontal - labelTextBounds.left
            labelTextY = bgBottom - labelBackgroundPaddingVertical - labelTextBounds.bottom
            labelsY.add(Label(background, labelTextX, labelTextY, labelText, zeroLabelBackgroundPaint, zeroLabelTextPaint))
        }
    }

    private fun resetScrubCursor() {
        scrubAnimator.cancel()
        mainHandler.removeCallbacksAndMessages(null)
        scrubCursorTargetPos = null
        scrubCursorCurrentPos = scrubCursorTargetPos
    }

    private fun populateScrubCursor(newScrubCursorTargetPos: PointF) {
        when {
            scrubCursorCurrentPos == null -> {
                scrubCursorTargetPos = newScrubCursorTargetPos
                scrubCursorCurrentPos = scrubCursorTargetPos
            }
            newScrubCursorTargetPos == scrubCursorTargetPos -> return
            else -> {
                scrubCursorTargetPos = newScrubCursorTargetPos
                val animX = PropertyValuesHolder.ofFloat("x", scrubCursorCurrentPos!!.x, scrubCursorTargetPos!!.x)
                val animY = PropertyValuesHolder.ofFloat("y", scrubCursorCurrentPos!!.y, scrubCursorTargetPos!!.y)
                scrubAnimator.setValues(animX, animY)
                scrubAnimator.duration = 50
                scrubAnimator.addUpdateListener { animation: ValueAnimator ->
                    scrubCursorCurrentPos = PointF(animation.getAnimatedValue("x") as Float, animation.getAnimatedValue("y") as Float)
                    invalidate()
                }
                scrubAnimator.start()
            }
        }
        invalidate()
    }

    private fun clearData() {
        fillPath.reset()
        linePath.reset()
        gridLinePath.reset()
        baseLinePath.reset()
        zeroLinePath.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        // No allocations here, Allocate objects during initialization, or between animations. Never make an allocation while an animation is running.
        // In addition to making onDraw() leaner, also make sure it's called as infrequently as possible. Most calls to onDraw() are the result of a call to invalidate(), so eliminate unnecessary calls to invalidate().
        super.onDraw(canvas)
        canvas.drawPath(gridLinePath, gridLinePaint)
        if (fillType != LineChartFillType.NONE) {
            canvas.drawPath(fillPath, fillPaint)
        }
        canvas.drawPath(zeroLinePath, zeroLinePaint)
        canvas.drawPath(baseLinePath, baseLinePaint)
        canvas.drawPath(linePath, linePaint)
        drawScrubCursor(canvas)
        drawLabels(canvas)
    }

    private fun drawScrubCursor(canvas: Canvas) {
        scrubCursorCurrentPos?.let { currentPos ->
            scrubCursorImg?.let { cursorImg ->
                canvas.drawBitmap(
                    cursorImg,
                    currentPos.x - cursorImg.width / 2f,
                    currentPos.y - cursorImg.height / 2f,
                    scrubLinePaint
                )
            }
        }
    }

    private fun drawLabels(canvas: Canvas) {
        if (labelsY.isEmpty()) return
        for ((background, textX, textY, text, labelBackgroundPaint, labelTextPaint) in labelsY) {
            canvas.drawRoundRect(background, labelBackgroundRadius, labelBackgroundRadius, labelBackgroundPaint)
            canvas.drawText(text, textX, textY, labelTextPaint)
        }
    }

    override fun onScrubbed(x: Float, y: Float) {
        if (adapter.count == 0 || scrubAnimator.isRunning) return
        parent.requestDisallowInterceptTouchEvent(true)
        if (scrubCursorTargetPos == null) {
            hideLabels()
            mainHandler.removeCallbacksAndMessages(null)
        }
        val index = getNearestIndex(scaledXPoints, x)
        scrubListener?.onScrubbed(adapter.getItem(index))
        populateScrubCursor(PointF(scaleHelper.getX(adapter.getX(index)), scaleHelper.getY(adapter.getY(index))))
    }

    override fun onScrubEnded() {
        showLabels()
        scrubLinePath.reset()
        scrubListener?.onScrubbed(null)
        scrubAnimator.cancel()
        scrubCursorCurrentPos = scrubCursorTargetPos
        scrubCursorTargetPos = null
        mainHandler.postDelayed({ hideCursor() }, 3000)
    }

    private fun hideCursor() {
        scrubCursorCurrentPos = null
        invalidate()
    }

    /**
     * Decreases the alpha of the labels.
     */
    private fun hideLabels() {
        animateAlphaLabels(1f, 0.3f)
    }

    /**
     * Increases the alpha of the labels.
     */
    private fun showLabels() {
        animateAlphaLabels(0.3f, 1f)
    }

    private fun animateAlphaLabels(start: Float, end: Float) {
        labelAlphaAnimator = animateAlpha(
            labelAlphaAnimator,
            labelTextPaint, labelTextOriginalAlpha,
            labelBackgroundPaint, labelBackgroundOriginalAlpha,
            start, end,
        )
        zeroLabelAlphaAnimator = animateAlpha(
            zeroLabelAlphaAnimator,
            zeroLabelTextPaint, zeroLabelTextOriginalAlpha,
            zeroLabelBackgroundPaint, zeroLabelBackgroundOriginalAlpha,
            start, end,
        )
    }

    private fun animateAlpha(
        animator: ValueAnimator?,
        labelTextPaint: Paint,
        labelTextOriginalAlpha: Int,
        labelBackgroundPaint: Paint,
        labelBackgroundOriginalAlpha: Int,
        start: Float,
        end: Float
    ): ValueAnimator {
        // If animation was running, take the actual value and cancel animation
        var startVal = start
        animator?.let {
            startVal = it.animatedValue as Float
            it.cancel()
        }
        return ValueAnimator.ofFloat(startVal, end).apply {
            addUpdateListener { animation: ValueAnimator ->
                val index = animation.animatedValue as Float
                labelTextPaint.alpha = (labelTextOriginalAlpha * index).toInt()
                labelBackgroundPaint.alpha = (labelBackgroundOriginalAlpha * index).toInt()
                invalidate()
            }
            start()
        }
    }

    private val dataSetObserver: DataSetObserver = object : DataSetObserver() {
        override fun onChanged() {
            super.onChanged()
            populatePaths()
        }

        override fun onInvalidated() {
            super.onInvalidated()
            clearData()
        }
    }

    /**
     * Performs the path animation.
     */
    private fun doPathAnimation() {
        pathAnimator?.cancel()
        getAnimator()?.start()
    }

    /**
     * Gets the Animator from the LineChartAnimator.
     */
    private fun getAnimator(): Animator? {
        return lineChartAnimator?.getAnimation(this)
    }
}