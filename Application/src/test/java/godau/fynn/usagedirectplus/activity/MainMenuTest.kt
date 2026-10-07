package godau.fynn.usagedirectplus.activity

import android.content.Context
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainMenuTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /**
     * The activities are AppCompatActivities, whose menu inflater only honours
     * `app:showAsAction`. With `android:showAsAction` every item ended up in the
     * overflow menu and the toolbar lost its chart and color buttons.
     */
    @Test
    fun chartsActionIsShownInTheToolbar() {
        val menu = MenuBuilder(context)
        SupportMenuInflater(context).inflate(R.menu.menu, menu)

        val charts = menu.findItem(R.id.menu_charts) as MenuItemImpl
        assertThat(charts.requiresActionButton()).isTrue()

        val about = menu.findItem(R.id.menu_about) as MenuItemImpl
        assertThat(about.requiresActionButton()).isFalse()
    }
}
