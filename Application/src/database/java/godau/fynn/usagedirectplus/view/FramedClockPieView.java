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

package godau.fynn.usagedirectplus.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import godau.fynn.usagedirectplus.R;
import im.dacer.androidcharts.clockpie.ClockPieView;

public class FramedClockPieView extends FrameLayout {

    private final TextView textView;
    private final ClockPieView clockPieView;

    public FramedClockPieView(Context context) {
        super(context);

        LayoutInflater.from(context).inflate(R.layout.content_clock_pie_chart, this);

        textView = findViewById(R.id.clock_pie_label);
        clockPieView = findViewById(R.id.clock_pie_view);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public ClockPieView getClockPieView() {
        return clockPieView;
    }
}
