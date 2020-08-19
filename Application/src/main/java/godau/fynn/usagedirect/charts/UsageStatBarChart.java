package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.R;
import im.dacer.androidcharts.bar.BarView;
import im.dacer.androidcharts.bar.Line;
import im.dacer.androidcharts.bar.Value;

import java.util.Collections;
import java.util.Map;

public abstract class UsageStatBarChart extends Fragment {

    private TextView textView;
    protected BarView barView;
    private HorizontalScrollView scrollView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_bar_view, container, false);

        textView = view.findViewById(R.id.bar_chart_label);
        barView = view.findViewById(R.id.bar_chart);
        scrollView = view.findViewById(R.id.bar_chart_scroll);

        return view;
    }

    protected void setText(@StringRes int text) {
        textView.setText(text);
    }

    /**
     * Set the bar view's data to the provided map of days to longs. Data
     * is displayed in the order of the map's key set. The day in month is
     * used as a label. Adds scale to bar view.
     */
    protected void setData(Map<Day, Long> map) {
        // Collect data and labels

        Value[] values = new Value[map.size()];

        int i = 0;
        for (Day d : map.keySet()) {
            int seconds = (int) (map.get(d) / 1000);
            values[i++] = new Value(seconds, String.valueOf(d.day));
        }

        int max = (int) (Collections.max(map.values()) / 1000);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setData(values, chartMax);

        addScale(max, chartMax);

    }

    /**
     * Calculate positions of vertical lines and their texts for scale
     *
     * @param max Maximum second value in the dataset displayed in the
     *            chart
     * @param chartMax Maximum value (upper border) in the chart
     */
    protected void addScale(int max, int chartMax) {
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
