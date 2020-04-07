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
import android.graphics.drawable.Drawable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import humanize.Humanize;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Provide views to RecyclerView with the directory entries.
 */
public class UsageListAdapter extends RecyclerView.Adapter<UsageListAdapter.ViewHolder> {

    private List<UsageStats> mUsageStatsList = new ArrayList<>();
    private DateFormat mDateFormat = new SimpleDateFormat();

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

    public UsageListAdapter() {
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.usage_row, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        UsageStats usageStats = mUsageStatsList.get(position);

        viewHolder.getPackageName().setText(usageStats.getPackageName());
        long lastTimeUsed = usageStats.getLastTimeUsed();
        viewHolder.getLastTimeUsed().setText(mDateFormat.format(new Date(lastTimeUsed)));

        long timeUsed = usageStats.getTotalTimeInForeground();
        viewHolder.getTimeUsed().setText(Humanize.duration(timeUsed / 1000));

        viewHolder.getAppIcon().setImageDrawable(IconThread.iconMap.get(usageStats));
    }

    @Override
    public int getItemCount() {
        return mUsageStatsList.size();
    }

    public void setCustomUsageStatsList(List<UsageStats> usageStats) {
        mUsageStatsList = usageStats;
    }

    public int getPosition(UsageStats usageStats) {
        return mUsageStatsList.indexOf(usageStats);
    }

    /*public static void updateIcon(UsageStats usageStats, Drawable icon) {
        get
    }*/
}