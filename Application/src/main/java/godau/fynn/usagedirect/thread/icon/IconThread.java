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

package godau.fynn.usagedirect.thread.icon;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirect.R;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IconThread extends Thread {

    public static Map<String, Drawable> iconMap = new ConcurrentHashMap<>();
    public static Map<String, String> nameMap = new ConcurrentHashMap<>();

    private final String[] applicationIds;
    private final RecyclerView.LayoutManager layout;
    private final Context context;

    public IconThread(String[] applicationIds, RecyclerView.LayoutManager layout, Context context) {
        this.applicationIds = applicationIds;
        this.layout = layout;
        this.context = context;
    }

    @Override
    public void run() {

        PackageManager packageManager = context.getPackageManager();

        for (int i = 0; i < applicationIds.length; i++) {
            String applicationId = applicationIds[i];

            try {
                if (!iconMap.containsKey(applicationId)) {
                    final Drawable appIcon = packageManager.getApplicationIcon(applicationId);
                    iconMap.put(applicationId, appIcon);
                }

                if (!nameMap.containsKey(applicationId)) {
                    ApplicationInfo appInfo = packageManager.getApplicationInfo(applicationId, 0);
                    final String appName = (String) packageManager.getApplicationLabel(appInfo);
                    nameMap.put(applicationId, appName);
                }

                int finalI = i;
                new Handler(Looper.getMainLooper()).post(() -> onIconLoaded(finalI, applicationId));

            } catch (PackageManager.NameNotFoundException e) {
                Log.i("ICONTHREAD", String.format("App Icon not found for %s", applicationId));
            }

        }
    }

    protected void onIconLoaded(int position, String applicationId) {
        View view = layout.findViewByPosition(position);

        if (view == null) return;

        ImageView imageView = view.findViewById(R.id.app_icon);
        imageView.setImageDrawable(iconMap.get(applicationId));

        TextView textView = view.findViewById(R.id.textview_package_name);
        textView.setText(nameMap.get(applicationId));

    }
}
