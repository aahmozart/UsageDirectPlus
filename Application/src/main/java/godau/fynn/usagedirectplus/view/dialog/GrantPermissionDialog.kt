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

package godau.fynn.usagedirectplus.view.dialog

import android.content.Intent
import android.provider.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.activity.AppUsageStatisticsActivity

class GrantPermissionDialog(context: AppUsageStatisticsActivity) : MaterialAlertDialogBuilder(context) {

    init {
        setTitle(R.string.explanation_access_appusage_title)
        setMessage(R.string.explanation_access_appusage_message)
        setPositiveButton(R.string.go) { _, _ ->
            context.grantPermissionLauncher.launch(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        setNegativeButton(R.string.leave_app) { _, _ ->
            context.finish()
        }
        setCancelable(false)
    }
}
