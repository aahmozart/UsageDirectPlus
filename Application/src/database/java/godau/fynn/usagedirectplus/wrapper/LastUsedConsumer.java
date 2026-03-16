package godau.fynn.usagedirectplus.wrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * This consumer will be fed all times that usages of a package have ended and
 * only store the last one.
 */
public class LastUsedConsumer implements BiConsumer<String, Long> {

    public final Map<String, Long> applicationLastUsedMap = new HashMap<>();

    @Override
    public void accept(String applicationId, Long endTime) {
        applicationLastUsedMap.put(applicationId, endTime);
    }

}
