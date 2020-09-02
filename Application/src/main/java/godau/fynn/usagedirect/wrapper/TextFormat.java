package godau.fynn.usagedirect.wrapper;

import android.content.res.Resources;
import godau.fynn.usagedirect.R;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

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
                            "MMM d"  // Abbr. month and day ("Jul 11")
            ));
        }
    }

    public static String formatWeekday(DayOfWeek weekday) {
        return weekday.getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault());
    }
    
}
