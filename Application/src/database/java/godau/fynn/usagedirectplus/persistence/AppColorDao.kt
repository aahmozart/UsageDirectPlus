package godau.fynn.usagedirectplus.persistence

import android.database.Cursor
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import godau.fynn.usagedirectplus.persistence.combined.ColoredSimpleUsageStat
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor

@Dao
abstract class AppColorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertStored(appColors: Array<StoredAppColor>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertApps(apps: List<StoredApp>)

    @Query("SELECT * FROM apps WHERE applicationId IN (:applicationIds)")
    protected abstract fun getAppsByApplicationIds(applicationIds: List<String>): List<StoredApp>

    @Query("DELETE FROM colors WHERE appId = :appId")
    protected abstract fun deleteByAppId(appId: Long)

    @Query(
        "SELECT apps.applicationId AS applicationId, colors.color AS color, colors.priority AS priority " +
            "FROM colors " +
            "INNER JOIN apps ON apps.id = colors.appId " +
            "WHERE apps.applicationId = :applicationId"
    )
    abstract fun getAppColor(applicationId: String): AppColor?

    @Query("DELETE FROM colors")
    protected abstract fun deleteAll()

    @Query(
        "SELECT apps.applicationId AS applicationId, colorApps.applicationId AS color_applicationId, " +
            "sum(usageStats.timeUsed) AS totalTimeUsed, colors.color AS color_color, colors.priority AS color_priority " +
            "FROM usageStats " +
            "INNER JOIN apps ON apps.id = usageStats.appId " +
            "LEFT JOIN colors ON usageStats.appId = colors.appId " +
            "LEFT JOIN apps AS colorApps ON colorApps.id = colors.appId " +
            "WHERE usageStats.hidden == 0 " +
            "GROUP BY usageStats.appId " +
            "ORDER BY colors.priority DESC, sum(usageStats.timeUsed) DESC"
    )
    abstract fun getTimeAppColors(): Array<TimeAppColor>

    @Query(
        "SELECT apps.applicationId AS applicationId, usageStats.timeUsed AS timeUsed, colors.color AS color, " +
            "usageStats.day AS day, usageStats.hidden AS hidden " +
            "FROM usageStats " +
            "INNER JOIN apps ON apps.id = usageStats.appId " +
            "LEFT JOIN colors ON usageStats.appId == colors.appId " +
            "WHERE usageStats.hidden == 0 " +
            "ORDER BY usageStats.day, colors.priority DESC"
    )
    abstract fun getColoredUsageStats(): Array<ColoredSimpleUsageStat>

    @Query(
        "SELECT apps.applicationId AS applicationId, colors.color AS color " +
            "FROM colors " +
            "INNER JOIN apps ON apps.id = colors.appId"
    )
    protected abstract fun getAppColorCursor(): Cursor

    @Transaction
    open fun delete(appColor: AppColor) {
        val appId = resolveAppIds(listOf(appColor.applicationId))[appColor.applicationId] ?: return
        deleteByAppId(appId)
    }

    @Transaction
    open fun updateExclusive(appColors: Array<AppColor>) {
        deleteAll()
        insert(appColors)
    }

    @Transaction
    open fun insert(appColors: Array<AppColor>) {
        if (appColors.isEmpty()) return

        val appIds = resolveAppIds(appColors.map(AppColor::applicationId))
        insertStored(
            appColors.mapNotNull { appColor ->
                val appId = appIds[appColor.applicationId] ?: return@mapNotNull null
                StoredAppColor(appId, appColor.color, appColor.priority)
            }.toTypedArray()
        )
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

    private fun resolveAppIds(applicationIds: Collection<String>): Map<String, Long> {
        val uniqueIds = LinkedHashSet(applicationIds)
        if (uniqueIds.isEmpty()) return emptyMap()

        insertApps(uniqueIds.map { StoredApp(applicationId = it) })
        return getAppsByApplicationIds(uniqueIds.toList())
            .associate { it.applicationId to it.id }
    }
}
