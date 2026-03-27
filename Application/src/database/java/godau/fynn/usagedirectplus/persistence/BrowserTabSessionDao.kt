package godau.fynn.usagedirectplus.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class BrowserTabSessionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertApps(apps: List<StoredApp>)

    @Query("SELECT * FROM apps WHERE applicationId IN (:applicationIds)")
    protected abstract fun getAppsByApplicationIds(applicationIds: List<String>): List<StoredApp>

    @Insert
    protected abstract fun insertStored(session: StoredBrowserTabSession): Long

    @Query(
        "SELECT browserTabSessions.id AS id, " +
            "browserTabSessions.openedAt AS openedAt, " +
            "browserTabSessions.closedAt AS closedAt, " +
            "apps.applicationId AS applicationId, " +
            "browserTabSessions.title AS title, " +
            "browserTabSessions.url AS url, " +
            "browserTabSessions.privacyMode AS privacyMode, " +
            "browserTabSessions.urlConfidence AS urlConfidence, " +
            "browserTabSessions.closeReason AS closeReason " +
            "FROM browserTabSessions " +
            "INNER JOIN apps ON apps.id = browserTabSessions.appId " +
            "WHERE browserTabSessions.closedAt IS NULL " +
            "ORDER BY browserTabSessions.openedAt DESC " +
            "LIMIT 1"
    )
    abstract fun getOpenSession(): BrowserTabSession?

    @Query(
        "SELECT browserTabSessions.id AS id, " +
            "browserTabSessions.openedAt AS openedAt, " +
            "browserTabSessions.closedAt AS closedAt, " +
            "apps.applicationId AS applicationId, " +
            "browserTabSessions.title AS title, " +
            "browserTabSessions.url AS url, " +
            "browserTabSessions.privacyMode AS privacyMode, " +
            "browserTabSessions.urlConfidence AS urlConfidence, " +
            "browserTabSessions.closeReason AS closeReason " +
            "FROM browserTabSessions " +
            "INNER JOIN apps ON apps.id = browserTabSessions.appId " +
            "WHERE apps.applicationId = :applicationId " +
            "AND browserTabSessions.openedAt < :end " +
            "AND COALESCE(browserTabSessions.closedAt, 9223372036854775807) > :start " +
            "ORDER BY browserTabSessions.openedAt"
    )
    abstract fun getByAppAndTimeRange(applicationId: String, start: Long, end: Long): List<BrowserTabSession>

    @Query(
        "SELECT COUNT(*) FROM browserTabSessions " +
            "INNER JOIN apps ON apps.id = browserTabSessions.appId " +
            "WHERE apps.applicationId = :applicationId"
    )
    abstract fun getCountByApp(applicationId: String): Int

    @Query(
        "UPDATE browserTabSessions " +
            "SET title = :title, url = :url, privacyMode = :privacyMode, urlConfidence = :urlConfidence " +
            "WHERE id = :id"
    )
    protected abstract fun updateMetadata(
        id: Long,
        title: String?,
        url: String?,
        privacyMode: Int,
        urlConfidence: Int
    )

    @Query(
        "UPDATE browserTabSessions " +
            "SET closedAt = :closedAt, closeReason = :closeReason, " +
            "title = :title, url = :url, privacyMode = :privacyMode, urlConfidence = :urlConfidence " +
            "WHERE id = :id"
    )
    protected abstract fun closeSession(
        id: Long,
        closedAt: Long,
        closeReason: Int,
        title: String?,
        url: String?,
        privacyMode: Int,
        urlConfidence: Int
    )

    @Transaction
    open fun insertOpenSession(
        applicationId: String,
        openedAt: Long,
        title: String?,
        url: String?,
        privacyMode: Int,
        urlConfidence: Int
    ): Long {
        val appId = resolveAppIds(listOf(applicationId))[applicationId] ?: return -1
        return insertStored(
            StoredBrowserTabSession(
                appId = appId,
                openedAt = openedAt,
                title = title,
                url = url,
                privacyMode = privacyMode,
                urlConfidence = urlConfidence
            )
        )
    }

    @Transaction
    open fun updateOpenSessionMetadata(
        id: Long,
        title: String?,
        url: String?,
        privacyMode: Int,
        urlConfidence: Int
    ) {
        updateMetadata(id, title, url, privacyMode, urlConfidence)
    }

    @Transaction
    open fun closeOpenSession(
        id: Long,
        closedAt: Long,
        closeReason: Int,
        title: String?,
        url: String?,
        privacyMode: Int,
        urlConfidence: Int
    ) {
        closeSession(id, closedAt, closeReason, title, url, privacyMode, urlConfidence)
    }

    private fun resolveAppIds(applicationIds: Collection<String>): Map<String, Long> {
        val uniqueIds = LinkedHashSet(applicationIds)
        if (uniqueIds.isEmpty()) return emptyMap()

        insertApps(uniqueIds.map { StoredApp(applicationId = it) })
        return getAppsByApplicationIds(uniqueIds.toList())
            .associate { it.applicationId to it.id }
    }
}
