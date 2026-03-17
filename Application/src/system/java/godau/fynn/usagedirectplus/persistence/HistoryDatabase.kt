package godau.fynn.usagedirectplus.persistence

import android.content.Context

/**
 * STUB
 */
class HistoryDatabase private constructor(val context: Context) {

    fun close() {}

    companion object {
        @JvmStatic
        fun get(context: Context): HistoryDatabase {
            return HistoryDatabase(context)
        }
    }
}
