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

package godau.fynn.usagedirect.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import godau.fynn.usagedirect.R;

public class GrantPermissionDialog extends AlertDialog.Builder {

    public static final int REQUEST_CODE = 473; // GPD on a numpad

    public GrantPermissionDialog(final Activity context) {
        super(context);
        setTitle(R.string.explanation_access_appusage_title);
        setMessage(R.string.explanation_access_appusage_message);
        setPositiveButton(R.string.go, (dialog, which) ->
                context.startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE));
        setNegativeButton(R.string.leave_app, (dialog, which) ->
                context.finish());
        setCancelable(false);
    }
}
