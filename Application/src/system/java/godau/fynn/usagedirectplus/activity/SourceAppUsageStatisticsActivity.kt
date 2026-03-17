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

package godau.fynn.usagedirectplus.activity

import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.ProgressBar
import androidx.viewpager.widget.ViewPager
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.ActivitySystemStatsBinding
import godau.fynn.usagedirectplus.view.adapter.system.SystemTimespanPagerAdapter
import godau.fynn.usagedirectplus.wrapper.UsageStatsWrapper

/**
 * Different implementation of AUSA for the two source flavors
 */
class SourceAppUsageStatisticsActivity : AppUsageStatisticsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivitySystemStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        super.onCreate(savedInstanceState)

        setTitle(R.string.title_system_stats)

        binding.systemStatsWarning.setOnClickListener {
            startActivity(Intent(this@SourceAppUsageStatisticsActivity, HelpActivity::class.java))
        }
    }

    override fun onStop() {
        UsageStatsWrapper.flushCache()
        super.onStop()
    }

    override fun prepare() {
    }

    override fun setAdapter(viewPager: ViewPager) {
        val timespanAdapter = SystemTimespanPagerAdapter(this, UsageStatsWrapper(this))

        viewPager.adapter = timespanAdapter
        viewPager.addOnPageChangeListener(timespanAdapter)

        viewPager.pageMargin =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        viewPager.setPageMarginDrawable(ColorDrawable(getColor(R.color.page_switch_indicator)))
    }

    override fun onReload(viewPager: ViewPager, progressBar: ProgressBar, then: Runnable) {
        UsageStatsWrapper.flushCache()
        viewPager.adapter!!.notifyDataSetChanged()
        then.run()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)

            if (item.icon != null) {
                val newIcon = item.icon
                newIcon!!.mutate().setColorFilter(getThemeAccentColor(), PorterDuff.Mode.SRC_IN)
                item.icon = newIcon
            }
        }
        return true
    }

    /**
     * @return The accent color from the currently set theme
     */
    private fun getThemeAccentColor(): Int {
        val outValue = TypedValue()
        theme.resolveAttribute(android.R.attr.colorAccent, outValue, true)

        return outValue.data
    }
}
