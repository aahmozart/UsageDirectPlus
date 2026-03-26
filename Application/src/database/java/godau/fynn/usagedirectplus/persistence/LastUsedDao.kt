package godau.fynn.usagedirectplus.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class LastUsedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insertStored(lastUsedStats: Array<StoredLastUsedStat>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertApps(apps: List<StoredApp>)

    @Query("SELECT * FROM apps WHERE applicationId IN (:applicationIds)")
    protected abstract fun getAppsByApplicationIds(applicationIds: List<String>): List<StoredApp>

    @Query(
        "SELECT apps.applicationId AS applicationId, lastUsed.lastUsed AS lastUsed " +
            "FROM lastUsed " +
            "INNER JOIN apps ON apps.id = lastUsed.appId"
    )
    abstract fun getLastUsedStats(): Array<LastUsedStat>

    fun insert(applicationLastUsedMap: Map<String, Long>) {
        val appIds = resolveAppIds(applicationLastUsedMap.keys)
        val lastUsedStats = applicationLastUsedMap.entries.mapNotNull { (applicationId, lastUsed) ->
            val appId = appIds[applicationId] ?: return@mapNotNull null
            StoredLastUsedStat(appId, lastUsed)
        }.toTypedArray()
        insertStored(lastUsedStats)
    }

    private fun resolveAppIds(applicationIds: Collection<String>): Map<String, Long> {
        val uniqueIds = LinkedHashSet(applicationIds)
        if (uniqueIds.isEmpty()) return emptyMap()

        insertApps(uniqueIds.map { StoredApp(applicationId = it) })
        return getAppsByApplicationIds(uniqueIds.toList())
            .associate { it.applicationId to it.id }
    }
}
