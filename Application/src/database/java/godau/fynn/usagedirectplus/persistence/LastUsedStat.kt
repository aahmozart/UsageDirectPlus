package godau.fynn.usagedirectplus.persistence

data class LastUsedStat(
    @JvmField val applicationId: String,
    @JvmField val lastUsed: Long
)
