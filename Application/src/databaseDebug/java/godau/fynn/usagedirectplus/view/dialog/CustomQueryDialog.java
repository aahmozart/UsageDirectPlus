package godau.fynn.usagedirectplus.view.dialog;

import android.app.AlertDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.persistence.EventLogRunnable;
import godau.fynn.usagedirectplus.persistence.HistoryDatabase;
import godau.fynn.usagedirectplus.persistence.UsageStatsDao;
import godau.fynn.usagedirectplus.wrapper.ComponentForegroundStat;
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper;
import godau.fynn.usagedirectplus.wrapper.UsageStatsManagerWrapper;
import im.dacer.androidcharts.clockpie.ClockPieSegment;

import java.time.*;
import java.util.List;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class CustomQueryDialog extends AlertDialog.Builder {


    private TimePicker fromTime, toTime;

    public CustomQueryDialog(Context context) {
        super(context);

        setView(R.layout.dialog_custom_query);


        setOnDismissListener((dialog) -> {
        });
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();

        Button queryButton = dialog.findViewById(R.id.button_query);
        fromTime = dialog.findViewById(R.id.time_from);
        toTime = dialog.findViewById(R.id.time_to);

        fromTime.setIs24HourView(true);
        toTime.setIs24HourView(true);


        queryButton.setOnClickListener(v -> new Thread(() -> {

            EventLogWrapper eventLog = new EventLogWrapper(getContext());

            long start = LocalTime.of(
                    fromTime.getCurrentHour(),
                    fromTime.getCurrentMinute()
            )
                    .atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            long end = LocalTime.of(
                    toTime.getCurrentHour(),
                    toTime.getCurrentMinute()
            )
                    .atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            List<ComponentForegroundStat> stats = eventLog.getForegroundStatsByTimestamps(start, end);

            new Handler(Looper.getMainLooper()).post(() ->
                    new ClockPieChartDialog(
                            stats,
                            new ClockPieSegment(
                                    fromTime.getCurrentHour(), fromTime.getCurrentMinute(),
                                    toTime.getCurrentHour(), toTime.getCurrentMinute()
                            ),
                            getContext()
                    ).show()
            );

        }).start());

        return dialog;
    }

}
