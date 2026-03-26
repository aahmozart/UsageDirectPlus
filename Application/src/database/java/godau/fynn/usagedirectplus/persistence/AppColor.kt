package godau.fynn.usagedirectplus.persistence

import androidx.annotation.ColorInt
import androidx.room.Ignore

data class AppColor(
    val applicationId: String,
    @ColorInt var color: Int,
    var priority: Int
) {
    @Ignore
    constructor(other: AppColor) : this(other.applicationId, other.color, other.priority)
}
