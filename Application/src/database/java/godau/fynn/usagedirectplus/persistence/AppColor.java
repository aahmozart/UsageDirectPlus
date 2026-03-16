package godau.fynn.usagedirectplus.persistence;

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
    private final @NonNull String applicationId;

    /**
     * Timestamp in milliseconds that the package was last used
     */
    private @ColorInt int color;

    private int priority;

    public AppColor(@NonNull String applicationId, int color, int priority) {
        this.applicationId = applicationId;
        this.color = color;
        this.priority = priority;
    }

    public AppColor(AppColor appColor) {
        this.applicationId = appColor.applicationId;
        this.color = appColor.color;
        this.priority = appColor.priority;
    }

    public int getColor() {
        return color;
    }

    @NonNull
    public String getApplicationId() {
        return applicationId;
    }

    public int getPriority() {
        return priority;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
