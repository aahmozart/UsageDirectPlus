package godau.fynn.usagedirect.persistence;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DatabaseExportWorker extends Worker {

    private static final String TAG = "DatabaseExportWorker";
    public static final String WORK_NAME = "database_export";
    public static final String PREF_EXPORT_ENABLED = "export_enabled";
    public static final String PREF_EXPORT_URI = "export_uri";
    public static final String PREF_EXPORT_INTERVAL_HOURS = "export_interval_hours";

    public DatabaseExportWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        String uriString = context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
                .getString(PREF_EXPORT_URI, null);

        if (uriString == null) {
            Log.w(TAG, "No export directory configured");
            return Result.failure();
        }

        Uri treeUri = Uri.parse(uriString);
        DocumentFile directory = DocumentFile.fromTreeUri(context, treeUri);
        if (directory == null || !directory.exists() || !directory.canWrite()) {
            Log.e(TAG, "Export directory not accessible");
            return Result.failure();
        }

        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(new Date());
        String fileName = "usageDirectPlus-" + timestamp + ".sqlite3";

        DocumentFile outputFile = directory.createFile("application/vnd.sqlite3", fileName);
        if (outputFile == null) {
            Log.e(TAG, "Could not create output file");
            return Result.failure();
        }

        File databaseFile = context.getDatabasePath(HistoryDatabase.DATABASE_NAME);
        if (!databaseFile.exists()) {
            Log.e(TAG, "Database file does not exist");
            return Result.failure();
        }

        try (FileInputStream inputStream = new FileInputStream(databaseFile);
             OutputStream outputStream = context.getContentResolver().openOutputStream(outputFile.getUri())) {

            if (outputStream == null) {
                Log.e(TAG, "Could not open output stream");
                return Result.failure();
            }

            byte[] buffer = new byte[4096];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();

            Log.i(TAG, "Database exported to " + fileName);
            return Result.success();

        } catch (IOException e) {
            Log.e(TAG, "Export failed", e);
            return Result.failure();
        }
    }

    /**
     * Schedules periodic database export with the given interval.
     */
    public static void schedule(Context context, long intervalHours) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DatabaseExportWorker.class,
                intervalHours, TimeUnit.HOURS
        )
                .setConstraints(new Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build())
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    /**
     * Cancels any scheduled periodic database export.
     */
    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}
