package godau.fynn.usagedirectplus.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.ActivityBrowserTabSessionsBinding
import godau.fynn.usagedirectplus.persistence.BrowserCaptureAccessibilityService
import godau.fynn.usagedirectplus.persistence.BrowserCaptureDiagnostics
import godau.fynn.usagedirectplus.persistence.BrowserCaptureStatus
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.thread.icon.IconThread
import godau.fynn.usagedirectplus.view.adapter.BrowserTabSessionAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class BrowserTabSessionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrowserTabSessionsBinding
    private lateinit var applicationId: String
    private var start: Long = 0
    private var end: Long = 0
    private var loadJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBrowserTabSessionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applicationId = intent.getStringExtra("applicationId")!!
        val day = intent.getLongExtra("day", 0)
        val date = LocalDate.ofEpochDay(day)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = IconThread.nameMap[applicationId] ?: applicationId
        supportActionBar?.subtitle = date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

        IconThread.iconMap[applicationId]?.let(binding.appIcon::setImageDrawable)
            ?: run { binding.appIcon.visibility = View.GONE }

        start = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        end = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.enableCaptureButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onStart() {
        super.onStart()
        loadSessions()
    }

    override fun onStop() {
        loadJob?.cancel()
        loadJob = null
        super.onStop()
    }

    private fun loadSessions() {
        loadJob?.cancel()
        loadJob = lifecycleScope.launch {
            val database = HistoryDatabase.get(this@BrowserTabSessionsActivity)
            val (sessions, captureStatus) = try {
                withContext(Dispatchers.IO) {
                    database.getBrowserTabSessionDao().getByAppAndTimeRange(applicationId, start, end) to
                        BrowserCaptureDiagnostics.load(this@BrowserTabSessionsActivity)
                }
            } finally {
                database.close()
            }

            renderSessions(sessions, captureStatus)
        }
    }

    private fun renderSessions(sessions: List<BrowserTabSession>, captureStatus: BrowserCaptureStatus) {
        if (sessions.isEmpty()) {
            binding.recyclerview.adapter = null
            binding.recyclerview.visibility = View.GONE
            binding.emptyMessage.visibility = View.VISIBLE
            binding.summaryText.visibility = View.GONE
            binding.emptyMessage.text = getEmptyMessage(
                applicationId = applicationId,
                captureStatus = captureStatus
            )
            binding.enableCaptureButton.visibility =
                if (BrowserCaptureAccessibilityService.isEnabled(this)) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
            return
        }

        val totalMs = sessions.sumOf { (it.closedAt ?: System.currentTimeMillis()) - it.openedAt }
        val totalMinutes = totalMs / 60000
        val sessionCount = sessions.size

        binding.summaryText.text = getString(
            R.string.browser_tab_sessions_summary,
            sessionCount,
            formatMinutes(totalMinutes)
        )
        binding.recyclerview.adapter = BrowserTabSessionAdapter(sessions)
        binding.recyclerview.visibility = View.VISIBLE
        binding.emptyMessage.visibility = View.GONE
        binding.summaryText.visibility = View.VISIBLE
        binding.enableCaptureButton.visibility = View.GONE
    }

    private fun formatMinutes(totalMinutes: Long): String {
        return if (totalMinutes >= 60) {
            "${totalMinutes / 60}h ${totalMinutes % 60} min"
        } else {
            "$totalMinutes min"
        }
    }

    private fun getEmptyMessage(applicationId: String, captureStatus: BrowserCaptureStatus): String {
        if (!BrowserCaptureAccessibilityService.isEnabled(this)) {
            return getString(R.string.browser_tab_sessions_empty)
        }

        if (
            captureStatus.packageName == applicationId &&
            captureStatus.result == BrowserCaptureDiagnostics.RESULT_REJECTED &&
            captureStatus.timestamp > 0
        ) {
            val time = TIME_FORMATTER.format(
                java.time.Instant.ofEpochMilli(captureStatus.timestamp)
                    .atZone(ZoneId.systemDefault())
            )
            return getString(
                R.string.browser_tab_sessions_empty_rejected,
                BrowserCaptureDiagnostics.formatReason(this, captureStatus.detail),
                time
            )
        }

        return getString(R.string.browser_tab_sessions_empty_enabled)
    }

    companion object {
        private val TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
    }
}
