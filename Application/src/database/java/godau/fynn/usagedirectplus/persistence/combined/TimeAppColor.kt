package godau.fynn.usagedirectplus.persistence.combined

import androidx.room.Embedded
import godau.fynn.usagedirectplus.persistence.AppColor

data class TimeAppColor(
    @Embedded(prefix = "color_")
    var appColor: AppColor?,
    val totalTimeUsed: Int,
    val applicationId: String
)
