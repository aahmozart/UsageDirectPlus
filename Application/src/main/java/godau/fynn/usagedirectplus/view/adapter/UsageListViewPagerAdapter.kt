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

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.thread.icon.AppUsageStatisticsIconThread
import godau.fynn.usagedirectplus.view.UsageListView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue

/**
 * Child pager of [TimespanPagerAdapter], pages [UsageListView]s
 */
abstract class UsageListViewPagerAdapter(protected val context: AppCompatActivity) : PagerAdapter() {

    /**
     * For performance, don't instantiate the first two pages if they are not actually displayed.
     *
     * @see [related issue](https://codeberg.org/fynngodau/usageDirect/issues/55)
     */
    private var fakeInstantiateFirstPages = true

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // "This does not need to be a View[...]"
        if (fakeInstantiateFirstPages && position < 2 && count > 7) {
            Log.d("CPVPA", "Faking item at position $position")
            return Any()
        }

        if (position > 5) fakeInstantiateFirstPages = false

        // Setup view
        val usageListView: UsageListView
        if (recycleViewList.peek() == null) {
            usageListView = UsageListView(context)
            Log.d("ULVPA", "Creating usage list view for position $position")
        } else {
            Log.d("ULVPA", "Recycling usage list view from recycle bin for position $position")
            usageListView = recycleViewList.poll()!!
            usageListView.setUsageStatsList(null)
        }
        container.addView(usageListView)

        usageListView.setOnItemClickListener(getOnItemClickListener())

        // Get data
        context.lifecycleScope.launch(Dispatchers.IO) {
            val usageStatsList = getUsageStats(position)

            // Filter unused apps
            val iterator = usageStatsList.listIterator(usageStatsList.size)
            while (iterator.hasPrevious()) {
                val usageStat = iterator.previous()
                if (usageStat.timeUsed <= 0) iterator.remove()
            }

            val lastUsedMap = getLastUsedMap()
            val colorMap = getColorMap()

            withContext(Dispatchers.Main) {
                usageListView.setLastUsedMap(lastUsedMap)
                usageListView.setUsageStatsList(usageStatsList)
                usageListView.setColorMap(colorMap)

                // Get missing icons from system
                AppUsageStatisticsIconThread(
                    usageStatsList, usageListView.layoutManager!!, context
                ).start()
            }
        }

        return usageListView
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return `object` === view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (`object` is View) {
            Log.d("ULVPA", "Moving item $position to recycle bin")
            container.removeView(`object`)
            recycleViewList.add(`object` as UsageListView)
        } // Discard placeholder fake items
    }

    abstract override fun getPageTitle(position: Int): CharSequence?

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
        /* TODO this is a kind of inproper way to do it
         * We are doing it anyway because our destroy and recreate process
         * makes use of recycling, which should make it somewhat as performant
         * as updating each usage list view directly.
         */
    }

    /**
     * Usage stats must be pre-sorted globally by usage time (and priority, if applicable)
     *
     * TODO Sorting globally is not needed and takes additional time.
     */
    protected abstract fun getUsageStats(position: Int): MutableList<SimpleUsageStat>

    /**
     * Called after [getUsageStats]
     *
     * @return A mapping of package names to last used timestamp
     */
    protected abstract fun getLastUsedMap(): Map<String, Long>

    protected open fun getColorMap(): Map<String, Int> {
        return HashMap()
    }

    protected open fun getOnItemClickListener(): UsageListAdapter.OnItemClickListener? {
        return null
    }

    companion object {
        private val recycleViewList: java.util.Queue<UsageListView> = LinkedBlockingQueue()
    }
}
