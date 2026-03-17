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

package godau.fynn.usagedirectplus.thread.icon

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.usagedirectplus.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

open class IconThread(
    private val applicationIds: Array<String>,
    private val layout: RecyclerView.LayoutManager,
    private val context: Context
) : Thread() {

    override fun run() {
        val packageManager = context.packageManager

        for (i in applicationIds.indices) {
            val applicationId = applicationIds[i]

            try {
                if (!iconMap.containsKey(applicationId)) {
                    val appIcon = packageManager.getApplicationIcon(applicationId)
                    iconMap[applicationId] = appIcon
                }

                if (!nameMap.containsKey(applicationId)) {
                    val appInfo = packageManager.getApplicationInfo(applicationId, 0)
                    val appName = packageManager.getApplicationLabel(appInfo) as String
                    nameMap[applicationId] = appName
                }

                val finalI = i
                MainScope().launch(Dispatchers.Main) { onIconLoaded(finalI, applicationId) }
            } catch (e: PackageManager.NameNotFoundException) {
                Log.i("ICONTHREAD", String.format("App Icon not found for %s", applicationId))
            }
        }
    }

    protected open fun onIconLoaded(position: Int, applicationId: String) {
        val view = layout.findViewByPosition(position) ?: return

        val imageView = view.findViewById<ImageView>(R.id.app_icon)
        imageView.setImageDrawable(iconMap[applicationId])

        val textView = view.findViewById<TextView>(R.id.textview_package_name)
        textView.text = nameMap[applicationId]
    }

    companion object {
        @JvmField
        val iconMap: MutableMap<String, Drawable> = ConcurrentHashMap()

        @JvmField
        val nameMap: MutableMap<String, String> = ConcurrentHashMap()
    }
}
