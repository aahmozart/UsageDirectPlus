package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.ColoredSimpleUsageStat;
import im.dacer.androidcharts.bar.BarView;
import im.dacer.androidcharts.bar.Line;
import im.dacer.androidcharts.bar.MultiValue;
import im.dacer.androidcharts.bar.Value;

import java.time.LocalDate;
import java.util.ArrayList;

public abstract class UsageStatBarChart extends Fragment {

    private TextView textView;
    protected BarView barView;
    private HorizontalScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayout(), container, false);

        textView = view.findViewById(R.id.bar_chart_label);
        barView = view.findViewById(R.id.bar_chart);
        scrollView = view.findViewById(R.id.bar_chart_scroll);

        return view;
    }

    protected @LayoutRes int getLayout() {
        return R.layout.content_bar_view;
    }

    protected void setText(@StringRes int text) {
        textView.setText(text);
    }

    /**
     * Set the bar view's data for each of the days in the <code>days</code>
     * array, in its order.
     */
    protected abstract void setData(long[] days, ColoredSimpleUsageStat[] coloredUsageStats);

    /**
     * Calculate positions of vertical lines and their texts for scale
     *
     * @param chartMax Maximum value (upper border) in the chart
     */
    protected void addScale(int chartMax) {
        // Calculate vertical line frequency
        int maxHours = (chartMax / 60 / 60) + 1;
        int frequency = 1;
        while (maxHours / frequency > 10) {
            // If a power of 10, increase by the factor 2
            if (Math.log10(frequency) % 1 == 0) {
                frequency *= 2;
            } else {
                // If last step was a multiplication with 2, go back and multiply with 5
                if (Math.log10(frequency / 2) % 1 == 0) {
                    frequency = frequency / 2 * 5;
                } else {
                    // If last step was this multiplication with 5, go to next power of 10
                    frequency *= 2;
                }
            }
        }

        // Add lines
        // Note: maxHours is overestimating the total hours by up to one
        Line[] lines = new Line[(maxHours - 1) / frequency];

        for (int counter = frequency, i = 0; counter < maxHours; counter += frequency, i++) {
            lines[i] = new Line(counter * 60 * 60, String.valueOf(counter));
        }

        barView.setVerticalLines(lines, chartMax);

    }

    protected void scrollToEnd() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(Integer.MAX_VALUE / 2 /* Integer.MAX_VALUE broke things… */, 0);
            }
        });
    }
}
