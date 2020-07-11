/*
* Copyright (C) 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics;

import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import humanize.Humanize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<UsageStats> mUsageStatsList;
    private Context mContext;

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

        UsageStats usageStats = mUsageStatsList.get(position);

        String name = IconThread.nameMap.get(usageStats);
        viewHolder.getPackageName().setText(
                name == null?
                usageStats.getPackageName() : name
        );


        long lastTimeUsed = usageStats.getLastTimeUsed();

        if (usageStats.getPackageName().equals(BuildConfig.APPLICATION_ID))
            viewHolder.getLastTimeUsed().setText(R.string.last_used_now);
        else if (lastTimeUsed > 1)
            viewHolder.getLastTimeUsed().setText(
                    mContext.getString(R.string.last_used, Humanize.naturalTime(new Date(lastTimeUsed)))
            );
        else
            viewHolder.getLastTimeUsed().setText(R.string.not_used);

        long secondsUsed = usageStats.getTotalTimeInForeground() / 1000;
        viewHolder.getTimeUsed().setText(String.format(Locale.ENGLISH, "%d:%02d:%02d",
                        secondsUsed / 3600, (secondsUsed / 60) % 60, secondsUsed % 60)
        );

        viewHolder.getAppIcon().setImageDrawable(IconThread.iconMap.get(usageStats));

        viewHolder.getAppIcon().setTag(usageStats.getPackageName());
    }

    @Override
    public int getItemCount() {
        if (mUsageStatsList == null)
            return 0;
        else
            return mUsageStatsList.size();
    }

    public void setUsageStatsList(List<UsageStats> usageStats) {
        mUsageStatsList = usageStats;
        notifyDataSetChanged();
    }
}