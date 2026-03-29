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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract fun insertHostnames(hostnames: List<StoredHostname>)

    @Query("SELECT * FROM hostnames WHERE hostname IN (:hostnames)")
    protected abstract fun getHostnamesByNames(hostnames: List<String>): List<StoredHostname>

    @Insert
    protected abstract fun insertStored(session: StoredBrowserTabSession): Long

    @Query(
        "SELECT browserTabSessions.id AS id, " +
            "browserTabSessions.openedAt AS openedAt, " +
            "browserTabSessions.closedAt AS closedAt, " +
            "apps.applicationId AS applicationId, " +
            "hostnames.hostname AS hostname, " +
            "browserTabSessions.privacyMode AS privacyMode, " +
            "browserTabSessions.urlConfidence AS urlConfidence, " +
            "browserTabSessions.closeReason AS closeReason " +
            "FROM browserTabSessions " +
            "INNER JOIN apps ON apps.id = browserTabSessions.appId " +
            "INNER JOIN hostnames ON hostnames.id = browserTabSessions.hostnameId " +
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
            "hostnames.hostname AS hostname, " +
            "browserTabSessions.privacyMode AS privacyMode, " +
            "browserTabSessions.urlConfidence AS urlConfidence, " +
            "browserTabSessions.closeReason AS closeReason " +
            "FROM browserTabSessions " +
            "INNER JOIN apps ON apps.id = browserTabSessions.appId " +
            "INNER JOIN hostnames ON hostnames.id = browserTabSessions.hostnameId " +
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
            "SET hostnameId = :hostnameId, privacyMode = :privacyMode, urlConfidence = :urlConfidence " +
            "WHERE id = :id"
    )
    protected abstract fun updateMetadata(
        id: Long,
        hostnameId: Long,
        privacyMode: Int,
        urlConfidence: Int
    )

    @Query(
        "UPDATE browserTabSessions " +
            "SET closedAt = :closedAt, closeReason = :closeReason, " +
            "hostnameId = :hostnameId, privacyMode = :privacyMode, urlConfidence = :urlConfidence " +
            "WHERE id = :id"
    )
    protected abstract fun closeSession(
        id: Long,
        closedAt: Long,
        closeReason: Int,
        hostnameId: Long,
        privacyMode: Int,
        urlConfidence: Int
    )

    @Transaction
    open fun insertOpenSession(
        applicationId: String,
        openedAt: Long,
        hostname: String,
        privacyMode: Int,
        urlConfidence: Int
    ): Long {
        val appId = resolveAppIds(listOf(applicationId))[applicationId] ?: return -1
        val hostnameId = resolveHostnameIds(listOf(hostname))[hostname] ?: return -1
        return insertStored(
            StoredBrowserTabSession(
                appId = appId,
                hostnameId = hostnameId,
                openedAt = openedAt,
                privacyMode = privacyMode,
                urlConfidence = urlConfidence
            )
        )
    }

    @Transaction
    open fun updateOpenSessionMetadata(
        id: Long,
        hostname: String,
        privacyMode: Int,
        urlConfidence: Int
    ) {
        val hostnameId = resolveHostnameIds(listOf(hostname))[hostname] ?: return
        updateMetadata(id, hostnameId, privacyMode, urlConfidence)
    }

    @Transaction
    open fun closeOpenSession(
        id: Long,
        closedAt: Long,
        closeReason: Int,
        hostname: String,
        privacyMode: Int,
        urlConfidence: Int
    ) {
        val hostnameId = resolveHostnameIds(listOf(hostname))[hostname] ?: return
        closeSession(id, closedAt, closeReason, hostnameId, privacyMode, urlConfidence)
    }

    private fun resolveAppIds(applicationIds: Collection<String>): Map<String, Long> {
        val uniqueIds = LinkedHashSet(applicationIds)
        if (uniqueIds.isEmpty()) return emptyMap()

        insertApps(uniqueIds.map { StoredApp(applicationId = it) })
        return getAppsByApplicationIds(uniqueIds.toList())
            .associate { it.applicationId to it.id }
    }

    private fun resolveHostnameIds(hostnames: Collection<String>): Map<String, Long> {
        val uniqueNames = LinkedHashSet(hostnames)
        if (uniqueNames.isEmpty()) return emptyMap()

        insertHostnames(uniqueNames.map { StoredHostname(hostname = it) })
        return getHostnamesByNames(uniqueNames.toList())
            .associate { it.hostname to it.id }
    }
}
