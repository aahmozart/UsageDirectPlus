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

package godau.fynn.usagedirect.view;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import godau.fynn.usagedirect.R;
import im.dacer.androidcharts.clockpie.ClockPieView;

public class FramedClockPieView extends LinearLayout {

    private final TextView textView;
    private final ClockPieView clockPieView;

    public FramedClockPieView(Context context) {
        super(context);

        setOrientation(VERTICAL);

        int margin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                8,
                context.getResources().getDisplayMetrics()
        );

        textView = new TextView(context);
        textView.setText(R.string.charts_clock_pie);
        textView.setTextColor(Color.BLACK);
        LayoutParams textLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textLayoutParams.setMargins(margin, margin, margin, margin);
        textView.setLayoutParams(textLayoutParams);
        addView(textView);

        int size = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                220,
                context.getResources().getDisplayMetrics()
        );
        clockPieView = new ClockPieView(context);
        LayoutParams layoutParams = new LinearLayout.LayoutParams(size, size);
        layoutParams.gravity = Gravity.CENTER;
        clockPieView.setLayoutParams(layoutParams);
        addView(clockPieView);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public ClockPieView getClockPieView() {
        return clockPieView;
    }
}
