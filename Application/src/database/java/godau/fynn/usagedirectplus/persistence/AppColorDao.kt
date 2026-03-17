package godau.fynn.usagedirectplus.persistence

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import godau.fynn.usagedirectplus.persistence.combined.ColoredSimpleUsageStat
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor

@Dao
abstract class AppColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(appColors: Array<AppColor>)

    @Delete
    abstract fun delete(appColor: AppColor)

    @Query("SELECT * FROM colors WHERE applicationId = :applicationId")
    abstract fun getAppColor(applicationId: String): AppColor?

    @Query("DELETE FROM colors")
    protected abstract fun deleteAll()

    @Query(
        "SELECT usageStats.applicationId, colors.applicationId as color_applicationId, sum(timeUsed) AS totalTimeUsed, color AS color_color, priority AS color_priority FROM usageStats " +
            "LEFT JOIN colors ON usageStats.applicationId == colors.applicationId " +
            "WHERE hidden == 0 " +
            "GROUP BY usageStats.applicationId " +
            "ORDER BY color_priority DESC, sum(timeUsed) DESC"
    )
    abstract fun getTimeAppColors(): Array<TimeAppColor>

    @Query(
        "SELECT usageStats.applicationId, timeUsed, color, day, hidden FROM usageStats " +
            "LEFT JOIN colors ON usageStats.applicationId == colors.applicationId " +
            "WHERE hidden == 0 " +
            "ORDER BY day, priority DESC"
    )
    abstract fun getColoredUsageStats(): Array<ColoredSimpleUsageStat>

    @Query("SELECT applicationId, color FROM colors")
    protected abstract fun getAppColorCursor(): Cursor

    @Transaction
    open fun updateExclusive(appColors: Array<AppColor>) {
        deleteAll()
        insert(appColors)
    }

    fun getAppColorMap(): Map<String, Int> {
        val cursor = getAppColorCursor()
        val map = LinkedHashMap<String, Int>()
        while (cursor.moveToNext()) {
            map[cursor.getString(0)] = cursor.getInt(1)
        }
        cursor.close()
        return map
    }
}
