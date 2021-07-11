package godau.fynn.usagedirect.view.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.thread.icon.IconThread;

import java.util.Map;

public class ColorAdapter extends SimpleRecyclerViewAdapter<String, ColorAdapter.ViewHolder> {
    
    private final Map<String, Long> timePerApp;
    
    public ColorAdapter(Map<String, Long> timePerApp) {
        this.timePerApp = timePerApp;
        content.addAll(timePerApp.keySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row_color, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, String item, int position) {
        holder.mPackageName.setText(item);

        String name = IconThread.nameMap.get(item);
        holder.mPackageName.setText(
                name == null?
                        item : name
        );
        holder.mAppIcon.setImageDrawable(IconThread.iconMap.get(item));

        int hours = (int) (timePerApp.get(item) / 1000 / 60 / 60);
        holder.mTimeUsed.setText(context.getResources().getQuantityString(
                R.plurals.time_used_total_hours,
                hours, hours
        ));
        holder.mTimeUsed.setVisibility(
                hours > 0?
                        View.VISIBLE : View.GONE
        );

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mPackageName;
        public final TextView mTimeUsed;
        public final ImageView mAppIcon;

        public ViewHolder(View v) {
            super(v);
            mPackageName = v.findViewById(R.id.textview_package_name);
            mTimeUsed = v.findViewById(R.id.textview_time_used);
            mAppIcon = v.findViewById(R.id.app_icon);
        }
    }
}
