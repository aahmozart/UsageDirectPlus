package godau.fynn.usagedirectplus.wrapper

import im.dacer.androidcharts.clockpie.ClockPieSegment
import java.time.Instant
import java.time.ZoneId

class ComponentForegroundStat(
    @JvmField val beginTime: Long,
    @JvmField val endTime: Long,
    @JvmField val packageName: String
) {
    fun asClockPieSegment(): ClockPieSegment {
        val begin = Instant
            .ofEpochMilli(beginTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
        val end = Instant
            .ofEpochMilli(endTime)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()

        return ClockPieSegment(
            begin.hour, begin.minute, begin.second - 10,
            end.hour, end.minute, end.second + 10
        )
    }

    override fun toString(): String {
        return "ComponentForegroundStat{" +
            "beginTime=" + Instant.ofEpochMilli(beginTime) +
            ", endTime=" + Instant.ofEpochMilli(endTime) +
            ", packageName='" + packageName + '\'' +
            '}'
    }
}
