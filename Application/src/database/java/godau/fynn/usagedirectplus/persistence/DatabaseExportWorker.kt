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
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream

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
        val compress = prefs.getBoolean(PREF_EXPORT_COMPRESS, false)
        val outputFile = resolveOutputFile(directory, useFixedFilename, compress)
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
                context.contentResolver.openOutputStream(outputFile.uri, openMode).use { rawOutputStream ->
                    if (rawOutputStream == null) {
                        Log.e(TAG, "Could not open output stream")
                        return Result.failure()
                    }

                    val outputStream: OutputStream = if (compress) GZIPOutputStream(rawOutputStream) else rawOutputStream

                    val buffer = ByteArray(4096)
                    var len: Int
                    while (inputStream.read(buffer).also { len = it } != -1) {
                        outputStream.write(buffer, 0, len)
                    }
                    outputStream.flush()
                    if (outputStream is GZIPOutputStream) {
                        outputStream.finish()
                    }

                    Log.i(TAG, "Database exported to ${outputFile.name}")

                    val removeOld = prefs.getBoolean(PREF_EXPORT_REMOVE_OLD, false)
                    if (removeOld && !useFixedFilename) {
                        val deleted = removeOldExports(directory, outputFile)
                        if (deleted > 0) {
                            Log.i(TAG, "Removed $deleted old export(s)")
                        }
                    }

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
        const val PREF_EXPORT_REMOVE_OLD = "export_remove_old"
        const val PREF_EXPORT_COMPRESS = "export_compress"
        private const val FIXED_FILENAME = "usageDirectPlus.sqlite3"
        private const val FIXED_FILENAME_GZ = "usageDirectPlus.sqlite3.gz"
        private const val MIME_TYPE = "application/vnd.sqlite3"
        private const val MIME_TYPE_GZ = "application/gzip"

        private val TIMESTAMPED_PATTERN = Regex("^usageDirectPlus-.*\\.sqlite3(\\.gz)?$")

        @JvmStatic
        internal fun removeOldExports(directory: DocumentFile, currentFile: DocumentFile): Int {
            val files = directory.listFiles()
            var deleted = 0
            for (file in files) {
                if (file.uri == currentFile.uri) continue
                val name = file.name ?: continue
                if (TIMESTAMPED_PATTERN.matches(name)) {
                    if (file.delete()) {
                        deleted++
                    } else {
                        Log.w(TAG, "Failed to delete old export: $name")
                    }
                }
            }
            return deleted
        }

        @JvmStatic
        internal fun resolveOutputFile(
            directory: DocumentFile,
            useFixedFilename: Boolean,
            compress: Boolean = false
        ): DocumentFile? {
            val mimeType = if (compress) MIME_TYPE_GZ else MIME_TYPE
            return if (useFixedFilename) {
                val filename = if (compress) FIXED_FILENAME_GZ else FIXED_FILENAME
                directory.findFile(filename)
                    ?: directory.createFile(mimeType, filename)
            } else {
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
                val ext = if (compress) "sqlite3.gz" else "sqlite3"
                directory.createFile(mimeType, "usageDirectPlus-$timestamp.$ext")
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
