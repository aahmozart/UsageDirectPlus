package godau.fynn.usagedirect.persistence;

import androidx.annotation.NonNull;
import androidx.room.Entity;

/**
 * Represents the point in time that a specific package was last used.
 */
@Entity(tableName = "lastUsed", primaryKeys = {"applicationId"})
public class LastUsedStat {

    /**
     * Application ID, i.e. package name, of the concerned package
     */
    public final @NonNull String applicationId;

    /**
     * Timestamp in milliseconds that the package was last used
     */
    public final long lastUsed;

    public LastUsedStat(@NonNull String applicationId, long lastUsed) {
        this.applicationId = applicationId;
        this.lastUsed = lastUsed;
    }
}
