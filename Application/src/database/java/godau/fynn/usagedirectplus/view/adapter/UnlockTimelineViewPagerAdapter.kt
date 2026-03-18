package godau.fynn.usagedirectplus.view.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.persistence.ScreenEvent
import godau.fynn.usagedirectplus.persistence.ScreenEventDao
import godau.fynn.usagedirectplus.view.TimelineView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.LinkedBlockingQueue

class UnlockTimelineViewPagerAdapter(
    private val context: Context,
    private val screenEventDao: ScreenEventDao,
    private val dayCount: Int
) : PagerAdapter() {

    private val accentColor = ContextCompat.getColor(context, R.color.accent)

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val timelineView: TimelineView
        if (recycleViewList.peek() == null) {
            timelineView = TimelineView(context)
            Log.d("UTVPA", "Creating timeline view for position $position")
        } else {
            Log.d("UTVPA", "Recycling timeline view for position $position")
            timelineView = recycleViewList.poll()!!
            timelineView.setData(emptyList())
        }
        container.addView(timelineView)

        val isToday = position == count - 1
        if (isToday) {
            val now = LocalTime.now()
            timelineView.setShowNowIndicator(true)
            timelineView.setNowMinuteOfDay(now.hour * 60f + now.minute + now.second / 60f)
        } else {
            timelineView.setShowNowIndicator(false)
        }

        val dayOffset = count - 1 - position

        MainScope().launch(Dispatchers.IO) {
            val zone = ZoneId.systemDefault()
            val dayStart = java.time.LocalDate.now().minusDays(dayOffset.toLong())
                .atStartOfDay(zone).toInstant().toEpochMilli()
            val dayEnd = java.time.LocalDate.now().minusDays(dayOffset.toLong())
                .plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

            val events = screenEventDao.getKeyguardEvents(dayStart, dayEnd)
            val segments = toUnlockSegments(events, accentColor)

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

        internal fun toUnlockSegments(
            events: List<ScreenEvent>,
            accentColor: Int
        ): List<TimelineView.TimelineSegment> {
            if (events.isEmpty()) return emptyList()

            val zone = ZoneId.systemDefault()
            val segments = mutableListOf<TimelineView.TimelineSegment>()
            var unlockStart: Float? = null
            var stateKnown = false

            for (event in events) {
                val localTime = Instant.ofEpochMilli(event.timestamp).atZone(zone).toLocalTime()
                val minute = localTime.hour * 60f + localTime.minute + localTime.second / 60f

                when (event.eventType) {
                    ScreenEvent.KEYGUARD_HIDDEN -> {
                        // Unlock event — start a new unlocked period (if not already tracking one)
                        if (unlockStart == null) {
                            unlockStart = minute
                        }
                        stateKnown = true
                    }
                    ScreenEvent.KEYGUARD_SHOWN -> {
                        // Lock event
                        if (unlockStart != null) {
                            // Close the current unlocked period
                            segments.add(TimelineView.TimelineSegment(unlockStart, minute, accentColor))
                            unlockStart = null
                        } else if (!stateKnown) {
                            // First event is a lock → phone was unlocked since midnight
                            segments.add(TimelineView.TimelineSegment(0f, minute, accentColor))
                        }
                        stateKnown = true
                    }
                }
            }

            // If still unlocked at end of day, extend to end of day
            if (unlockStart != null) {
                segments.add(TimelineView.TimelineSegment(unlockStart, 1440f, accentColor))
            }

            return segments
        }
    }
}
