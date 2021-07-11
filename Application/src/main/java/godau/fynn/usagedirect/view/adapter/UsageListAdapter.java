/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package godau.fynn.usagedirect.view.adapter;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter;
import godau.fynn.usagedirect.*;
import godau.fynn.usagedirect.thread.icon.IconThread;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Recycler view adapter for displaying (aggregated) usage stats
 */
public class UsageListAdapter extends SimpleRecyclerViewAdapter<SimpleUsageStat, UsageListAdapter.ViewHolder> {

    private Map<String, Long> lastUsedMap;
    private final PrettyTime prettyTime = new PrettyTime();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final TextView mLastTimeUsed;
        private final TextView mTimeUsed;
        private final ImageView mAppIcon;

        public ViewHolder(View v) {
            super(v);
            mPackageName = v.findViewById(R.id.textview_package_name);
            mLastTimeUsed = v.findViewById(R.id.textview_last_time_used);
            mTimeUsed = v.findViewById(R.id.textview_time_used);
            mAppIcon = v.findViewById(R.id.app_icon);
        }

        public TextView getLastTimeUsed() {
            return mLastTimeUsed;
        }

        public TextView getTimeUsed() {
            return mTimeUsed;
        }

        public TextView getPackageName() {
            return mPackageName;
        }

        public ImageView getAppIcon() {
            return mAppIcon;
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 0 for the first, 1 for all other positions
        return Math.min(position, 1);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        View v;
        if (viewType == 0) {

            v = inflater.inflate(R.layout.row_usage_total, viewGroup, false);

            return new ViewHolder(v);
        } else {

            v = inflater.inflate(R.layout.row_usage, viewGroup, false);

            final ViewHolder viewHolder = new ViewHolder(v);

            // For performance, only set OnClickListener once
            viewHolder.getAppIcon().setOnClickListener(v1 -> {
                // Launch app that this icon is associated with
                try {
                    String packageName = (String) viewHolder.getAppIcon().getTag();
                    Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                    context.startActivity(intent);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.launch_unavailable, Toast.LENGTH_SHORT).show();
                }
            });

            // Register for context menu
            ((Activity) context).registerForContextMenu(v);

            return viewHolder;
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, SimpleUsageStat usageStat, final int position) {

        String name = IconThread.nameMap.get(usageStat.getApplicationId());
        viewHolder.getPackageName().setText(
                name == null?
                usageStat.getApplicationId() : name
        );

        viewHolder.getLastTimeUsed().setVisibility(View.GONE);

        if (lastUsedMap != null && lastUsedMap.containsKey(usageStat.getApplicationId())) {

            long lastUsed = lastUsedMap.get(usageStat.getApplicationId());

            Instant lastUsedInstant = Instant.ofEpochMilli(lastUsed);
            LocalDate day = LocalDate.ofEpochDay(usageStat.getDay());
            Instant startOfDay = day.atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
            Instant endOfDay = day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            boolean lastUsedOnSameDay = lastUsedInstant.isAfter(startOfDay) && lastUsedInstant.isBefore(endOfDay);

            if (lastUsedOnSameDay) {

                viewHolder.getLastTimeUsed().setVisibility(View.VISIBLE);

                if (day.isEqual(LocalDate.now())) {

                    if (usageStat.getApplicationId().equals(BuildConfig.APPLICATION_ID))
                        viewHolder.getLastTimeUsed().setText(R.string.last_used_now);
                    else if (lastUsed > 1) {
                        viewHolder.getLastTimeUsed().setText(
                                prettyTime.format(new Date(lastUsed))
                        );
                    } else {
                        viewHolder.getLastTimeUsed().setText(R.string.not_used);
                    }

                } else {
                    viewHolder.getLastTimeUsed().setText(R.string.last_used_this_day);
                }
            } else {
                viewHolder.getLastTimeUsed().setVisibility(View.GONE);
            }
        } else {
            viewHolder.getLastTimeUsed().setVisibility(View.GONE);
        }

        long secondsUsed = usageStat.getTimeUsed() / 1000;
        viewHolder.getTimeUsed().setText(context.getString(
                R.string.time_used_time_only,
                secondsUsed / 3600, (secondsUsed / 60) % 60, secondsUsed % 60)
        );

        viewHolder.getAppIcon().setImageDrawable(IconThread.iconMap.get(usageStat.getApplicationId()));

        viewHolder.getAppIcon().setTag(usageStat.getApplicationId());

        viewHolder.itemView.setTag(usageStat);
    }

    public void setUsageStatsList(List<SimpleUsageStat> usageStats) {
        content.clear();
        if (usageStats != null) {

            // Calculate total amount
            long total = 0;
            for (SimpleUsageStat stat : usageStats) {
                total += stat.getTimeUsed();
            }
            content.add(new SimpleUsageStat(0, total, context.getString(R.string.total)));

            content.addAll(usageStats);
        }
        notifyDataSetChanged();
    }

    public void setLastUsedMap(Map<String, Long> map) {
        lastUsedMap = map;
    }
}