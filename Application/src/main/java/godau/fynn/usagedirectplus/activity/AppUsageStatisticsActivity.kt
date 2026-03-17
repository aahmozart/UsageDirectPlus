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
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.SmartTabLayout
import godau.fynn.librariesdirect.AboutDirectActivity
import godau.fynn.librariesdirect.model.*
import godau.fynn.usagedirectplus.BuildConfig
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.view.dialog.GrantPermissionDialog
import godau.fynn.usagedirectplus.wrapper.UsageStatsManagerWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Shared code for both source flavors
 */
abstract class AppUsageStatisticsActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var progressBar: ProgressBar
    private lateinit var tabs: SmartTabLayout

    companion object {
        private const val RELOAD_INTERVAL = 5 * 60 * 1000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tabs = findViewById(R.id.viewpagertab)

        tabs.elevation = supportActionBar!!.elevation

        progressBar = findViewById(R.id.progress)
        viewPager = findViewById(R.id.viewpager)

        if (!UsageStatsManagerWrapper.isPermissionGranted(this)) {
            GrantPermissionDialog(this).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch(Dispatchers.IO) {
            prepare()

            withContext(Dispatchers.Main) {
                setAdapter(viewPager)

                viewPager.offscreenPageLimit = 3

                tabs.setViewPager(viewPager)
                progressBar.visibility = View.GONE
            }

            // Schedule reload
            while (isActive) {
                delay(RELOAD_INTERVAL)
                withContext(Dispatchers.Main) { this@AppUsageStatisticsActivity.reload() }
            }
        }
    }

    protected fun reload() {
        onReload(viewPager, progressBar) { tabs.setViewPager(viewPager) }
        invalidateOptionsMenu()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_about -> {
                val intent = AboutDirectActivity.IntentBuilder(this, R.string.app_name, BuildConfig.VERSION_NAME)
                    .setIcon(R.mipmap.ic_launcher)
                    .setAppDeveloperName("Fynn Godau")
                    .setAppDeveloperMastodon("https://fosstodon.org/@fynnDirect")
                    .setContent(arrayOf<Any>(
                        Artwork(getString(R.string.icon), License("CC BY-SA", null), null, "m4TZ", false, "https://social.anoxinon.de/@m4TZ"),
                        Translator("Стоян", null, Locale("bg")),
                        Translator("dc7ia", null, Locale("da")),
                        Translator("mondstern", null, Locale("da")),
                        Translator("Lars M\u00fchlbauer", null, Locale("de")),
                        Translator("Max Schallert", null, Locale("de")),
                        Translator("mondstern", null, Locale("de")),
                        Translator("Daniel Garcia Pallaviccini", null, Locale("es")),
                        Translator("Porrumentzio", null, Locale("eu")),
                        Translator("mondstern", null, Locale("eu")),
                        Translator("J. Lavoie", null, Locale("fr")),
                        Translator("eUgEntOptIc44", null, Locale("fr")),
                        Translator("jlemonde", null, Locale("fr")),
                        Translator("Xos\u00e9 M", null, Locale("gl")),
                        Translator("liimee", null, Locale("id")),
                        Translator("mondstern", null, Locale("id")),
                        Translator("Giordano Scarso", null, Locale("it")),
                        Translator("Thomas Di Cristofaro", null, Locale("it")),
                        Translator("Allan Nordh\u00f8y", null, Locale("nb")),
                        Translator("dc7ia", null, Locale("nb")),
                        Translator("mondstern", null, Locale("nb")),
                        Translator("Vistaus", null, Locale("nl")),
                        Translator("mondstern", null, Locale("nl")),
                        Translator("Oliwier Jaszczyszyn", null, Locale("pl")),
                        Translator("ewm", null, Locale("pl")),
                        Translator("Andr\u00e9 Marcelo Alvarenga", null, Locale("pt", "br")),
                        Translator("aevw", null, Locale("pt", "br")),
                        Translator("mondstern", null, Locale("pt", "br")),
                        Translator("Rikishi", null, Locale("ru")),
                        Translator("mondstern", null, Locale("ru")),
                        Translator("Hatsune Miku", null, Locale("si")),
                        Translator("dc7ia", null, Locale("sv")),
                        Translator("mondstern", null, Locale("sv")),
                        Translator("Naveen", null, Locale("ta")),
                        Translator("mondstern", null, Locale("ta")),
                        Translator("Quang Trung", null, Locale("vi")),
                        Translator("yeyuan98", null, Locale("zh", "CN")),
                        Library("PrettyTime", License.APACHE_20_LICENSE, null, "ocpsoft", false, "https://www.ocpsoft.org/prettytime/"),
                        Library("SmartTabLayout", License.APACHE_20_LICENSE, null, "ogaclejapan", false, "https://github.com/ogaclejapan/SmartTabLayout"),
                        Library("Pikolo", License.APACHE_20_LICENSE, null, "Madrapps", false, "https://github.com/Madrapps/Pikolo/"),
                        Library("chartDirect", License.MIT_LICENSE, "The MIT License (MIT)\n" +
                                "\n" +
                                "Copyright (c) 2020 Fynn Godau\n" +
                                "\n" +
                                "Copyright (c) 2013 Ding Wenhao", "Fynn Godau and AndroidChart contributors", true, "https://codeberg.org/fynngodau/chartDirect"
                        ),
                        Library("TypedRecyclerView", License.CC0_LICENSE, null, "Fynn Godau", false, "https://codeberg.org/fynngodau/TypedRecyclerView"),
                        Library("librariesDirect", License.CC0_LICENSE, null, "Fynn Godau", false, "https://codeberg.org/fynngodau/librariesDirect"),
                        Fork("AppUsageStatistics", License.APACHE_20_LICENSE, null, "AOSP", false, "https://github.com/googlesamples/android-AppUsageStatistics"),
                        OwnLicense(License.GNU_GPL_V3_OR_LATER_LICENSE, null, "https://codeberg.org/fynngodau/usageDirect"),
                        Imprint("Fynn Godau\n" +
                                "St\u00f6\u00dfelstra\u00dfe 6\n" +
                                "97422 Schweinfurt\n" +
                                "Deutschland\n" +
                                "\n" +
                                "fynngodau@mailbox.org\n" +
                                "+49 9721 730335\n" +
                                "\n" +
                                "Umsatzsteuer-Identifikationsnummer (VAT identification number): DE347187327\n" +
                                "\n" +
                                "Plattform der EU zur au\u00dfergerichtlichen Streitbeilegung\n" +
                                "(EU platform for Online Dispute Resolution):\n" +
                                "https://ec.europa.eu/consumers/odr/"),
                    ))
                    .build()

                startActivity(intent)
            }

            R.id.menu_reload -> reload()

            R.id.menu_charts -> startActivity(Intent(this, ChartsActivity::class.java))
        }

        return super.onOptionsItemSelected(item)
    }

    val grantPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        recreate()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    /**
     * Not on the main thread. Executed before [setAdapter] is called.
     */
    protected abstract fun prepare()

    /**
     * Responsible for setting an adapter to the passed view pager and possibly
     * configuring it further.
     *
     * @param viewPager ViewPager to configure
     */
    protected abstract fun setAdapter(viewPager: ViewPager)

    /**
     * @param then To be executed on the UI thread after reload is complete
     */
    protected abstract fun onReload(viewPager: ViewPager, progressBar: ProgressBar, then: Runnable)
}
