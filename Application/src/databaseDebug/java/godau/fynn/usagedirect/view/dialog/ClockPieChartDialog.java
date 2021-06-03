package godau.fynn.usagedirect.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TimePicker;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.ComponentForegroundStat;
import godau.fynn.usagedirect.wrapper.EventLogWrapper;
import im.dacer.androidcharts.clockpie.ClockPieSegment;
import im.dacer.androidcharts.clockpie.ClockPieView;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class ClockPieChartDialog extends AlertDialog.Builder {

    public ClockPieChartDialog(List<ComponentForegroundStat> componentForegroundStats,
                               ClockPieSegment backgroundSegment, Context context) {
        super(context);

        ClockPieView clockPieView = new ClockPieView(context);

        final ArrayList<ClockPieSegment> clockPieHelperList = new ArrayList<>();


        for (ComponentForegroundStat stat : componentForegroundStats) {
            clockPieHelperList.add(stat.asClockPieSegment());
        }

        clockPieView.setData(clockPieHelperList);

        clockPieView.setBackgroundSegment(backgroundSegment);
        clockPieView.setBackgroundColor(getContext().getResources().getColor(R.color.notice_blue));
        // TODO set clock pie view pie segment color

        setView(clockPieView);

    }

}
