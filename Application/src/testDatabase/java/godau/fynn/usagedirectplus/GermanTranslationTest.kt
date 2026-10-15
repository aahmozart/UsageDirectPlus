package godau.fynn.usagedirectplus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The export and interval screens shipped without German strings, so a German
 * system showed a mix of German and English.
 */
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "de")
class GermanTranslationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun exportAndIntervalScreensAreTranslated() {
        assertThat(context.getString(R.string.menu_export_settings)).isEqualTo("Automatischer Export")
        assertThat(context.getString(R.string.export_settings_title)).isEqualTo("Regelmäßiger Datenbankexport")
        assertThat(context.getString(R.string.usage_intervals_title)).isEqualTo("Nutzungsintervalle")
        assertThat(context.getString(R.string.export_dialog_description)).doesNotContain("Export the database")
    }
}
