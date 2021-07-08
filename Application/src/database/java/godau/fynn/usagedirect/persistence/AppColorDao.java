package godau.fynn.usagedirect.persistence;

import androidx.room.*;

@Dao
public abstract class AppColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(AppColor[] appColors);

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
