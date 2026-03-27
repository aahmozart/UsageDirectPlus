package godau.fynn.usagedirectplus.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.browser.BrowserSupport
import godau.fynn.usagedirectplus.databinding.ActivityUsageIntervalsBinding
import godau.fynn.usagedirectplus.persistence.BrowserCaptureAccessibilityService
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.thread.icon.IconThread
import godau.fynn.usagedirectplus.view.adapter.UsageIntervalAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class UsageIntervalsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityUsageIntervalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val applicationId = intent.getStringExtra("applicationId")!!
        val day = intent.getLongExtra("day", 0)

        // Set action bar title to app name
        val appName = IconThread.nameMap[applicationId]
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            if (appName != null) {
                supportActionBar!!.title = appName
            } else {
                supportActionBar!!.title = applicationId
            }
            val date = LocalDate.ofEpochDay(day)
            supportActionBar!!.subtitle =
                date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        }

        // Set app icon
        val icon = IconThread.iconMap[applicationId]
        if (icon != null) {
            binding.appIcon.setImageDrawable(icon)
        } else {
            binding.appIcon.visibility = View.GONE
        }

        if (BrowserSupport.isSupportedBrowser(applicationId)) {
            binding.browserTabHistoryButton.visibility = View.VISIBLE
            binding.browserTabHistoryButton.setOnClickListener {
                val browserTabHistoryIntent = Intent(this, BrowserTabSessionsActivity::class.java).apply {
                    putExtra("applicationId", applicationId)
                    putExtra("day", day)
                }

                if (BrowserCaptureAccessibilityService.isEnabled(this)) {
                    startActivity(browserTabHistoryIntent)
                } else {
                    MaterialAlertDialogBuilder(this)
                        .setTitle(R.string.browser_capture_dialog_title)
                        .setMessage(R.string.browser_capture_dialog_message)
                        .setPositiveButton(R.string.go) { _, _ ->
                            startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                        .setNeutralButton(R.string.browser_capture_dialog_view_history) { _, _ ->
                            startActivity(browserTabHistoryIntent)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .show()
                }
            }
        }

        // Compute day boundaries
        val date = LocalDate.ofEpochDay(day)
        val start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        lifecycleScope.launch(Dispatchers.IO) {
            val intervals = HistoryDatabase.get(this@UsageIntervalsActivity)
                .getUsageIntervalDao()
                .getByAppAndTimeRange(applicationId, start, end)

            withContext(Dispatchers.Main) {
                if (intervals.isEmpty()) {
                    binding.recyclerview.visibility = View.GONE
                    binding.emptyMessage.visibility = View.VISIBLE
                    binding.totalTime.visibility = View.GONE
                } else {
                    // Compute and display total time
                    var totalMs: Long = 0
                    for (interval in intervals) {
                        totalMs += interval.endTime - interval.beginTime
                    }
                    val totalMinutes = totalMs / 60000
                    if (totalMinutes >= 60) {
                        binding.totalTime.text = "${totalMinutes / 60}h ${totalMinutes % 60} min"
                    } else {
                        binding.totalTime.text = "$totalMinutes min"
                    }

                    binding.recyclerview.adapter = UsageIntervalAdapter(intervals)
                }
            }
        }
    }
}
