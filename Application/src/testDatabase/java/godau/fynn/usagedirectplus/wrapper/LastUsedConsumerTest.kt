package godau.fynn.usagedirectplus.wrapper

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class LastUsedConsumerTest {

    @Test
    fun `initial map is empty`() {
        val consumer = LastUsedConsumer()

        assertThat(consumer.applicationLastUsedMap).isEmpty()
    }

    @Test
    fun `accept stores value`() {
        val consumer = LastUsedConsumer()

        consumer.accept("com.example", 1000L)

        assertThat(consumer.applicationLastUsedMap).containsEntry("com.example", 1000L)
    }

    @Test
    fun `accept overwrites previous value for same application`() {
        val consumer = LastUsedConsumer()

        consumer.accept("com.example", 1000L)
        consumer.accept("com.example", 2000L)

        assertThat(consumer.applicationLastUsedMap["com.example"]).isEqualTo(2000L)
        assertThat(consumer.applicationLastUsedMap).hasSize(1)
    }

    @Test
    fun `accept stores separate entries for different applications`() {
        val consumer = LastUsedConsumer()

        consumer.accept("com.a", 1000L)
        consumer.accept("com.b", 2000L)

        assertThat(consumer.applicationLastUsedMap).hasSize(2)
        assertThat(consumer.applicationLastUsedMap["com.a"]).isEqualTo(1000L)
        assertThat(consumer.applicationLastUsedMap["com.b"]).isEqualTo(2000L)
    }
}
