package godau.fynn.usagedirectplus.view.dialog;

import android.app.AlertDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.persistence.EventLogRunnable;
import godau.fynn.usagedirectplus.persistence.HistoryDatabase;
import godau.fynn.usagedirectplus.persistence.UsageStatsDao;

import static android.content.Context.JOB_SCHEDULER_SERVICE;

public class DatabaseDebugDialog extends AlertDialog.Builder {

    private final HistoryDatabase database;
    private final UsageStatsDao usageStats;

    private TextView status;
    private Button insert;

    public DatabaseDebugDialog(Context context) {
        super(context);

        setView(R.layout.dialog_database);

        database = HistoryDatabase.get(context);
        usageStats = database.getUsageStatsDao();

        setOnDismissListener((dialog) -> database.close());
    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog = super.show();

        status = dialog.findViewById(R.id.text_status);
        insert = dialog.findViewById(R.id.button_insert);
        final CheckBox schedule = dialog.findViewById(R.id.button_schedule);

        final JobScheduler scheduler = (JobScheduler) getContext().getSystemService(JOB_SCHEDULER_SERVICE);
        boolean scheduled = scheduler.getAllPendingJobs().size() > 0;

        schedule.setChecked(scheduled);
        schedule.setEnabled(false);

        insert.setEnabled(false);

        insert.setOnClickListener(v -> {

            status.setText(R.string.db_wait);
            insert.setEnabled(false);

            new Thread(() -> {

                new EventLogRunnable(getContext()).run();
                updateViews();

            }).start();
        });

        updateViews();

        return dialog;
    }

    private void updateViews() {
        new Thread(() -> {
            final int daysStored = usageStats.getDaysStoredAmount();
            final long totalHours = usageStats.getTotalTimeUsed() / 1000 / 60 / 60;

            new Handler(Looper.getMainLooper()).post(() -> {
                status.setText(getContext().getString(R.string.db_status, daysStored, totalHours));
                insert.setEnabled(true);
            });
        }).start();
    }
}
