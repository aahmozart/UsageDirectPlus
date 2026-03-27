package godau.fynn.usagedirectplus.view.dialog

import android.app.job.JobScheduler
import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.DialogDatabaseBinding
import godau.fynn.usagedirectplus.persistence.BrowserCaptureDiagnostics
import godau.fynn.usagedirectplus.persistence.EventLogRunnable
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class DatabaseDebugDialog(context: Context) : MaterialAlertDialogBuilder(context) {

    private val database: HistoryDatabase = HistoryDatabase.get(context)
    private val usageStats = database.getUsageStatsDao()

    private lateinit var binding: DialogDatabaseBinding

    init {
        val inflatedBinding = DialogDatabaseBinding.inflate(LayoutInflater.from(context))
        binding = inflatedBinding
        setView(binding.root)

        setOnDismissListener { database.close() }
    }

    override fun show(): AlertDialog {
        val dialog = super.show()

        binding.buttonInsert.isEnabled = false

        val scheduler = getContext().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val scheduled = scheduler.allPendingJobs.size > 0

        binding.buttonSchedule.isChecked = scheduled
        binding.buttonSchedule.isEnabled = false

        binding.buttonInsert.setOnClickListener {
            binding.textStatus.setText(R.string.db_wait)
            binding.buttonInsert.isEnabled = false

            MainScope().launch(Dispatchers.IO) {
                EventLogRunnable(getContext()).run()
                updateViews()
            }
        }

        updateViews()

        return dialog
    }

    private fun updateViews() {
        MainScope().launch(Dispatchers.IO) {
            val daysStored = usageStats.getDaysStoredAmount()
            val totalHours = usageStats.getTotalTimeUsed() / 1000 / 60 / 60
            val captureStatus = BrowserCaptureDiagnostics.load(getContext())

            withContext(Dispatchers.Main) {
                binding.textStatus.text = getContext().getString(R.string.db_status, daysStored, totalHours)
                binding.textBrowserCapture.text = getContext().getString(
                    R.string.db_browser_capture,
                    BrowserCaptureDiagnostics.formatResult(getContext(), captureStatus),
                    captureStatus.packageName ?: getContext().getString(R.string.db_browser_capture_no_package),
                    if (captureStatus.timestamp > 0) {
                        TIME_FORMATTER.format(
                            Instant.ofEpochMilli(captureStatus.timestamp).atZone(ZoneId.systemDefault())
                        )
                    } else {
                        getContext().getString(R.string.db_browser_capture_no_time)
                    }
                )
                binding.buttonInsert.isEnabled = true
            }
        }
    }

    companion object {
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
    }
}
