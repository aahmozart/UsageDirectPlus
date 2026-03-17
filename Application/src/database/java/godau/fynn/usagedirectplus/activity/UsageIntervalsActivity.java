package godau.fynn.usagedirectplus.activity;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.persistence.HistoryDatabase;
import godau.fynn.usagedirectplus.persistence.UsageInterval;
import godau.fynn.usagedirectplus.thread.icon.IconThread;
import godau.fynn.usagedirectplus.view.adapter.UsageIntervalAdapter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

public class UsageIntervalsActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_usage_intervals);

        String applicationId = getIntent().getStringExtra("applicationId");
        long day = getIntent().getLongExtra("day", 0);

        // Set action bar title to app name
        String appName = IconThread.nameMap.get(applicationId);
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            if (appName != null) {
                getActionBar().setTitle(appName);
            } else {
                getActionBar().setTitle(applicationId);
            }
            LocalDate date = LocalDate.ofEpochDay(day);
            getActionBar().setSubtitle(
                    date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
            );
        }

        // Set app icon
        ImageView appIcon = findViewById(R.id.app_icon);
        Drawable icon = IconThread.iconMap.get(applicationId);
        if (icon != null) {
            appIcon.setImageDrawable(icon);
        } else {
            appIcon.setVisibility(View.GONE);
        }

        // Compute day boundaries
        LocalDate date = LocalDate.ofEpochDay(day);
        long start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        new Thread(() -> {
            List<UsageInterval> intervals = HistoryDatabase.get(this)
                    .getUsageIntervalDao()
                    .getByAppAndTimeRange(applicationId, start, end);

            runOnUiThread(() -> {
                if (intervals.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    findViewById(R.id.empty_message).setVisibility(View.VISIBLE);
                    findViewById(R.id.total_time).setVisibility(View.GONE);
                } else {
                    // Compute and display total time
                    long totalMs = 0;
                    for (UsageInterval interval : intervals) {
                        totalMs += interval.getEndTime() - interval.getBeginTime();
                    }
                    long totalMinutes = totalMs / 60000;
                    TextView totalTime = findViewById(R.id.total_time);
                    if (totalMinutes >= 60) {
                        totalTime.setText(totalMinutes / 60 + "h " + totalMinutes % 60 + " min");
                    } else {
                        totalTime.setText(totalMinutes + " min");
                    }

                    recyclerView.setAdapter(new UsageIntervalAdapter(intervals));
                }
            });
        }).start();
    }
}
