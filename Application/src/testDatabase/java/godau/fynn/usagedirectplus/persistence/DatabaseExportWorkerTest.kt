package godau.fynn.usagedirectplus.persistence

import androidx.documentfile.provider.DocumentFile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
}
