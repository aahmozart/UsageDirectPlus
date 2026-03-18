package godau.fynn.usagedirectplus.persistence

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class ExportCompressionTest {

    @Test
    fun `gzip output has correct magic bytes`() {
        val data = "SQLite format 3".repeat(100).toByteArray()
        val compressed = gzipCompress(data)

        // GZIP magic number: 0x1f 0x8b
        assertThat(compressed[0]).isEqualTo(0x1f.toByte())
        assertThat(compressed[1]).isEqualTo(0x8b.toByte())
    }

    @Test
    fun `compressed data is smaller than original`() {
        // Simulate repetitive SQLite-like data
        val data = ByteArray(10_000) { (it % 256).toByte() }
        val compressed = gzipCompress(data)

        assertThat(compressed.size).isLessThan(data.size)
    }

    @Test
    fun `round-trip compression and decompression preserves data`() {
        val original = "SQLite format 3\u0000".toByteArray() + ByteArray(4096) { (it % 256).toByte() }
        val compressed = gzipCompress(original)
        val decompressed = gzipDecompress(compressed)

        assertThat(decompressed).isEqualTo(original)
    }

    private fun gzipCompress(data: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream()
        GZIPOutputStream(baos).use { gzos ->
            gzos.write(data)
            gzos.finish()
        }
        return baos.toByteArray()
    }

    private fun gzipDecompress(data: ByteArray): ByteArray {
        return GZIPInputStream(ByteArrayInputStream(data)).use { it.readBytes() }
    }
}
