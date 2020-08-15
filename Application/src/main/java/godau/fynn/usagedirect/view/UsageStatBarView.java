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
        final List<String> labels = new ArrayList<>();
        final List<Integer> data = new ArrayList<>();
        for (Day d : map.keySet()) {
            labels.add(String.valueOf(d.day));
            int seconds = (int) (map.get(d) / 1000);
            data.add(seconds);
        }

        setData(data);
        barView.setBottomTextList(labels);
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


        ArrayList<String> bottomText = new ArrayList<>();

        for (int i = accumulatedTimes.size() - 1; i >= 0; i--) {
            bottomText.add(NaturalText.formatShort(interval, i));
        }

        setData(accumulatedTimes);
        barView.setBottomTextList(bottomText);
    }

    /**
     * Set the provided integer list as data for the chart. Afterwards,
     * add scale to bar view.
     *
     * @param data A list of second values
     */
    protected void setData(List<Integer> data) {
        int max = Collections.max(data);

        // Use maximum of timespan plus 30 minutes so no bar hits the top
        int chartMax = max + (60 * 30);

        barView.setDataList(data, chartMax);

        addScale(max, chartMax);
    }

    /**
     * Calculate positions of vertical lines and their texts for scale
     *
     * @param max Maximum second value in the dataset displayed in the
     *            chart
     * @param chartMax Maximum value (upper border) in the chart
     */
    private void addScale(int max, int chartMax) {
        // Calculate vertical line frequency
        int maxHours = (max / 60 / 60) + 1;
        int frequency = 1;
        while (maxHours / 10 >= frequency) {
            frequency *= 10;
        }

        // Add lines
        List<Integer> lines = new ArrayList<>();
        List<String> lineLabels = new ArrayList<>();
        int counter = frequency;
        do {
            lines.add(counter * 60 * 60);
            lineLabels.add(String.valueOf(counter));
        } while ((counter += frequency) < maxHours);

        barView.setVerticalLines(lines, chartMax);

        // Don't display more than 4 vertical line labels
        if (lineLabels.size() > 4) {
            lineLabels.clear();
            lineLabels.add(String.valueOf(frequency));
        }

        barView.setVerticalLines(lines, chartMax);
        barView.setVerticalLineLabels(lineLabels);

    }
}
