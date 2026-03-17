package godau.fynn.usagedirectplus.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.persistence.UsageInterval;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UsageIntervalAdapter extends RecyclerView.Adapter<UsageIntervalAdapter.ViewHolder> {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    private final List<UsageInterval> intervals;

    public UsageIntervalAdapter(List<UsageInterval> intervals) {
        this.intervals = intervals;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeRange;
        private final TextView duration;

        public ViewHolder(View v) {
            super(v);
            timeRange = v.findViewById(R.id.time_range);
            duration = v.findViewById(R.id.duration);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_usage_interval, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsageInterval interval = intervals.get(position);

        String begin = TIME_FORMAT.format(Instant.ofEpochMilli(interval.getBeginTime()));
        String end = TIME_FORMAT.format(Instant.ofEpochMilli(interval.getEndTime()));
        holder.timeRange.setText(begin + " \u2013 " + end);

        long minutes = (interval.getEndTime() - interval.getBeginTime()) / 60000;
        if (minutes >= 60) {
            holder.duration.setText(minutes / 60 + "h " + minutes % 60 + " min");
        } else {
            holder.duration.setText(minutes + " min");
        }
    }

    @Override
    public int getItemCount() {
        return intervals.size();
    }
}
