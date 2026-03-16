package godau.fynn.usagedirect.persistence;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "screenEvents",
        indices = {
                @Index("timestamp")
        }
)
public class ScreenEvent {

    @PrimaryKey
    private final long timestamp;
    private final int eventType;

    public static final int SCREEN_ON = 15;
    public static final int SCREEN_OFF = 16;
    public static final int KEYGUARD_SHOWN = 17;
    public static final int KEYGUARD_HIDDEN = 18;

    public ScreenEvent(long timestamp, int eventType) {
        this.timestamp = timestamp;
        this.eventType = eventType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getEventType() {
        return eventType;
    }
}
