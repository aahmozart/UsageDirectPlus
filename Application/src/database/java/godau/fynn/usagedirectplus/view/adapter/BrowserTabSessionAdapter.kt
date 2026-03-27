package godau.fynn.usagedirectplus.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.RowBrowserTabSessionBinding
import godau.fynn.usagedirectplus.persistence.BrowserTabSession
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class BrowserTabSessionAdapter(
    private val sessions: List<BrowserTabSession>
) : RecyclerView.Adapter<BrowserTabSessionAdapter.ViewHolder>() {

    class ViewHolder(val binding: RowBrowserTabSessionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowBrowserTabSessionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val session = sessions[position]
        val context = holder.binding.root.context
        val closedAt = session.closedAt ?: session.openedAt
        val durationMinutes = (closedAt - session.openedAt).coerceAtLeast(0) / 60000

        holder.binding.title.text = session.title ?: context.getString(R.string.browser_tab_sessions_untitled)
        holder.binding.url.text = session.url
        holder.binding.url.visibility = if (session.url.isNullOrBlank()) View.GONE else View.VISIBLE

        val begin = TIME_FORMAT.format(Instant.ofEpochMilli(session.openedAt))
        val end = TIME_FORMAT.format(Instant.ofEpochMilli(closedAt))
        holder.binding.timeRange.text = "$begin \u2013 $end"
        holder.binding.duration.text = if (durationMinutes >= 60) {
            "${durationMinutes / 60}h ${durationMinutes % 60} min"
        } else {
            "$durationMinutes min"
        }

        holder.binding.privacy.text = context.getString(R.string.browser_tab_sessions_private)
        holder.binding.privacy.visibility = if (session.privacyMode == BrowserTabSession.PRIVACY_MODE_PRIVATE) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    override fun getItemCount(): Int = sessions.size

    companion object {
        private val TIME_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    }
}
