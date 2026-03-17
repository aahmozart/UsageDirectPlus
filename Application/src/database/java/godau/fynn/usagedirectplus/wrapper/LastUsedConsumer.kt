package godau.fynn.usagedirectplus.wrapper

import java.util.function.BiConsumer

/**
 * This consumer will be fed all times that usages of a package have ended and
 * only store the last one.
 */
class LastUsedConsumer : BiConsumer<String, Long> {

    @JvmField
    val applicationLastUsedMap: MutableMap<String, Long> = HashMap()

    override fun accept(applicationId: String, endTime: Long) {
        applicationLastUsedMap[applicationId] = endTime
    }
}
