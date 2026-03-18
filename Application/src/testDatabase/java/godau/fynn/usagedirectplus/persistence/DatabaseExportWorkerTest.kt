package godau.fynn.usagedirectplus.persistence

import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class DatabaseExportWorkerTest {

    @Test
    fun `timestamped mode creates file with timestamp and does not call findFile`() {
        val createdFile = mockk<DocumentFile>()
        val directory = mockk<DocumentFile> {
            every { createFile(any(), match { it.startsWith("usageDirectPlus-") && it.endsWith(".sqlite3") }) } returns createdFile
        }

        val result = DatabaseExportWorker.resolveOutputFile(directory, useFixedFilename = false, compress = false)

        assertThat(result).isEqualTo(createdFile)
        verify(exactly = 0) { directory.findFile(any()) }
        verify(exactly = 1) { directory.createFile("application/vnd.sqlite3", match { it.startsWith("usageDirectPlus-") }) }
    }

    @Test
    fun `fixed mode with no existing file creates new file`() {
        val createdFile = mockk<DocumentFile>()
        val directory = mockk<DocumentFile> {
            every { findFile("usageDirectPlus.sqlite3") } returns null
            every { createFile(any(), "usageDirectPlus.sqlite3") } returns createdFile
        }

        val result = DatabaseExportWorker.resolveOutputFile(directory, useFixedFilename = true, compress = false)

        assertThat(result).isEqualTo(createdFile)
        verify(exactly = 1) { directory.findFile("usageDirectPlus.sqlite3") }
        verify(exactly = 1) { directory.createFile("application/vnd.sqlite3", "usageDirectPlus.sqlite3") }
    }

    @Test
    fun `fixed mode with existing file returns existing file without creating`() {
        val existingFile = mockk<DocumentFile>()
        val directory = mockk<DocumentFile> {
            every { findFile("usageDirectPlus.sqlite3") } returns existingFile
        }

        val result = DatabaseExportWorker.resolveOutputFile(directory, useFixedFilename = true, compress = false)

        assertThat(result).isEqualTo(existingFile)
        verify(exactly = 1) { directory.findFile("usageDirectPlus.sqlite3") }
        verify(exactly = 0) { directory.createFile(any(), any()) }
    }

    @Test
    fun `timestamped compressed mode creates gz file with gzip mime type`() {
        val createdFile = mockk<DocumentFile>()
        val directory = mockk<DocumentFile> {
            every { createFile(any(), match { it.endsWith(".sqlite3.gz") }) } returns createdFile
        }

        val result = DatabaseExportWorker.resolveOutputFile(directory, useFixedFilename = false, compress = true)

        assertThat(result).isEqualTo(createdFile)
        verify(exactly = 0) { directory.findFile(any()) }
        verify(exactly = 1) { directory.createFile("application/gzip", match { it.startsWith("usageDirectPlus-") && it.endsWith(".sqlite3.gz") }) }
    }

    @Test
    fun `fixed compressed mode with no existing file creates gz file`() {
        val createdFile = mockk<DocumentFile>()
        val directory = mockk<DocumentFile> {
            every { findFile("usageDirectPlus.sqlite3.gz") } returns null
            every { createFile(any(), "usageDirectPlus.sqlite3.gz") } returns createdFile
        }

        val result = DatabaseExportWorker.resolveOutputFile(directory, useFixedFilename = true, compress = true)

        assertThat(result).isEqualTo(createdFile)
        verify(exactly = 1) { directory.findFile("usageDirectPlus.sqlite3.gz") }
        verify(exactly = 1) { directory.createFile("application/gzip", "usageDirectPlus.sqlite3.gz") }
    }

    @Test
    fun `fixed compressed mode with existing file returns existing file`() {
        val existingFile = mockk<DocumentFile>()
        val directory = mockk<DocumentFile> {
            every { findFile("usageDirectPlus.sqlite3.gz") } returns existingFile
        }

        val result = DatabaseExportWorker.resolveOutputFile(directory, useFixedFilename = true, compress = true)

        assertThat(result).isEqualTo(existingFile)
        verify(exactly = 1) { directory.findFile("usageDirectPlus.sqlite3.gz") }
        verify(exactly = 0) { directory.createFile(any(), any()) }
    }

    // --- removeOldExports tests ---

    @AfterEach
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    private fun mockDocumentFile(name: String?, uri: Uri, deleteResult: Boolean = true): DocumentFile {
        return mockk {
            every { this@mockk.name } returns name
            every { this@mockk.uri } returns uri
            every { delete() } returns deleteResult
        }
    }

    private fun mockUri(): Uri = mockk()

    @Test
    fun `removeOldExports deletes matching timestamped files but not current file`() {
        val currentUri = mockUri()
        val currentFile = mockDocumentFile("usageDirectPlus-2026-03-19_120000.sqlite3", currentUri)
        val oldFile1 = mockDocumentFile("usageDirectPlus-2026-03-18_120000.sqlite3", mockUri())
        val oldFile2 = mockDocumentFile("usageDirectPlus-2026-03-17_120000.sqlite3.gz", mockUri())
        val unrelatedFile = mockDocumentFile("notes.txt", mockUri())
        val directory = mockk<DocumentFile> {
            every { listFiles() } returns arrayOf(currentFile, oldFile1, oldFile2, unrelatedFile)
        }

        val deleted = DatabaseExportWorker.removeOldExports(directory, currentFile)

        assertThat(deleted).isEqualTo(2)
        verify(exactly = 0) { currentFile.delete() }
        verify(exactly = 1) { oldFile1.delete() }
        verify(exactly = 1) { oldFile2.delete() }
        verify(exactly = 0) { unrelatedFile.delete() }
    }

    @Test
    fun `removeOldExports does not delete fixed-filename files`() {
        val currentUri = mockUri()
        val currentFile = mockDocumentFile("usageDirectPlus-2026-03-19_120000.sqlite3", currentUri)
        val fixedFile = mockDocumentFile("usageDirectPlus.sqlite3", mockUri())
        val fixedGzFile = mockDocumentFile("usageDirectPlus.sqlite3.gz", mockUri())
        val directory = mockk<DocumentFile> {
            every { listFiles() } returns arrayOf(currentFile, fixedFile, fixedGzFile)
        }

        val deleted = DatabaseExportWorker.removeOldExports(directory, currentFile)

        assertThat(deleted).isEqualTo(0)
        verify(exactly = 0) { fixedFile.delete() }
        verify(exactly = 0) { fixedGzFile.delete() }
    }

    @Test
    fun `removeOldExports counts only successful deletions`() {
        mockkStatic(Log::class)
        every { Log.w(any<String>(), any<String>()) } returns 0

        val currentUri = mockUri()
        val currentFile = mockDocumentFile("usageDirectPlus-2026-03-19_120000.sqlite3", currentUri)
        val deletableFile = mockDocumentFile("usageDirectPlus-2026-03-18_120000.sqlite3", mockUri())
        val failFile = mockDocumentFile("usageDirectPlus-2026-03-17_120000.sqlite3", mockUri(), deleteResult = false)
        val directory = mockk<DocumentFile> {
            every { listFiles() } returns arrayOf(currentFile, deletableFile, failFile)
        }

        val deleted = DatabaseExportWorker.removeOldExports(directory, currentFile)

        assertThat(deleted).isEqualTo(1)
        verify(exactly = 1) { deletableFile.delete() }
        verify(exactly = 1) { failFile.delete() }
    }

    @Test
    fun `removeOldExports handles empty directory`() {
        val currentFile = mockDocumentFile("usageDirectPlus-2026-03-19_120000.sqlite3", mockUri())
        val directory = mockk<DocumentFile> {
            every { listFiles() } returns emptyArray()
        }

        val deleted = DatabaseExportWorker.removeOldExports(directory, currentFile)

        assertThat(deleted).isEqualTo(0)
    }

    @Test
    fun `removeOldExports skips files with null name`() {
        val currentUri = mockUri()
        val currentFile = mockDocumentFile("usageDirectPlus-2026-03-19_120000.sqlite3", currentUri)
        val nullNameFile = mockDocumentFile(null, mockUri())
        val directory = mockk<DocumentFile> {
            every { listFiles() } returns arrayOf(currentFile, nullNameFile)
        }

        val deleted = DatabaseExportWorker.removeOldExports(directory, currentFile)

        assertThat(deleted).isEqualTo(0)
        verify(exactly = 0) { nullNameFile.delete() }
    }
}
