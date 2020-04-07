/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import godau.fynn.librariesdirect.AboutLibrariesActivity;
import godau.fynn.librariesdirect.AboutLibrariesConfig;
import godau.fynn.librariesdirect.Library;
import godau.fynn.librariesdirect.License;

/**
 * Launcher Activity for the App Usage Statistics sample app.
 */
public class AppUsageStatisticsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_usage_statistics);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, AppUsageStatisticsFragment.newInstance())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_about:

                AboutLibrariesConfig.setLibraries(new Library[]{
                        new Library("usageDirect", License.APACHE_20_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/usageDirect"),
                        new Library("AppUsageStatistics", License.APACHE_20_LICENSE, null, "The Android Open Source Project, Inc", "https://github.com/googlesamples/android-AppUsageStatistics"),
                        new Library("Humanize", License.APACHE_20_LICENSE, null, "mfornos", "http://mfornos.github.io/humanize/"),
                        new Library("librariesDirect", License.CC0_LICENSE, null, "Fynn Godau", "https://codeberg.org/fynngodau/librariesDirect"),
                });

                AboutLibrariesConfig.setHeaderText(getString(R.string.about_libraries_header));

                startActivity(new Intent(this, AboutLibrariesActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }
}
