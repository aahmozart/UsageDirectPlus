package godau.fynn.usagedirectplus.persistence.combined

import godau.fynn.usagedirectplus.SimpleUsageStat

class ColoredSimpleUsageStat(
    day: Long,
    timeUsed: Long,
    applicationId: String,
    hidden: Boolean,
    val color: Int?
) : SimpleUsageStat(day, timeUsed, applicationId, hidden)
