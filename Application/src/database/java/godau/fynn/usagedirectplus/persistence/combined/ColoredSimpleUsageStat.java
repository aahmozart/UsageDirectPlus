package godau.fynn.usagedirectplus.persistence.combined;

import androidx.annotation.NonNull;
import godau.fynn.usagedirectplus.SimpleUsageStat;

/**
 * {@link SimpleUsageStat} that is enhanced with a color value
 */
public class ColoredSimpleUsageStat extends SimpleUsageStat {

    private final Integer color;

    public ColoredSimpleUsageStat(long day, long timeUsed, @NonNull String applicationId, boolean hidden,
                                  Integer color) {
        super(day, timeUsed, applicationId, hidden);
        this.color = color;
    }


    public Integer getColor() {
        return color;
    }
}
