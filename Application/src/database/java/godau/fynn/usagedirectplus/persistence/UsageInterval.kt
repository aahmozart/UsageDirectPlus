package godau.fynn.usagedirectplus.persistence

data class UsageInterval(
    val beginTime: Long,
    val endTime: Long,
    val applicationId: String
)
