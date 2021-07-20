package godau.fynn.usagedirect.persistence;

import androidx.annotation.Nullable;
import androidx.room.*;

@Dao
public abstract class AppColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(AppColor[] appColors);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(AppColor appColors);

    @Delete
    public abstract void delete(AppColor appColor);

    /**
     * @return {@link ColoredSimpleUsageStat} objects for each usage stat that has been
     * recorded in the database.
     */
    @Query(
            "SELECT usageStats.applicationId, timeUsed, color, day, hidden FROM usageStats " +
                    "LEFT JOIN colors ON usageStats.applicationId == colors.applicationId " +
                    "WHERE hidden == 0 " +
                    "ORDER BY day, priority"
    )
    public abstract ColoredSimpleUsageStat[] getColoredUsageStats();

    @Query("SELECT * FROM colors WHERE applicationId = :applicationId")
    public abstract @Nullable
    AppColor getAppColor(String applicationId);
}
