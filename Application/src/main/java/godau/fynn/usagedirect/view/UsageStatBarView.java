package godau.fynn.usagedirect.view;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.wrapper.Interval;
import godau.fynn.usagedirect.wrapper.NaturalText;
import im.dacer.androidcharts.bar.Line;
import im.dacer.androidcharts.bar.Value;

public class UsageStatBarView extends FramedBarView {
    public UsageStatBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UsageStatBarView(Context context) {
        super(context);
    }

    /**
     * Set the bar view's data to the provided map of days to longs. Data
     * is displayed in the order of the map's key set. The day in month is
     * used as a label. Adds scale to bar view.
     */
    public void setData(Map<Day, Long> map) {
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
     * Set the bar view's data to the provided list of accumulated times.
     * The last integer is assumed to be for the currently ongoing period,
     * the previous integers to be the respective periods before that.
     * Also adds scale to bar view.
     *
     * @param interval Interval for bottom text calculation
     */
    public void setSystemData(List<Integer> accumulatedTimes, Interval interval) {


        Value[] values = new Value[accumulatedTimes.size()];

        for (int i = 0; i < accumulatedTimes.size(); i++) {
            values[i] = new Value(accumulatedTimes.get(i),
                    NaturalText.formatShort(interval, accumulatedTimes.size() - 1 - i)
            );
        }

        int max = Collections.max(accumulatedTimes);

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
        int maxHours = (max / 60 / 60) + 1;
        int frequency = 1;
        while (maxHours / 10 >= frequency) {
            frequency *= 10;
        }

        // Add lines
        // Note: maxHours is overestimating the total hours by up to one
        Line[] lines = new Line[(maxHours - 1) / frequency];

        for (int counter = frequency, i = 0; counter < maxHours; counter += frequency, i++) {
            //if (lines.length > 5 & i != 0) {
            //    lines[i] = new Line(counter * 60 * 60);
            //} else {
                lines[i] = new Line(counter * 60 * 60, String.valueOf(counter));
            //}
        }

        barView.setVerticalLines(lines, chartMax);

    }
}
