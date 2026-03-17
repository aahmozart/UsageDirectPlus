package godau.fynn.usagedirectplus.view.dialog

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.wrapper.ComponentForegroundStat
import im.dacer.androidcharts.clockpie.ClockPieSegment
import im.dacer.androidcharts.clockpie.ClockPieView

class ClockPieChartDialog(
    componentForegroundStats: List<ComponentForegroundStat>,
    backgroundSegment: ClockPieSegment,
    context: Context
) : MaterialAlertDialogBuilder(context) {

    init {
        val clockPieView = ClockPieView(context)

        val clockPieHelperList = ArrayList<ClockPieSegment>()

        for (stat in componentForegroundStats) {
            clockPieHelperList.add(stat.asClockPieSegment())
        }

        clockPieView.setData(clockPieHelperList)

        clockPieView.setBackgroundSegment(backgroundSegment)
        clockPieView.setBackgroundColor(getContext().getColor(R.color.notice_blue))
        // TODO set clock pie view pie segment color

        setView(clockPieView)
    }
}
