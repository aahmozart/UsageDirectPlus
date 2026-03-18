package godau.fynn.usagedirectplus.thread.icon

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IconThreadTest {

    private val packageManager = mockk<PackageManager>()
    private val context = mockk<Context> {
        every { packageManager } returns this@IconThreadTest.packageManager
    }
    private val layout = mockk<RecyclerView.LayoutManager>()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        IconThread.iconMap.clear()
        IconThread.nameMap.clear()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkStatic(Log::class)
    }

    private fun createThread(applicationIds: Array<String>): IconThread {
        return object : IconThread(applicationIds, layout, context) {
            override fun onIconLoaded(position: Int, applicationId: String) {
                // no-op: avoids RecyclerView dependency in JVM tests
            }
        }
    }

    @Test
    fun `icon and name are cached when package is visible`() {
        val icon = mockk<Drawable>()
        val appInfo = ApplicationInfo()

        every { packageManager.getApplicationIcon("com.visible") } returns icon
        every { packageManager.getApplicationInfo("com.visible", 0) } returns appInfo
        every { packageManager.getApplicationLabel(appInfo) } returns "Visible App"

        val thread = createThread(arrayOf("com.visible"))
        thread.run()

        assertThat(IconThread.iconMap).containsKey("com.visible")
        assertThat(IconThread.iconMap["com.visible"]).isSameInstanceAs(icon)
        assertThat(IconThread.nameMap).containsKey("com.visible")
        assertThat(IconThread.nameMap["com.visible"]).isEqualTo("Visible App")
    }

    @Test
    fun `NameNotFoundException is handled gracefully`() {
        every { packageManager.getApplicationIcon("com.hidden") } throws
            PackageManager.NameNotFoundException("com.hidden")

        val thread = createThread(arrayOf("com.hidden"))
        thread.run()

        assertThat(IconThread.iconMap).doesNotContainKey("com.hidden")
        assertThat(IconThread.nameMap).doesNotContainKey("com.hidden")
    }

    @Test
    fun `mix of visible and invisible packages caches only visible ones`() {
        val iconA = mockk<Drawable>()
        val appInfoA = ApplicationInfo()

        every { packageManager.getApplicationIcon("com.a") } returns iconA
        every { packageManager.getApplicationInfo("com.a", 0) } returns appInfoA
        every { packageManager.getApplicationLabel(appInfoA) } returns "App A"

        every { packageManager.getApplicationIcon("com.b") } throws
            PackageManager.NameNotFoundException("com.b")

        val iconC = mockk<Drawable>()
        val appInfoC = ApplicationInfo()

        every { packageManager.getApplicationIcon("com.c") } returns iconC
        every { packageManager.getApplicationInfo("com.c", 0) } returns appInfoC
        every { packageManager.getApplicationLabel(appInfoC) } returns "App C"

        val thread = createThread(arrayOf("com.a", "com.b", "com.c"))
        thread.run()

        assertThat(IconThread.iconMap.keys).containsExactly("com.a", "com.c")
        assertThat(IconThread.nameMap.keys).containsExactly("com.a", "com.c")
        assertThat(IconThread.nameMap["com.a"]).isEqualTo("App A")
        assertThat(IconThread.nameMap["com.c"]).isEqualTo("App C")
    }
}
