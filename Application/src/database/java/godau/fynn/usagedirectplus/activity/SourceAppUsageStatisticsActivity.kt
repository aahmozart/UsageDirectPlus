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
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import godau.fynn.usagedirectplus.DebugMenu
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.ActivityAppUsageStatisticsBinding
import godau.fynn.usagedirectplus.SimpleUsageStat
import godau.fynn.usagedirectplus.persistence.EventLogRunnable
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.view.adapter.database.DatabaseTimespanPagerAdapter
import godau.fynn.usagedirectplus.view.dialog.ExportDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Different implementation of AUSA for the two source flavors
 */
class SourceAppUsageStatisticsActivity : AppUsageStatisticsActivity() {

    private lateinit var databaseTimespanPagerAdapter: DatabaseTimespanPagerAdapter

    private var lastContextMenuTag: Any? = null
    private var currentExportDialog: ExportDialog? = null

    val exportDirectoryPickerLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            currentExportDialog?.onDirectorySelected(uri)
        }
    }

    private val colorActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            reload()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val binding = ActivityAppUsageStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseTimespanPagerAdapter = DatabaseTimespanPagerAdapter(this@SourceAppUsageStatisticsActivity)

        super.onCreate(savedInstanceState)
    }

    override fun prepare() {
        EventLogRunnable(this).run()

        databaseTimespanPagerAdapter.prepare(0)
    }

    override fun setAdapter(viewPager: ViewPager) {
        val usageListViewPagerAdapter = databaseTimespanPagerAdapter.getUsageListViewPagerAdapter(0)
        viewPager.adapter = usageListViewPagerAdapter
        viewPager.currentItem = usageListViewPagerAdapter.count
    }

    override fun onReload(viewPager: ViewPager, progressBar: ProgressBar, then: Runnable) {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch(Dispatchers.IO) {
            prepare()

            withContext(Dispatchers.Main) {
                databaseTimespanPagerAdapter.notifyDataSetChanged()
                viewPager.adapter!!.notifyDataSetChanged()

                progressBar.visibility = View.GONE

                then.run()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (DebugMenu.onOptionsItemSelected(item, this)) {
            return true
        } else when (item.itemId) {
            R.id.menu_color ->
                colorActivityLauncher.launch(Intent(this, ColorActivity::class.java))

            R.id.menu_feedback ->
                MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.menu_feedback)
                    .setMessage(R.string.feedback_message)
                    .setPositiveButton(R.string.menu_feedback) { _, _ ->
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_email))))
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()

            R.id.menu_restore_hidden ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val database = HistoryDatabase.get(this@SourceAppUsageStatisticsActivity)
                    val hiddenAmount = database.getUsageStatsDao().getHiddenAmount()

                    withContext(Dispatchers.Main) {
                        MaterialAlertDialogBuilder(this@SourceAppUsageStatisticsActivity)
                            .setTitle(R.string.menu_restore_hidden)
                            .setMessage(
                                resources.getQuantityString(
                                    R.plurals.menu_restore_hidden_details,
                                    hiddenAmount, hiddenAmount
                                )
                            )
                            .setPositiveButton(R.string.menu_restore_hidden_positive) { _, _ ->
                                lifecycleScope.launch(Dispatchers.IO) {
                                    database.getUsageStatsDao().markUnhiddenAll()
                                    database.close()
                                    withContext(Dispatchers.Main) { reload() }
                                }
                            }
                            .setNegativeButton(R.string.cancel) { _, _ -> database.close() }
                            .show()
                    }
                }

            R.id.menu_export_database -> {
                val dialog = ExportDialog(this, exportDirectoryPickerLauncher)
                currentExportDialog = dialog
                dialog.show()
            }

            R.id.menu_export_settings ->
                startActivity(Intent(this, ExportSettingsActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        DebugMenu.addTo(menu)

        lifecycleScope.launch(Dispatchers.IO) {
            val database = HistoryDatabase.get(this@SourceAppUsageStatisticsActivity)
            val hasHidden = database.getUsageStatsDao().getHiddenAmount() > 0
            withContext(Dispatchers.Main) { menu.findItem(R.id.menu_restore_hidden).isVisible = hasHidden }
        }

        super.onCreateOptionsMenu(menu)
        return true
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?) {
        menuInflater.inflate(R.menu.usagestat_context_menu, menu)
        lastContextMenuTag = v.tag
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_context_hide -> {
                val hideUsageStat = lastContextMenuTag as SimpleUsageStat
                lifecycleScope.launch(Dispatchers.IO) {
                    val database = HistoryDatabase.get(this@SourceAppUsageStatisticsActivity)
                    database.getUsageStatsDao().markHidden(hideUsageStat)
                    database.close()

                    withContext(Dispatchers.Main) { reload() }
                }

                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

}
