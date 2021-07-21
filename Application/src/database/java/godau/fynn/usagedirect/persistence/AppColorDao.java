package godau.fynn.usagedirect.persistence;

import android.database.Cursor;
import androidx.annotation.Nullable;
import androidx.room.*;
import godau.fynn.usagedirect.persistence.combined.ColoredSimpleUsageStat;
import godau.fynn.usagedirect.persistence.combined.TimeAppColor;

import java.util.LinkedHashMap;
import java.util.Map;

@Dao
public abstract class AppColorDao {

    /**
     * @see #updateExclusive(AppColor[])
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void insert(AppColor[] appColors);

    @Delete
    public abstract void delete(AppColor appColor);

    @Query("SELECT * FROM colors WHERE applicationId = :applicationId")
    public abstract @Nullable
    AppColor getAppColor(String applicationId);

    @Query("DELETE FROM colors")
    protected abstract void delete();

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
                    "ORDER BY day, priority DESC"
    )
    public abstract ColoredSimpleUsageStat[] getColoredUsageStats();

    /**
     * Used only for {@link #getAppColorMap()}
     *
     * @return A cursor which reads a matching of application ID to its color
     */
    @Query("SELECT applicationId, color FROM colors")
    protected abstract Cursor getAppColorCursor();


    /**
     * Delete all colors, then insert the provided ones
     */
    @Transaction
    public void updateExclusive(AppColor[] appColors) {
        delete();
        insert(appColors);
    }

    /**
     * @return Mapping of package names to their color
     */
    public Map<String, Integer> getAppColorMap() {
        Cursor cursor = getAppColorCursor();
        Map<String, Integer> map = new LinkedHashMap<>();

        // Cursor starts before first row
        while (cursor.moveToNext()) {
            map.put(cursor.getString(0), cursor.getInt(1));
        }

        cursor.close();
        return map;
    }
}
