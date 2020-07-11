package com.example.android.appusagestatistics;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconThread extends Thread {

    public static Map<UsageStats, Drawable> iconMap = new ConcurrentHashMap<>();
    public static Map<UsageStats, String> nameMap = new ConcurrentHashMap<>();

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
                if (!iconMap.containsKey(u)) {
                    final Drawable appIcon = packageManager.getApplicationIcon(u.getPackageName());
                    iconMap.put(u, appIcon);
                }

                if (!nameMap.containsKey(u)) {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(u.getPackageName(), 0);
                    final String appName = (String) packageManager.getApplicationLabel(appInfo);
                    nameMap.put(u, appName);
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View view = layout.findViewByPosition(usageStats.indexOf(u));

                        if (view == null) return;

                        ImageView imageView = view.findViewById(R.id.app_icon);
                        imageView.setImageDrawable(iconMap.get(u));

                        TextView textView = view.findViewById(R.id.textview_package_name);
                        textView.setText(nameMap.get(u));
                    }
                });

            } catch (PackageManager.NameNotFoundException e) {
                Log.i("ICONTHREAD", String.format("App Icon not found for %s", u.getPackageName()));
            }

        }
    }
}
