package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

/**
 * Remembers the directory the manual export dialog last wrote to, so it does not have to be picked
 * again on every export.
 *
 * This is deliberately kept separate from [DatabaseExportWorker.PREF_EXPORT_URI]: a one-off manual
 * export must not silently retarget the scheduled export. The scheduled directory is still used as
 * the initial suggestion when no manual pick has happened yet.
 */
class ExportDirectoryMemory(private val prefs: SharedPreferences) {

    companion object {
        const val PREF_MANUAL_EXPORT_URI = "manual_export_uri"

        @JvmStatic
        fun of(context: Context) = ExportDirectoryMemory(
            context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
        )
    }

    fun remember(uri: Uri) {
        prefs.edit().putString(PREF_MANUAL_EXPORT_URI, uri.toString()).apply()
    }

    /**
     * The directory to preselect, or `null` if there is nothing to suggest. The caller is
     * responsible for checking that it is still writable — a remembered directory can outlive its
     * permission grant, its storage volume or itself.
     */
    fun recall(): Uri? {
        val stored = prefs.getString(PREF_MANUAL_EXPORT_URI, null)
            ?: prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null)
            ?: return null

        return Uri.parse(stored)
    }

    /** Drops the remembered directory once it turns out to be unusable. */
    fun forget() {
        prefs.edit().remove(PREF_MANUAL_EXPORT_URI).apply()
    }
}
