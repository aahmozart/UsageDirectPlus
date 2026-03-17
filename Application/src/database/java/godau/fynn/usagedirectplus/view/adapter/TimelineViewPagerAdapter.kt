package godau.fynn.usagedirectplus.view.adapter

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import godau.fynn.usagedirectplus.view.TimelineView
import godau.fynn.usagedirectplus.wrapper.ComponentForegroundStat
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.LinkedBlockingQueue

class TimelineViewPagerAdapter(
    private val context: Context,
    private val eventLogWrapper: EventLogWrapper,
    private val colorMap: Map<String, Int>,
    private val dayCount: Int
) : PagerAdapter() {

    private var fakeInstantiateFirstPages = true

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (fakeInstantiateFirstPages && position < 2) {
            Log.d("TVPA", "Faking item at position $position")
            return Any()
        }

        if (position > 5) fakeInstantiateFirstPages = false

        val timelineView: TimelineView
        if (recycleViewList.peek() == null) {
            timelineView = TimelineView(context)
            Log.d("TVPA", "Creating timeline view for position $position")
        } else {
            Log.d("TVPA", "Recycling timeline view for position $position")
            timelineView = recycleViewList.poll()!!
            timelineView.setData(emptyList())
        }
        container.addView(timelineView)

        val dayOffset = count - 1 - position

        val isToday = position == count - 1
        if (isToday) {
            val now = LocalTime.now()
            timelineView.setShowNowIndicator(true)
            timelineView.setNowMinuteOfDay(now.hour * 60f + now.minute + now.second / 60f)
        } else {
            timelineView.setShowNowIndicator(false)
        }

        MainScope().launch(Dispatchers.IO) {
            val foregroundStats = eventLogWrapper.getForegroundStatsByRelativeDay(dayOffset)
            val segments = foregroundStats.map { it.toTimelineSegment(colorMap, DEFAULT_SEGMENT_COLOR) }

            withContext(Dispatchers.Main) {
                timelineView.setData(segments)
            }
        }

        return timelineView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) {
            container.removeView(`object`)
            recycleViewList.add(`object` as TimelineView)
        }
    }

    override fun getCount(): Int = dayCount

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    companion object {
        private val recycleViewList: java.util.Queue<TimelineView> = LinkedBlockingQueue()
        private const val DEFAULT_SEGMENT_COLOR = Color.GRAY

        internal fun ComponentForegroundStat.toTimelineSegment(
            colorMap: Map<String, Int>,
            defaultColor: Int
        ): TimelineView.TimelineSegment {
            val zone = ZoneId.systemDefault()
            val beginLocal = Instant.ofEpochMilli(beginTime).atZone(zone).toLocalTime()
            val endLocal = Instant.ofEpochMilli(endTime).atZone(zone).toLocalTime()

            val startMinute = beginLocal.hour * 60f + beginLocal.minute + beginLocal.second / 60f
            val endMinute = endLocal.hour * 60f + endLocal.minute + endLocal.second / 60f

            val color = colorMap[packageName] ?: defaultColor

            return TimelineView.TimelineSegment(startMinute, endMinute, color)
        }
    }
}
