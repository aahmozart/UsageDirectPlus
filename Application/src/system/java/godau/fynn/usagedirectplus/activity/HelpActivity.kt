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

package godau.fynn.usagedirectplus.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    companion object {
        private const val DATABASE_FLAVOR_PACKAGE_NAME = "godau.fynn.usagedirectplus"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val databaseIntent = packageManager.getLaunchIntentForPackage(DATABASE_FLAVOR_PACKAGE_NAME)
        val databaseInstalled = databaseIntent != null

        if (databaseInstalled) {
            binding.textDatabase.setText(R.string.help_database_flavor_installed)
            binding.databaseLayout.elevation = 0f
            binding.databaseLayout.isClickable = false
        } else {
            binding.databaseLayout.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/$DATABASE_FLAVOR_PACKAGE_NAME")))
            }
        }
    }
}
