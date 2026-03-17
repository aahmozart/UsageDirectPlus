package godau.fynn.usagedirectplus.view.dialog

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.databinding.DialogCustomQueryBinding
import godau.fynn.usagedirectplus.wrapper.EventLogWrapper
import im.dacer.androidcharts.clockpie.ClockPieSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CustomQueryDialog(context: Context) : MaterialAlertDialogBuilder(context) {

    private lateinit var binding: DialogCustomQueryBinding

    init {
        val inflatedBinding = DialogCustomQueryBinding.inflate(LayoutInflater.from(context))
        binding = inflatedBinding
        setView(binding.root)

        setOnDismissListener { }
    }

    override fun show(): AlertDialog {
        val dialog = super.show()

        binding.timeFrom.setIs24HourView(true)
        binding.timeTo.setIs24HourView(true)

        binding.buttonQuery.setOnClickListener {
            MainScope().launch(Dispatchers.IO) {
                val eventLog = EventLogWrapper(getContext())

                val start = LocalTime.of(
                    binding.timeFrom.currentHour,
                    binding.timeFrom.currentMinute
                )
                    .atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val end = LocalTime.of(
                    binding.timeTo.currentHour,
                    binding.timeTo.currentMinute
                )
                    .atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()

                val stats = eventLog.getForegroundStatsByTimestamps(start, end)

                withContext(Dispatchers.Main) {
                    ClockPieChartDialog(
                        stats,
                        ClockPieSegment(
                            binding.timeFrom.currentHour, binding.timeFrom.currentMinute,
                            binding.timeTo.currentHour, binding.timeTo.currentMinute
                        ),
                        getContext()
                    ).show()
                }
            }
        }

        return dialog
    }
}
