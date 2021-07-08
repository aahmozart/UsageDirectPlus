package godau.fynn.usagedirect.persistence;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Stores a color that has been chosen by the user for an application.
 */
@Entity(tableName = "colors")
public class AppColor {

    /**
     * Application ID, i.e. package name of the concerned package
     */
    @PrimaryKey
    public final @NonNull String applicationId;

    /**
     * Timestamp in milliseconds that the package was last used
     */
    public final @ColorInt int color;

    public final int priority;

    public AppColor(@NonNull String applicationId, int color, int priority) {
        this.applicationId = applicationId;
        this.color = color;
        this.priority = priority;
    }
}
