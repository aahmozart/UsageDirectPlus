package godau.fynn.usagedirect.view;

import android.content.Context;
import android.util.AttributeSet;
import godau.fynn.usagedirect.Day;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.NaturalText;

import java.util.*;

public class WeeklyAverageBarView extends UsageStatBarView {
    public WeeklyAverageBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setText(context.getString(R.string.chart_average));
    }

    public WeeklyAverageBarView(Context context) {
        super(context);
        setText(context.getString(R.string.chart_average));
    }

    @Override
    public void setData(Map<Day, Long> map) {

        Map<Integer, Average> weekdayMap = new HashMap<>();

        for (Day d : map.keySet()) {
            int seconds = (int) (map.get(d) / 1000);

            Average a;
            Integer weekday = d.asCalendar().get(Calendar.DAY_OF_WEEK);
            if (weekdayMap.containsKey(weekday)) {
                a = weekdayMap.get(weekday);
            } else {
                weekdayMap.put(weekday, a = new Average());
            }

            a.add(seconds);
        }

        List<String> labels = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = Calendar.MONDAY; i <= Calendar.SATURDAY; i++) {

            if (weekdayMap.containsKey(i)) {
                values.add(weekdayMap.get(i).average());
            } else {
                values.add(0);
            }

            labels.add(NaturalText.formatWeekday(i));
        }

        // Sunday
        {
            if (weekdayMap.containsKey(Calendar.SUNDAY)) {
                values.add(weekdayMap.get(Calendar.SUNDAY).average());
            } else {
                values.add(0);
            }

            labels.add(NaturalText.formatWeekday(Calendar.SUNDAY));
        }

        barView.setBottomTextList(labels);

        setData(values);
    }


    private static class Average {
        private int count;
        private int sum;

        public void add(int value) {
            count++;
            sum += value;
        }

        public int average() {
            return sum / count;
        }
    }
}
