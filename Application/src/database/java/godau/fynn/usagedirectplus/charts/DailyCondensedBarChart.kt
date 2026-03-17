package godau.fynn.usagedirectplus.charts

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import godau.fynn.usagedirectplus.R
import im.dacer.androidcharts.bar.CondensedBarView
import java.time.LocalDate
import java.time.temporal.WeekFields

class DailyCondensedBarChart : DailyBarChart() {

    override fun getLayout(): Int {
        return R.layout.content_bar_view_condensed
    }

    @StringRes
    override fun getText(): Int {
        return R.string.charts_bar_daily_condensed
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barView.setZeroLineEnabled(true)
        (barView as CondensedBarView).setBarWidth(8)
        (barView as CondensedBarView).setLabelIndicatorMode(CondensedBarView.LabelIndicatorMode.IN_CHART)
    }

    override fun getLabel(date: LocalDate): String? {
        val week = WeekFields.ISO

        // Add label at the beginning of each week
        return if (date.dayOfWeek == week.firstDayOfWeek) {
            date.get(week.weekOfYear()).toString()
        } else {
            null
        }
    }
}
