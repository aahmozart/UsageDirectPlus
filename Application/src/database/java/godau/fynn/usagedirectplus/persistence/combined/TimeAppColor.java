package godau.fynn.usagedirectplus.persistence.combined;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import godau.fynn.usagedirectplus.SimpleUsageStat;
import godau.fynn.usagedirectplus.persistence.AppColor;

/**
 * {@link SimpleUsageStat} that is enhanced with its total usage time
 */
public class TimeAppColor {

    private final int totalTimeUsed;

    @Embedded(prefix = "color_")
    private AppColor appColor;

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

    public void setAppColor(AppColor appColor) {
        this.appColor = appColor;
    }
}
