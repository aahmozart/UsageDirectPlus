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

package godau.fynn.usagedirectplus.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import godau.fynn.usagedirectplus.R;

public class HelpActivity extends Activity {

    private static final String DATABASE_FLAVOR_PACKAGE_NAME = "godau.fynn.usagedirectplus";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_help);

        final View databaseBox = findViewById(R.id.database_layout);
        TextView databaseText = findViewById(R.id.text_database);

        Intent databaseIntent = getPackageManager().getLaunchIntentForPackage(DATABASE_FLAVOR_PACKAGE_NAME);
        boolean databaseInstalled = databaseIntent != null;

        if (databaseInstalled) {
            databaseText.setText(R.string.help_database_flavor_installed);
            databaseBox.setElevation(0f);
            databaseBox.setClickable(false);
        } else {
            databaseBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/" + DATABASE_FLAVOR_PACKAGE_NAME)));
                }
            });
        }
    }
}
