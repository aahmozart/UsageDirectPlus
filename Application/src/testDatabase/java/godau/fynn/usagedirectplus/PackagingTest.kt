package godau.fynn.usagedirectplus

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PackagingTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun applicationIdIsInOwnNamespace() {
        assertThat(BuildConfig.APPLICATION_ID).isEqualTo("aah.mozart.usagedirectplus.debug")
    }

    @Test
    fun noAccessibilityServiceIsDeclared() {
        val services = context.packageManager
            .getPackageInfo(context.packageName, PackageManager.GET_SERVICES)
            .services
            .orEmpty()

        assertThat(services.map { it.name }).isNotEmpty()
        assertThat(services.filter { it.permission == android.Manifest.permission.BIND_ACCESSIBILITY_SERVICE })
            .isEmpty()
    }

    @Test
    fun feedbackOpensIssueTrackerInsteadOfUpstreamEmail() {
        assertThat(context.getString(R.string.url_feedback))
            .isEqualTo("https://github.com/aahmozart/UsageDirectPlus/issues")
    }
}
