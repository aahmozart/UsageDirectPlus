package godau.fynn.usagedirectplus.wrapper

import android.content.res.Resources
import godau.fynn.usagedirectplus.R
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date

abstract class TextFormat private constructor() {
    companion object {
        @JvmStatic
        fun formatDay(offset: Int, resources: Resources): String {
            return when {
                offset == 0 -> resources.getString(R.string.ts_today)
                offset == 1 -> resources.getString(R.string.ts_yesterday)
                else -> LocalDate.now().minusDays(offset.toLong()).format(
                    DateTimeFormatter.ofPattern(
                        if (offset < 7) "EEEE" else "MMM dd"
                    )
                )
            }
        }

        @JvmStatic
        fun formatWeekday(weekday: DayOfWeek): String {
            val c = Calendar.getInstance()
            c.set(Calendar.DAY_OF_WEEK, weekday.value + 1 % 7)
            return SimpleDateFormat("EEE").format(Date(c.timeInMillis))
        }
    }
}
