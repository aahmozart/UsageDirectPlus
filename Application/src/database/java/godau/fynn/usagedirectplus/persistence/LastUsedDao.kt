package godau.fynn.usagedirectplus.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
abstract class LastUsedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(lastUsedStats: Array<LastUsedStat>)

    @Query("SELECT * FROM lastUsed")
    abstract fun getLastUsedStats(): Array<LastUsedStat>

    fun insert(applicationLastUsedMap: Map<String, Long>) {
        val lastUsedStats = applicationLastUsedMap.entries.map { (applicationId, lastUsed) ->
            LastUsedStat(applicationId, lastUsed)
        }.toTypedArray()
        insert(lastUsedStats)
    }
}
