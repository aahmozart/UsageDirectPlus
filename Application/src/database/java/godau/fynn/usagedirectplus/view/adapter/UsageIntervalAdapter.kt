package godau.fynn.usagedirectplus.view.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.usagedirectplus.databinding.RowUsageIntervalBinding
import godau.fynn.usagedirectplus.persistence.UsageInterval
import java.util.Date

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

        // Follows the device's 12/24-hour clock setting
        val timeFormat = DateFormat.getTimeFormat(holder.itemView.context)
        val begin = timeFormat.format(Date(interval.beginTime))
        val end = timeFormat.format(Date(interval.endTime))
        holder.binding.timeRange.text = "$begin \u2013 $end"

        val minutes = (interval.endTime - interval.beginTime) / 60000
        holder.binding.duration.text = if (minutes >= 60) {
            "${minutes / 60}h ${minutes % 60} min"
        } else {
            "$minutes min"
        }
    }

    override fun getItemCount(): Int = intervals.size
}
