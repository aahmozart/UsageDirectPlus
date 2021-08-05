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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.Color;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.SimpleUsageStat;
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
    private Map<String, Integer> colorMap;
    private final PrettyTime prettyTime = new PrettyTime();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView packageName;
        private final TextView lastTimeUsed;
        private final TextView timeUsed;
        private final ImageView appIcon;

        public ViewHolder(View v) {
            super(v);
            packageName = v.findViewById(R.id.textview_package_name);
            lastTimeUsed = v.findViewById(R.id.textview_last_time_used);
            timeUsed = v.findViewById(R.id.textview_time_used);
            appIcon = v.findViewById(R.id.app_icon);
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
            viewHolder.appIcon.setOnClickListener(v1 -> {
                // Launch app that this icon is associated with
                try {
                    String packageName = (String) viewHolder.appIcon.getTag();
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

    @SuppressLint("ResourceType") // apparently incorrect annotation of TypedArray.getColor causes lint to complain
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, SimpleUsageStat usageStat, final int position) {

        String name = IconThread.nameMap.get(usageStat.getApplicationId());
        viewHolder.packageName.setText(
                name == null?
                usageStat.getApplicationId() : name
        );

        viewHolder.lastTimeUsed.setVisibility(View.GONE);

        // Set colors (background and text)
        TypedArray array;
        if (position > 0 && colorMap != null && colorMap.containsKey(usageStat.getApplicationId())) {

            @ColorInt int backgroundColor = colorMap.get(usageStat.getApplicationId());

            viewHolder.itemView.setBackgroundColor(backgroundColor);

            @StyleRes int style;
            if (
                    Color.luminance(backgroundColor) < 0.4f
            ) {
                // System dark theme
                style = android.R.style.Theme_DeviceDefault;
            } else {
                // System light theme
                style = android.R.style.Theme_DeviceDefault_Light;
            }

            Resources.Theme theme = context.getResources().newTheme();
            theme.applyStyle(style, true);

            array = theme.obtainStyledAttributes(new int[]{
                    android.R.attr.textColorPrimary, android.R.attr.textColorSecondary
            });
        } else {
            viewHolder.itemView.setBackground(null);

            // Current theme (DayNight from API 29 onwards)
            array = context.getTheme().obtainStyledAttributes(new int[] {
                    android.R.attr.textColorPrimary, android.R.attr.textColorSecondary
            });
        }

        viewHolder.packageName.setTextColor(
                array.getColor(0, Color.RED)
        );

        viewHolder.timeUsed.setTextColor(
                array.getColor(1, Color.RED)
        );
        viewHolder.lastTimeUsed.setTextColor(
                array.getColor(1, Color.RED)
        );

        array.recycle();

        if (lastUsedMap != null && lastUsedMap.containsKey(usageStat.getApplicationId())) {

            long lastUsed = lastUsedMap.get(usageStat.getApplicationId());

            Instant lastUsedInstant = Instant.ofEpochMilli(lastUsed);
            LocalDate day = LocalDate.ofEpochDay(usageStat.getDay());
            Instant startOfDay = day.atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
            Instant endOfDay = day.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            boolean lastUsedOnSameDay = lastUsedInstant.isAfter(startOfDay) && lastUsedInstant.isBefore(endOfDay);

            if (lastUsedOnSameDay) {

                viewHolder.lastTimeUsed.setVisibility(View.VISIBLE);

                if (day.isEqual(LocalDate.now())) {

                    if (usageStat.getApplicationId().equals(BuildConfig.APPLICATION_ID))
                        viewHolder.lastTimeUsed.setText(R.string.last_used_now);
                    else if (lastUsed > 1) {
                        viewHolder.lastTimeUsed.setText(
                                prettyTime.format(new Date(lastUsed))
                        );
                    } else {
                        viewHolder.lastTimeUsed.setText(R.string.not_used);
                    }

                } else {
                    viewHolder.lastTimeUsed.setText(R.string.last_used_this_day);
                }
            } else {
                viewHolder.lastTimeUsed.setVisibility(View.GONE);
            }
        } else {
            viewHolder.lastTimeUsed.setVisibility(View.GONE);
        }

        long secondsUsed = usageStat.getTimeUsed() / 1000;
        viewHolder.timeUsed.setText(context.getString(
                R.string.time_used_time_only,
                secondsUsed / 3600, (secondsUsed / 60) % 60, secondsUsed % 60)
        );

        viewHolder.appIcon.setImageDrawable(IconThread.iconMap.get(usageStat.getApplicationId()));

        viewHolder.appIcon.setTag(usageStat.getApplicationId());

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

    public void setColorMap(Map<String, Integer> map) {
        colorMap = map;
    }
}