package godau.fynn.usagedirect.view.dialog;

import android.app.AlertDialog;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.room.Room;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.EventLogRunnable;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.UsageStatsDao;

import static android.content.Context.JOB_SCHEDULER_SERVICE;
import static godau.fynn.usagedirect.persistence.HistoryDatabase.DATABASE_NAME;

public class DatabaseDebugDialog extends AlertDialog.Builder {

    private UsageStatsDao usageStats;

    private TextView status;
    private Button insert;

    public DatabaseDebugDialog(Context context) {
        super(context);

        setView(R.layout.dialog_database);

        usageStats = HistoryDatabase.getUsageStatsDao(context);
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

        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                status.setText(R.string.db_wait);
                insert.setEnabled(false);

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        new EventLogRunnable(getContext()).run();
                        updateViews();

                    }
                }).start();
            }
        });

        updateViews();

        return dialog;
    }

    private void updateViews() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int daysStored = usageStats.getDaysStoredAmount();
                final long totalHours = usageStats.getTotalTimeUsed() / 1000 / 60 / 60;

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        status.setText(getContext().getString(R.string.db_status, daysStored, totalHours));
                        insert.setEnabled(true);
                    }
                });
            }
        }).start();
    }
}
