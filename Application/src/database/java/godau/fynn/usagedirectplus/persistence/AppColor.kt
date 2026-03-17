package godau.fynn.usagedirectplus.persistence

import androidx.annotation.ColorInt
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "colors")
data class AppColor(
    @PrimaryKey
    val applicationId: String,
    @ColorInt var color: Int,
    var priority: Int
) {
    @Ignore
    constructor(other: AppColor) : this(other.applicationId, other.color, other.priority)
}
