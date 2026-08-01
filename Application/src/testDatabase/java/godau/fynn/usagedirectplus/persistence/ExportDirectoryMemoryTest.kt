package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ExportDirectoryMemoryTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var memory: ExportDirectoryMemory

    private val downloads: Uri =
        Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADownload")
    private val documents: Uri =
        Uri.parse("content://com.android.externalstorage.documents/tree/primary%3ADocuments")

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        prefs = context.getSharedPreferences(HistoryDatabase.DATABASE_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        memory = ExportDirectoryMemory(prefs)
    }

    @Test
    fun `recalls nothing before a directory has ever been picked`() {
        assertThat(memory.recall()).isNull()
    }

    @Test
    fun `remembers the directory that was picked`() {
        memory.remember(downloads)

        assertThat(memory.recall()).isEqualTo(downloads)
    }

    @Test
    fun `survives a new instance, as it would across app restarts`() {
        memory.remember(downloads)

        assertThat(ExportDirectoryMemory(prefs).recall()).isEqualTo(downloads)
    }

    @Test
    fun `the most recently picked directory wins`() {
        memory.remember(downloads)
        memory.remember(documents)

        assertThat(memory.recall()).isEqualTo(documents)
    }

    @Test
    fun `falls back to the auto-export directory when nothing was picked manually`() {
        prefs.edit().putString(DatabaseExportWorker.PREF_EXPORT_URI, documents.toString()).commit()

        assertThat(memory.recall()).isEqualTo(documents)
    }

    @Test
    fun `a manual pick takes precedence over the auto-export directory`() {
        prefs.edit().putString(DatabaseExportWorker.PREF_EXPORT_URI, documents.toString()).commit()
        memory.remember(downloads)

        assertThat(memory.recall()).isEqualTo(downloads)
    }

    @Test
    fun `remembering the manual directory leaves the auto-export target alone`() {
        prefs.edit().putString(DatabaseExportWorker.PREF_EXPORT_URI, documents.toString()).commit()

        memory.remember(downloads)

        // A one-off manual export must not silently retarget the scheduled export.
        assertThat(prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null))
            .isEqualTo(documents.toString())
    }

    @Test
    fun `forgetting an unusable directory clears it`() {
        memory.remember(downloads)

        memory.forget()

        assertThat(memory.recall()).isNull()
    }

    @Test
    fun `forgetting falls back to the auto-export directory rather than the cleared one`() {
        prefs.edit().putString(DatabaseExportWorker.PREF_EXPORT_URI, documents.toString()).commit()
        memory.remember(downloads)

        memory.forget()

        assertThat(memory.recall()).isEqualTo(documents)
    }
}
