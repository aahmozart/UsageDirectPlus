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

package godau.fynn.usagedirectplus.view

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import godau.fynn.usagedirectplus.databinding.ContentClockPieChartBinding
import im.dacer.androidcharts.clockpie.ClockPieView

class FramedClockPieView(context: Context) : FrameLayout(context) {

    private val binding: ContentClockPieChartBinding
    val clockPieView: ClockPieView

    init {
        binding = ContentClockPieChartBinding.inflate(LayoutInflater.from(context), this, true)
        clockPieView = binding.clockPieView
    }

    fun setText(text: String) {
        binding.clockPieLabel.text = text
    }
}
