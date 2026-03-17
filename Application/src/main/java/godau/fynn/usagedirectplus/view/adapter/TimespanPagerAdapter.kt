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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import godau.fynn.usagedirectplus.databinding.ContentTimespanBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Parent pager, pages [UsageListViewPagerAdapter]
 */
abstract class TimespanPagerAdapter(protected val context: AppCompatActivity) : PagerAdapter(),
    ViewPager.OnPageChangeListener {

    private val viewPagerMap = HashMap<Int, ViewPager>()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding = ContentTimespanBinding.inflate(LayoutInflater.from(context), container, false)
        container.addView(binding.root)

        context.lifecycleScope.launch(Dispatchers.IO) {
            prepare(position)

            withContext(Dispatchers.Main) {
                binding.viewpager.adapter = getUsageListViewPagerAdapter(position)

                binding.viewpager.currentItem = binding.viewpager.adapter!!.count - 1
                binding.viewpager.offscreenPageLimit = 2

                binding.viewpagertab.setViewPager(binding.viewpager)

                viewPagerMap[position] = binding.viewpager
            }
        }

        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
        viewPagerMap.remove(position)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` || (`object` as? View)?.parent === `object`
    }

    override fun notifyDataSetChanged() {
        for (pager in viewPagerMap.values) {
            pager.adapter!!.notifyDataSetChanged()
        }
    }

    override fun onPageSelected(position: Int) {
        // Scroll back to initial position
        if (viewPagerMap.containsKey(position)) {
            val viewPager = viewPagerMap[position]!!
            viewPager.currentItem = viewPager.adapter!!.count - 1
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    abstract fun prepare(position: Int)

    abstract fun getUsageListViewPagerAdapter(position: Int): UsageListViewPagerAdapter
}
