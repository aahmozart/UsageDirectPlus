package godau.fynn.usagedirect.persistence;

import android.content.Context;

/**
 * STUB
 */
public class HistoryDatabase {

    private final Context context;

    public static HistoryDatabase get(Context context) {
        return new HistoryDatabase(context);
    }

    private HistoryDatabase(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public void close() {
    }
}
