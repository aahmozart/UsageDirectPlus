package godau.fynn.usagedirectplus

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ComparatorTest {

    private val comparator = Comparator.TimeInForegroundComparatorDesc()

    @Test
    fun `sorts descending by timeUsed`() {
        val stats = mutableListOf(
            SimpleUsageStat(1L, 100L, "com.a"),
            SimpleUsageStat(1L, 500L, "com.b"),
            SimpleUsageStat(1L, 300L, "com.c")
        )

        stats.sortWith(comparator)

        assertThat(stats.map { it.applicationId }).containsExactly("com.b", "com.c", "com.a").inOrder()
    }

    @Test
    fun `equal timeUsed returns zero`() {
        val a = SimpleUsageStat(1L, 200L, "com.a")
        val b = SimpleUsageStat(1L, 200L, "com.b")

        assertThat(comparator.compare(a, b)).isEqualTo(0)
    }

    @Test
    fun `higher timeUsed comes first`() {
        val high = SimpleUsageStat(1L, 1000L, "com.high")
        val low = SimpleUsageStat(1L, 1L, "com.low")

        assertThat(comparator.compare(high, low)).isLessThan(0)
        assertThat(comparator.compare(low, high)).isGreaterThan(0)
    }
}
