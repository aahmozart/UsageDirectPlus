package godau.fynn.usagedirectplus.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import godau.fynn.usagedirectplus.R

class TimelineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class TimelineSegment(
        val startMinuteOfDay: Float,
        val endMinuteOfDay: Float,
        val color: Int
    )

    private val density = resources.displayMetrics.density

    private var segments: List<TimelineSegment> = emptyList()
    private var showNowIndicator = false
    private var nowMinuteOfDay = 0f

    private val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        strokeWidth = 1f * density
    }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        textSize = 11f * density
        textAlign = Paint.Align.CENTER
    }
    private val nowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.accent)
        strokeWidth = 2f * density
    }

    private val rect = RectF()
    private val minSegmentWidthPx = 2f * density
    private val bandHeight = 64f * density
    private val tickHeight = 6f * density
    private val labelGap = 4f * density
    private val horizontalPadding = 24f * density

    fun setData(segments: List<TimelineSegment>) {
        this.segments = segments
        invalidate()
    }

    fun setShowNowIndicator(show: Boolean) {
        this.showNowIndicator = show
        invalidate()
    }

    fun setNowMinuteOfDay(minute: Float) {
        this.nowMinuteOfDay = minute
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = (bandHeight + tickHeight + labelGap + labelPaint.textSize + 16f * density).toInt()
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val chartLeft = horizontalPadding
        val chartRight = width - horizontalPadding
        val chartWidth = chartRight - chartLeft

        if (chartWidth <= 0) return

        // Center the band vertically
        val axisLabelHeight = tickHeight + labelGap + labelPaint.textSize
        val totalContentHeight = bandHeight + axisLabelHeight
        val topOffset = (height - totalContentHeight) / 2f

        val chartTop = topOffset
        val chartBottom = topOffset + bandHeight

        // Draw segments
        for (segment in segments) {
            val startX = minuteToX(segment.startMinuteOfDay, chartLeft, chartWidth)
            var endX = minuteToX(segment.endMinuteOfDay, chartLeft, chartWidth)

            if (endX - startX < minSegmentWidthPx) {
                endX = startX + minSegmentWidthPx
            }

            segmentPaint.color = segment.color
            rect.set(startX, chartTop, endX, chartBottom)
            canvas.drawRect(rect, segmentPaint)
        }

        // Draw "now" indicator
        if (showNowIndicator) {
            val nowX = minuteToX(nowMinuteOfDay, chartLeft, chartWidth)
            canvas.drawLine(nowX, chartTop, nowX, chartBottom, nowPaint)
        }

        // Draw axis line
        canvas.drawLine(chartLeft, chartBottom, chartRight, chartBottom, axisPaint)

        // Draw hour tick marks and labels
        val labelY = chartBottom + tickHeight + labelGap + labelPaint.textSize
        for (hour in 0..24 step 3) {
            val x = minuteToX(hour * 60f, chartLeft, chartWidth)
            canvas.drawLine(x, chartBottom, x, chartBottom + tickHeight, axisPaint)
            canvas.drawText(hour.toString(), x, labelY, labelPaint)
        }
    }

    companion object {
        internal fun minuteToX(minute: Float, chartLeft: Float, chartWidth: Float): Float {
            return chartLeft + (minute / 1440f) * chartWidth
        }
    }
}
