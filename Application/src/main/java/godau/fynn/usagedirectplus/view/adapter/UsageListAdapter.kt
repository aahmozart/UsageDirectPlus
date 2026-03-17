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

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter
import godau.fynn.usagedirectplus.BuildConfig
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.databinding.RowUsageBinding
import godau.fynn.usagedirectplus.databinding.RowUsageTotalBinding
import godau.fynn.usagedirectplus.thread.icon.IconThread
import org.ocpsoft.prettytime.PrettyTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

/**
 * Recycler view adapter for displaying (aggregated) usage stats
 */
class UsageListAdapter : SimpleRecyclerViewAdapter<SimpleUsageStat, UsageListAdapter.ViewHolder>() {

    fun interface OnItemClickListener {
        fun onItemClick(stat: SimpleUsageStat)
    }

    private var lastUsedMap: Map<String, Long>? = null
    private var colorMap: Map<String, Int>? = null
    private val prettyTime = PrettyTime()
    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        this.onItemClickListener = listener
    }

    class ViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val packageName: TextView
        val lastTimeUsed: TextView
        val timeUsed: TextView
        val appIcon: ImageView

        init {
            when (binding) {
                is RowUsageTotalBinding -> {
                    packageName = binding.textviewPackageName
                    lastTimeUsed = binding.textviewLastTimeUsed
                    timeUsed = binding.textviewTimeUsed
                    appIcon = binding.appIcon
                }
                is RowUsageBinding -> {
                    packageName = binding.textviewPackageName
                    lastTimeUsed = binding.textviewLastTimeUsed
                    timeUsed = binding.textviewTimeUsed
                    appIcon = binding.appIcon
                }
                else -> throw IllegalArgumentException("Unexpected binding type")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 0 for the first, 1 for all other positions
        return minOf(position, 1)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) {
            val binding = RowUsageTotalBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            return ViewHolder(binding)
        } else {
            val binding = RowUsageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
            val viewHolder = ViewHolder(binding)

            // For performance, only set OnClickListener once
            viewHolder.appIcon.setOnClickListener {
                // Launch app that this icon is associated with
                try {
                    val packageName = viewHolder.appIcon.tag as String
                    val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                    context.startActivity(intent)
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    Toast.makeText(context, R.string.launch_unavailable, Toast.LENGTH_SHORT).show()
                }
            }

            binding.root.setOnClickListener { v1 ->
                val stat = v1.tag as? SimpleUsageStat
                if (stat != null && onItemClickListener != null) {
                    onItemClickListener!!.onItemClick(stat)
                }
            }

            // Register for context menu
            (context as Activity).registerForContextMenu(binding.root)

            return viewHolder
        }
    }

    @SuppressLint("ResourceType") // apparently incorrect annotation of TypedArray.getColor causes lint to complain
    override fun onBindViewHolder(viewHolder: ViewHolder, usageStat: SimpleUsageStat, position: Int) {
        val name = IconThread.nameMap[usageStat.applicationId]
        viewHolder.packageName.text = name ?: usageStat.applicationId

        viewHolder.lastTimeUsed.visibility = View.GONE

        if (position > 0) {
            // Set colors (background and text)
            val array = if (colorMap != null && colorMap!!.containsKey(usageStat.applicationId)) {
                @ColorInt val backgroundColor = colorMap!![usageStat.applicationId]!!

                viewHolder.itemView.setBackgroundColor(backgroundColor)

                @StyleRes val style = if (Color.luminance(backgroundColor) < 0.4f) {
                    // System dark theme
                    android.R.style.Theme_DeviceDefault
                } else {
                    // System light theme
                    android.R.style.Theme_DeviceDefault_Light
                }

                val theme = context.resources.newTheme()
                theme.applyStyle(style, true)

                theme.obtainStyledAttributes(
                    intArrayOf(
                        android.R.attr.textColorPrimary, android.R.attr.textColorSecondary
                    )
                )
            } else {
                viewHolder.itemView.background = null

                // Current theme (DayNight from API 29 onwards)
                context.theme.obtainStyledAttributes(
                    intArrayOf(
                        android.R.attr.textColorPrimary, android.R.attr.textColorSecondary
                    )
                )
            }

            viewHolder.packageName.setTextColor(
                array.getColor(0, Color.RED)
            )

            viewHolder.timeUsed.setTextColor(
                array.getColor(1, Color.RED)
            )
            viewHolder.lastTimeUsed.setTextColor(
                array.getColor(1, Color.RED)
            )

            array.recycle()
        }

        if (lastUsedMap != null && lastUsedMap!!.containsKey(usageStat.applicationId)) {
            val lastUsed = lastUsedMap!![usageStat.applicationId]!!

            val lastUsedInstant = Instant.ofEpochMilli(lastUsed)
            val day = LocalDate.ofEpochDay(usageStat.day)
            val startOfDay = day.atStartOfDay(ZoneId.systemDefault()).toInstant()
            val endOfDay = day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()

            val lastUsedOnSameDay = lastUsedInstant.isAfter(startOfDay) && lastUsedInstant.isBefore(endOfDay)

            if (lastUsedOnSameDay) {
                viewHolder.lastTimeUsed.visibility = View.VISIBLE

                if (day.isEqual(LocalDate.now())) {
                    if (usageStat.applicationId == BuildConfig.APPLICATION_ID)
                        viewHolder.lastTimeUsed.setText(R.string.last_used_now)
                    else if (lastUsed > 1) {
                        viewHolder.lastTimeUsed.text = prettyTime.format(Date(lastUsed))
                    } else {
                        viewHolder.lastTimeUsed.setText(R.string.not_used)
                    }
                } else {
                    viewHolder.lastTimeUsed.setText(R.string.last_used_this_day)
                }
            } else {
                viewHolder.lastTimeUsed.visibility = View.GONE
            }
        } else {
            viewHolder.lastTimeUsed.visibility = View.GONE
        }

        val secondsUsed = usageStat.timeUsed / 1000
        viewHolder.timeUsed.text = context.getString(
            R.string.time_used_time_only,
            secondsUsed / 3600, (secondsUsed / 60) % 60, secondsUsed % 60
        )

        viewHolder.appIcon.setImageDrawable(IconThread.iconMap[usageStat.applicationId])

        viewHolder.appIcon.tag = usageStat.applicationId

        viewHolder.itemView.tag = usageStat
    }

    fun setUsageStatsList(usageStats: List<SimpleUsageStat>?) {
        content.clear()
        if (usageStats != null) {
            // Calculate total amount
            var total = 0L
            for (stat in usageStats) {
                total += stat.timeUsed
            }
            content.add(SimpleUsageStat(0, total, context.getString(R.string.total)))

            content.addAll(usageStats)
        }
        notifyDataSetChanged()
    }

    fun setLastUsedMap(map: Map<String, Long>) {
        lastUsedMap = map
    }

    fun setColorMap(map: Map<String, Int>) {
        colorMap = map
    }
}
