package godau.fynn.usagedirectplus.wrapper;

import android.content.res.Resources;
import godau.fynn.usagedirectplus.R;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public abstract class TextFormat {
    private TextFormat() {}
    
    public static String formatDay(int offset, Resources resources) {
        if (offset == 0) {
            return resources.getString(R.string.ts_today);
        } else if (offset == 1) {
            return resources.getString(R.string.ts_yesterday);
        } else {
            return LocalDate.now().minusDays(offset).format(DateTimeFormatter.ofPattern(
                    offset < 7?
                            "EEEE" : // Weekday ("Saturday")
                            "MMM dd"  // Abbr. month and two-digit day ("Jul 11")
            ));
        }
    }

    public static String formatWeekday(DayOfWeek weekday) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, weekday.getValue() + 1 % 7);
        return new SimpleDateFormat("EEE").format(new Date(c.getTimeInMillis()));
    }
    
}
