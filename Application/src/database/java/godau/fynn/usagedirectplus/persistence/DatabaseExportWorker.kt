package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.FileInputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DatabaseExportWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        val prefs = context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
        val uriString = prefs.getString(PREF_EXPORT_URI, null)

        if (uriString == null) {
            Log.w(TAG, "No export directory configured")
            return Result.failure()
        }

        val treeUri = android.net.Uri.parse(uriString)
        val directory = DocumentFile.fromTreeUri(context, treeUri)
        if (directory == null || !directory.exists() || !directory.canWrite()) {
            Log.e(TAG, "Export directory not accessible")
            return Result.failure()
        }

        val useFixedFilename = prefs.getBoolean(PREF_EXPORT_FIXED_FILENAME, false)
        val outputFile = resolveOutputFile(directory, useFixedFilename)
        if (outputFile == null) {
            Log.e(TAG, "Could not create output file")
            return Result.failure()
        }

        val databaseFile = context.getDatabasePath(HistoryDatabase.DATABASE_NAME)
        if (!databaseFile.exists()) {
            Log.e(TAG, "Database file does not exist")
            return Result.failure()
        }

        try {
            FileInputStream(databaseFile).use { inputStream ->
                val openMode = if (useFixedFilename && outputFile.length() > 0) "wt" else "w"
                context.contentResolver.openOutputStream(outputFile.uri, openMode).use { outputStream ->
                    if (outputStream == null) {
                        Log.e(TAG, "Could not open output stream")
                        return Result.failure()
                    }

                    val buffer = ByteArray(4096)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }
                    outputStream.flush()

                    Log.i(TAG, "Database exported to ${outputFile.name}")
                    return Result.success()
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Export failed", e)
            return Result.failure()
        }
    }

    companion object {
        private const val TAG = "DatabaseExportWorker"
        const val WORK_NAME = "database_export"
        const val PREF_EXPORT_ENABLED = "export_enabled"
        const val PREF_EXPORT_URI = "export_uri"
        const val PREF_EXPORT_INTERVAL_HOURS = "export_interval_hours"
        const val PREF_EXPORT_FIXED_FILENAME = "export_fixed_filename"
        private const val FIXED_FILENAME = "usageDirectPlus.sqlite3"
        private const val MIME_TYPE = "application/vnd.sqlite3"

        @JvmStatic
        internal fun resolveOutputFile(
            directory: DocumentFile,
            useFixedFilename: Boolean
        ): DocumentFile? {
            return if (useFixedFilename) {
                directory.findFile(FIXED_FILENAME)
                    ?: directory.createFile(MIME_TYPE, FIXED_FILENAME)
            } else {
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                directory.createFile(MIME_TYPE, "usageDirectPlus-$timestamp.sqlite3")
            }
        }

        /**
         * Schedules periodic database export with the given interval.
         */
        @JvmStatic
        fun schedule(context: Context, intervalHours: Long) {
            val request = PeriodicWorkRequest.Builder(
                DatabaseExportWorker::class.java,
                intervalHours, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        /**
         * Cancels any scheduled periodic database export.
         */
        @JvmStatic
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
