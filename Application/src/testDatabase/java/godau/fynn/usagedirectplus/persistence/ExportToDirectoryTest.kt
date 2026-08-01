package godau.fynn.usagedirectplus.persistence

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPInputStream

class ExportToDirectoryTest {

    private lateinit var tempDbFile: File
    private val dbContent = "SQLite format 3\u0000".toByteArray() + ByteArray(1024) { (it % 256).toByte() }

    @BeforeEach
    fun setUp() {
        tempDbFile = File.createTempFile("test-db", ".sqlite3")
        tempDbFile.writeBytes(dbContent)
    }

    @AfterEach
    fun tearDown() {
        tempDbFile.delete()
        try { unmockkStatic(DocumentFile::class) } catch (_: Exception) {}
    }

    @Test
    fun `uncompressed export copies bytes correctly`() {
        val outputStream = ByteArrayOutputStream()
        val outputFileUri = mockk<Uri>()
        val outputFile = mockk<DocumentFile> {
            every { uri } returns outputFileUri
        }
        val directory = mockk<DocumentFile> {
            every { createFile("application/vnd.sqlite3", match { it.endsWith(".sqlite3") && !it.endsWith(".gz") }) } returns outputFile
        }
        val contentResolver = mockk<ContentResolver> {
            every { openOutputStream(outputFileUri, "w") } returns outputStream
        }
        val context = mockk<Context> {
            every { getDatabasePath(HistoryDatabase.DATABASE_NAME) } returns tempDbFile
            every { getContentResolver() } returns contentResolver
        }

        val result = Export.exportToDirectory(context, directory, compress = false)

        assertThat(result.filename).endsWith(".sqlite3")
        assertThat(result.filename).doesNotContain(".gz")
        assertThat(result.uri).isEqualTo(outputFileUri)
        assertThat(result.mimeType).isEqualTo("application/vnd.sqlite3")
        assertThat(outputStream.toByteArray()).isEqualTo(dbContent)
    }

    @Test
    fun `compressed export produces valid GZIP output`() {
        val outputStream = ByteArrayOutputStream()
        val outputFileUri = mockk<Uri>()
        val outputFile = mockk<DocumentFile> {
            every { uri } returns outputFileUri
        }
        val directory = mockk<DocumentFile> {
            every { createFile("application/gzip", match { it.endsWith(".sqlite3.gz") }) } returns outputFile
        }
        val contentResolver = mockk<ContentResolver> {
            every { openOutputStream(outputFileUri, "w") } returns outputStream
        }
        val context = mockk<Context> {
            every { getDatabasePath(HistoryDatabase.DATABASE_NAME) } returns tempDbFile
            every { getContentResolver() } returns contentResolver
        }

        val result = Export.exportToDirectory(context, directory, compress = true)

        assertThat(result.filename).endsWith(".sqlite3.gz")
        assertThat(result.uri).isEqualTo(outputFileUri)
        assertThat(result.mimeType).isEqualTo("application/gzip")
        val compressed = outputStream.toByteArray()
        // GZIP magic bytes
        assertThat(compressed[0]).isEqualTo(0x1f.toByte())
        assertThat(compressed[1]).isEqualTo(0x8b.toByte())
        // Decompress and verify round-trip
        val decompressed = GZIPInputStream(compressed.inputStream()).use { it.readBytes() }
        assertThat(decompressed).isEqualTo(dbContent)
    }

    @Test
    fun `correct file extension for compressed vs uncompressed`() {
        val outputStream = ByteArrayOutputStream()
        val outputFileUri = mockk<Uri>()
        val outputFile = mockk<DocumentFile> {
            every { uri } returns outputFileUri
        }
        val contentResolver = mockk<ContentResolver> {
            every { openOutputStream(outputFileUri, "w") } returns outputStream
        }
        val context = mockk<Context> {
            every { getDatabasePath(HistoryDatabase.DATABASE_NAME) } returns tempDbFile
            every { getContentResolver() } returns contentResolver
        }

        val directoryUncompressed = mockk<DocumentFile> {
            every { createFile(any(), any()) } returns outputFile
        }
        val filenameUncompressed = Export.exportToDirectory(context, directoryUncompressed, compress = false).filename
        assertThat(filenameUncompressed).endsWith(".sqlite3")
        assertThat(filenameUncompressed).doesNotContain(".gz")

        val directoryCompressed = mockk<DocumentFile> {
            every { createFile(any(), any()) } returns outputFile
        }
        val filenameCompressed = Export.exportToDirectory(context, directoryCompressed, compress = true).filename
        assertThat(filenameCompressed).endsWith(".sqlite3.gz")
    }

    @Test
    fun `timestamped filename format is correct`() {
        val outputStream = ByteArrayOutputStream()
        val outputFileUri = mockk<Uri>()
        val outputFile = mockk<DocumentFile> {
            every { uri } returns outputFileUri
        }
        val directory = mockk<DocumentFile> {
            every { createFile(any(), any()) } returns outputFile
        }
        val contentResolver = mockk<ContentResolver> {
            every { openOutputStream(outputFileUri, "w") } returns outputStream
        }
        val context = mockk<Context> {
            every { getDatabasePath(HistoryDatabase.DATABASE_NAME) } returns tempDbFile
            every { getContentResolver() } returns contentResolver
        }

        val filename = Export.exportToDirectory(context, directory, compress = false).filename

        // Format: usageDirectPlus-yyyy-MM-dd_HHmmss.sqlite3
        assertThat(filename).matches("usageDirectPlus-\\d{4}-\\d{2}-\\d{2}_\\d{6}\\.sqlite3")
    }
}
