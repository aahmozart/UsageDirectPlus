package godau.fynn.usagedirect.persistence;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import java.util.Map;

@Dao
public abstract class LastUsedDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(LastUsedStat[] lastUsedStats);

    private int i;

    public void insert(Map<String, Long> applicationLastUsedMap) {
        LastUsedStat[] lastUsedStats = new LastUsedStat[applicationLastUsedMap.size()];

        i = 0;

        applicationLastUsedMap.forEach((applicationId, lastUsed) ->
                lastUsedStats[i++] = new LastUsedStat(applicationId, lastUsed));

        insert(lastUsedStats);
    }


}
