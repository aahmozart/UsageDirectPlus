/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package godau.fynn.usagedirectplus.view.adapter

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.view.FramedClockPieView
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper
import godau.fynn.usagedirectplus.wrapper.TextFormat
import im.dacer.androidcharts.clockpie.ClockPieSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.util.concurrent.LinkedBlockingQueue

class ClockPieViewPagerAdapter(
    private val context: Context,
    private val eventLogWrapper: EventLogWrapper,
    private val colorMap: Map<String, Int>
) : PagerAdapter() {

    /**
     * For performance, don't instantiate the first two pages that are not actually displayed.
     *
     * @see [related issue](https://codeberg.org/fynngodau/usageDirect/issues/55)
     */
    private var fakeInstantiateFirstPages = true

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // "This does not need to be a View[...]"
        if (fakeInstantiateFirstPages && position < 2) {
            Log.d("CPVPA", "Faking item at position $position")
            return Any()
        }

        if (position > 5) fakeInstantiateFirstPages = false

        val clockPieFrame: FramedClockPieView
        if (recycleViewList.peek() == null) {
            clockPieFrame = FramedClockPieView(context)
            Log.d("CPVPA", "Creating clock pie frame view for position $position")
        } else {
            Log.d("CPVPA", "Recycling clock pie frame view from recycle bin for position $position")
            clockPieFrame = recycleViewList.poll()!!
            clockPieFrame.clockPieView.setData(emptyArray<ClockPieSegment>())
        }
        container.addView(clockPieFrame)

        clockPieFrame.setText(
            context.getString(
                R.string.charts_clock_pie,
                TextFormat.formatDay(count - 1 - position, context.resources)
            )
        )

        val pieView = clockPieFrame.clockPieView

        val backgroundSegment = if (position == count - 1) {
            val now = LocalTime.now()
            ClockPieSegment(0, 0, 0, now.hour, now.minute, now.second)
        } else {
            ClockPieSegment(0, 0, 24, 0)
        }
        pieView.setBackgroundSegment(backgroundSegment)

        val clockPieHelperList = ArrayList<ClockPieSegment>()

        MainScope().launch(Dispatchers.IO) {
            val foregroundStats = eventLogWrapper.getForegroundStatsByRelativeDay(count - 1 - position)

            withContext(Dispatchers.Main) {
                for (stat in foregroundStats) {
                    val segment = stat.asClockPieSegment()

                    if (colorMap.containsKey(stat.packageName)) {
                        segment.setColor(colorMap[stat.packageName]!!)
                    }

                    clockPieHelperList.add(segment)
                }

                pieView.setData(clockPieHelperList)
            }
        }

        return clockPieFrame
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) {
            container.removeView(`object`)
            recycleViewList.add(`object` as FramedClockPieView)
        } // Discard non-View placeholder Objects
    }

    override fun getCount(): Int = 10

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    companion object {
        private val recycleViewList: java.util.Queue<FramedClockPieView> = LinkedBlockingQueue()
    }
}
