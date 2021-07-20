package godau.fynn.usagedirect.persistence.combined;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import godau.fynn.usagedirect.SimpleUsageStat;
import godau.fynn.usagedirect.persistence.AppColor;

/**
 * {@link SimpleUsageStat} that is enhanced with its total usage time
 */
public class TimeAppColor {

    private final int totalTimeUsed;

    @Embedded(prefix = "color_")
    private final AppColor appColor;

    private final String applicationId;

    public TimeAppColor(AppColor appColor, int totalTimeUsed, String applicationId) {
        this.appColor = appColor;
        this.totalTimeUsed = totalTimeUsed;
        this.applicationId = applicationId;
    }

    public int getTotalTimeUsed() {
        return totalTimeUsed;
    }

    public AppColor getAppColor() {
        return appColor;
    }

    public String getApplicationId() {
        return applicationId;
    }
}
