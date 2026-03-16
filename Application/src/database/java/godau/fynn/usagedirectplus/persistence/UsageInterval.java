package godau.fynn.usagedirectplus.persistence;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        tableName = "usageIntervals",
        primaryKeys = {"beginTime", "applicationId"},
        indices = {
                @Index("applicationId"),
                @Index("beginTime")
        }
)
public class UsageInterval {

    private final long beginTime;
    private final long endTime;
    @NonNull
    private final String applicationId;

    public UsageInterval(long beginTime, long endTime, @NonNull String applicationId) {
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.applicationId = applicationId;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @NonNull
    public String getApplicationId() {
        return applicationId;
    }
}
