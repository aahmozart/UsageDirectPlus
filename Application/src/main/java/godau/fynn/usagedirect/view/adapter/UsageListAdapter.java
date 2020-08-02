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
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import godau.fynn.usagedirect.BuildConfig;
import godau.fynn.usagedirect.IconThread;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.SimpleUsageStat;
import humanize.Humanize;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<SimpleUsageStat> mUsageStatsList;
    private Context mContext;

    private Map<String, Long> lastUsedMap;

    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mPackageName;
        private final TextView mLastTimeUsed;
        private final TextView mTimeUsed;
        private final ImageView mAppIcon;

        public ViewHolder(View v) {
            super(v);
            mPackageName = (TextView) v.findViewById(R.id.textview_package_name);
            mLastTimeUsed = (TextView) v.findViewById(R.id.textview_last_time_used);
            mTimeUsed = (TextView) v.findViewById(R.id.textview_time_used);
            mAppIcon = (ImageView) v.findViewById(R.id.app_icon);
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
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usage_row, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(v);

        // For performance, only set OnClickListener once
        viewHolder.getAppIcon().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch app that this icon is associated with
                try {
                    String packageName = (String) viewHolder.getAppIcon().getTag();
                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
                    mContext.startActivity(intent);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, R.string.launch_unavailable, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        SimpleUsageStat usageStat = mUsageStatsList.get(position);

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
                        mContext.getString(R.string.last_used, Humanize.naturalTime(new Date(lastTimeUsed)))
                );
            else
                viewHolder.getLastTimeUsed().setText(R.string.not_used);

        } else {
            viewHolder.getLastTimeUsed().setVisibility(View.GONE);
        }

        long secondsUsed = usageStat.getTimeUsed() / 1000;
        viewHolder.getTimeUsed().setText(mContext.getString(
                lastUsedMap == null ? R.string.time_used_time_only : R.string.time_used,
                secondsUsed / 3600, (secondsUsed / 60) % 60, secondsUsed % 60)
        );

        viewHolder.getAppIcon().setImageDrawable(IconThread.iconMap.get(usageStat.getApplicationId()));

        viewHolder.getAppIcon().setTag(usageStat.getApplicationId());
    }

    @Override
    public int getItemCount() {
        if (mUsageStatsList == null)
            return 0;
        else
            return mUsageStatsList.size();
    }

    public void setUsageStatsList(List<SimpleUsageStat> usageStats) {
        mUsageStatsList = usageStats;
        notifyDataSetChanged();
    }

    public void setLastUsedMap(Map<String, Long> map) {
        lastUsedMap = map;
    }
}