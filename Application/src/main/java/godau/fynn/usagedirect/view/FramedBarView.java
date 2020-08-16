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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import godau.fynn.usagedirect.R;
import im.dacer.androidcharts.bar.BarView;

public class FramedBarView extends FrameLayout {

    private final TextView textView;
    protected final BarView barView;
    private final HorizontalScrollView scrollView;

    public FramedBarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        addView(
                LayoutInflater.from(context).inflate(R.layout.content_bar_view, this, false)
        );

        textView = findViewById(R.id.bar_chart_label);
        barView = findViewById(R.id.bar_chart);
        scrollView = findViewById(R.id.bar_chart_scroll);
    }

    public FramedBarView(Context context) {
        this(context, null);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public BarView getBarView() {
        return barView;
    }

    public void scrollToEnd() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(Integer.MAX_VALUE / 2 /* Integer.MAX_VALUE broke things… */, 0);
            }
        });
    }
}
