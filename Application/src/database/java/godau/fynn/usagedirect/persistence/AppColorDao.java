package godau.fynn.usagedirect.persistence;

import androidx.annotation.Nullable;
import androidx.room.*;
import godau.fynn.usagedirect.persistence.combined.ColoredSimpleUsageStat;
import godau.fynn.usagedirect.persistence.combined.TimeAppColor;

@Dao
public abstract class AppColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(AppColor[] appColors);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(AppColor appColors);

    @Delete
    public abstract void delete(AppColor appColor);

    @Query("SELECT * FROM colors WHERE applicationId = :applicationId")
    public abstract @Nullable
    AppColor getAppColor(String applicationId);

    @Query(
            "SELECT usageStats.applicationId, colors.applicationId as color_applicationId, sum(timeUsed) AS totalTimeUsed, color AS color_color, priority AS color_priority FROM usageStats " +
                    "LEFT JOIN colors ON usageStats.applicationId == colors.applicationId " +
                    "WHERE hidden == 0 " +
                    "GROUP BY usageStats.applicationId " +
                    "ORDER BY color_priority DESC, sum(timeUsed) DESC"
    )
    public abstract TimeAppColor[] getTimeAppColors();

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
}
