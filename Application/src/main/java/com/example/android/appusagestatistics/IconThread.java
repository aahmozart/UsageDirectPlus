package com.example.android.appusagestatistics;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public class IconThread extends Thread {

    public static HashMap<UsageStats, Drawable> iconMap = new HashMap<>();

    private List<UsageStats> usageStats;
    private RecyclerView.LayoutManager layout;
    private Activity context;

    public IconThread(List<UsageStats> usageStats, RecyclerView.LayoutManager layout, Activity context) {
        this.usageStats = usageStats;
        this.layout = layout;
        this.context = context;
    }

    @Override
    public void run() {

        PackageManager packageManager = context.getPackageManager();

        for (final UsageStats u : usageStats) {

            try {
                final Drawable appIcon = packageManager.getApplicationIcon(u.getPackageName());
                iconMap.put(u, appIcon);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View view = layout.findViewByPosition(usageStats.indexOf(u));

                        if (view == null) return;

                        ImageView imageView = view.findViewById(R.id.app_icon);

                        imageView.setImageDrawable(appIcon);
                    }
                });

            } catch (PackageManager.NameNotFoundException e) {
                Log.w("ICONTHREAD", String.format("App Icon is not found for %s", u.getPackageName()));
            }

        }
    }
}
