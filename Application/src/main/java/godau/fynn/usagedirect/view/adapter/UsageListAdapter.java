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

import android.content.Context;
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
import humanize.Humanize;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Recycler view adapter for displaying (aggregated) usage stats
 */
public class UsageListAdapter extends SimpleRecyclerViewAdapter<SimpleUsageStat, UsageListAdapter.ViewHolder> {

    private Map<String, Long> lastUsedMap;

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

    public UsageListAdapter(Context context) {
        super(context);
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

            v = inflater.inflate(R.layout.usage_row_total, viewGroup, false);

            return new ViewHolder(v);
        } else {

            v = inflater.inflate(R.layout.usage_row, viewGroup, false);

            final ViewHolder viewHolder = new ViewHolder(v);

            // For performance, only set OnClickListener once
            viewHolder.getAppIcon().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Launch app that this icon is associated with
                    try {
                        String packageName = (String) viewHolder.getAppIcon().getTag();
                        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                        context.startActivity(intent);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(context, R.string.launch_unavailable, Toast.LENGTH_SHORT).show();
                    }
                }
            });

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

            viewHolder.getLastTimeUsed().setVisibility(View.VISIBLE);

            long lastTimeUsed = lastUsedMap.get(usageStat.getApplicationId());

            if (usageStat.getApplicationId().equals(BuildConfig.APPLICATION_ID))
                viewHolder.getLastTimeUsed().setText(R.string.last_used_now);
            else if (lastTimeUsed > 1)
                viewHolder.getLastTimeUsed().setText(
                        context.getString(R.string.last_used, Humanize.naturalTime(new Date(lastTimeUsed)))
                );
            else
                viewHolder.getLastTimeUsed().setText(R.string.not_used);

        } else {
            viewHolder.getLastTimeUsed().setVisibility(View.GONE);
        }

        long secondsUsed = usageStat.getTimeUsed() / 1000;
        viewHolder.getTimeUsed().setText(context.getString(
                lastUsedMap == null || position == 0 ? R.string.time_used_time_only : R.string.time_used,
                secondsUsed / 3600, (secondsUsed / 60) % 60, secondsUsed % 60)
        );

        viewHolder.getAppIcon().setImageDrawable(IconThread.iconMap.get(usageStat.getApplicationId()));

        viewHolder.getAppIcon().setTag(usageStat.getApplicationId());
    }

    public void setUsageStatsList(List<SimpleUsageStat> usageStats) {
        content.clear();
        if (usageStats != null) {

            // Calculate total amount
            long total = 0;
            for (SimpleUsageStat stat : usageStats) {
                total += stat.getTimeUsed();
            }
            content.add(new SimpleUsageStat(null, total, context.getString(R.string.total)));

            content.addAll(usageStats);
        }
        notifyDataSetChanged();
    }

    public void setLastUsedMap(Map<String, Long> map) {
        lastUsedMap = map;
    }
}