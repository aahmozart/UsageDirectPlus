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

package godau.fynn.usagedirect;

import android.app.Activity;
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

    public static Map<String, Drawable> iconMap = new ConcurrentHashMap<>();
    public static Map<String, String> nameMap = new ConcurrentHashMap<>();

    private final List<SimpleUsageStat> usageStats;
    private final RecyclerView.LayoutManager layout;
    private final Activity context;

    public IconThread(List<SimpleUsageStat> usageStats, RecyclerView.LayoutManager layout, Activity context) {
        this.usageStats = usageStats;
        this.layout = layout;
        this.context = context;
    }

    @Override
    public void run() {

        PackageManager packageManager = context.getPackageManager();

        for (final SimpleUsageStat u : usageStats) {

            try {
                if (!iconMap.containsKey(u.getApplicationId())) {
                    final Drawable appIcon = packageManager.getApplicationIcon(u.getApplicationId());
                    iconMap.put(u.getApplicationId(), appIcon);
                }

                if (!nameMap.containsKey(u.getApplicationId())) {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(u.getApplicationId(), 0);
                    final String appName = (String) packageManager.getApplicationLabel(appInfo);
                    nameMap.put(u.getApplicationId(), appName);
                }

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        View view = layout.findViewByPosition(usageStats.indexOf(u));

                        if (view == null) return;

                        ImageView imageView = view.findViewById(R.id.app_icon);
                        imageView.setImageDrawable(iconMap.get(u.getApplicationId()));

                        TextView textView = view.findViewById(R.id.textview_package_name);
                        textView.setText(nameMap.get(u.getApplicationId()));
                    }
                });

            } catch (PackageManager.NameNotFoundException e) {
                Log.i("ICONTHREAD", String.format("App Icon not found for %s", u.getApplicationId()));
            }

        }
    }
}
