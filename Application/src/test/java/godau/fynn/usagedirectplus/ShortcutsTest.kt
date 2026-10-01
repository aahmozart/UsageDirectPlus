package godau.fynn.usagedirectplus

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.xmlpull.v1.XmlPullParser

@RunWith(RobolectricTestRunner::class)
class ShortcutsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun shortcutIntentsTargetTheInstalledPackage() {
        val targetPackages = mutableListOf<String?>()

        val parser = context.resources.getXml(R.xml.shortcuts)
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "intent") {
                targetPackages += parser.getAttributeValue(ANDROID_NAMESPACE, "targetPackage")
            }
        }

        assertThat(targetPackages).isNotEmpty()
        for (targetPackage in targetPackages) {
            assertThat(targetPackage).isEqualTo(BuildConfig.APPLICATION_ID)
        }
    }

    companion object {
        private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
    }
}
