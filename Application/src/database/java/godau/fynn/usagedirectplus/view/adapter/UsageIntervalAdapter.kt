package godau.fynn.usagedirectplus.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.usagedirectplus.databinding.RowUsageIntervalBinding
import godau.fynn.usagedirectplus.persistence.UsageInterval
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class UsageIntervalAdapter(
    private val intervals: List<UsageInterval>
) : RecyclerView.Adapter<UsageIntervalAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowUsageIntervalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowUsageIntervalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val interval = intervals[position]

        val begin = TIME_FORMAT.format(Instant.ofEpochMilli(interval.beginTime))
        val end = TIME_FORMAT.format(Instant.ofEpochMilli(interval.endTime))
        holder.binding.timeRange.text = "$begin \u2013 $end"

        val minutes = (interval.endTime - interval.beginTime) / 60000
        holder.binding.duration.text = if (minutes >= 60) {
            "${minutes / 60}h ${minutes % 60} min"
        } else {
            "$minutes min"
        }
    }

    override fun getItemCount(): Int = intervals.size

    companion object {
        private val TIME_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    }
}
