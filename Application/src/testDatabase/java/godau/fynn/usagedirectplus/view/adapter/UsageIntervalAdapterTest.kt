package godau.fynn.usagedirectplus.view.adapter

import android.content.Context
import android.provider.Settings
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.persistence.UsageInterval
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
class UsageIntervalAdapterTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun bindRow(): String {
        val zone = ZoneId.systemDefault()
        val day = LocalDate.of(2024, 1, 1)
        val begin = day.atTime(LocalTime.of(16, 44)).atZone(zone).toInstant().toEpochMilli()
        val end = day.atTime(LocalTime.of(16, 46)).atZone(zone).toInstant().toEpochMilli()

        val adapter = UsageIntervalAdapter(listOf(UsageInterval(begin, end, "com.example.app")))
        val holder = adapter.onCreateViewHolder(FrameLayout(context), 0)
        adapter.onBindViewHolder(holder, 0)
        return holder.binding.timeRange.text.toString()
    }

    /**
     * The interval list used a fixed "h:mm a" pattern, so devices set to a 24-hour
     * clock still saw "4:44 PM – 4:46 PM".
     */
    @Test
    fun timeRangeFollowsThe24HourSystemSetting() {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, "24")

        val text = bindRow()

        assertThat(text).contains("16:44")
        assertThat(text).contains("16:46")
        assertThat(text).doesNotContainMatch("(?i)[ap]m")
    }

    @Test
    fun timeRangeFollowsThe12HourSystemSetting() {
        Settings.System.putString(context.contentResolver, Settings.System.TIME_12_24, "12")

        val text = bindRow()

        assertThat(text).contains("4:44")
        assertThat(text).containsMatch("(?i)pm")
    }
}
