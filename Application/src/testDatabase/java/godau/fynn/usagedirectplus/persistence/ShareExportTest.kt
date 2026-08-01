package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import godau.fynn.usagedirectplus.R
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareExportTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private val exportUri: Uri =
        Uri.parse("content://com.android.externalstorage.documents/document/primary%3ADownload%2Fexport.sqlite3")

    @Test
    fun `send intent carries the export as a stream`() {
        val intent = ShareExport.buildSendIntent(exportUri, "usageDirectPlus-2026-08-01_120000.sqlite3")

        assertThat(intent.action).isEqualTo(Intent.ACTION_SEND)
        assertThat(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)).isEqualTo(exportUri)
        assertThat(intent.getStringExtra(Intent.EXTRA_SUBJECT))
            .isEqualTo("usageDirectPlus-2026-08-01_120000.sqlite3")
        assertThat(intent.getStringExtra(Intent.EXTRA_TITLE))
            .isEqualTo("usageDirectPlus-2026-08-01_120000.sqlite3")
    }

    @Test
    fun `send intent grants read permission to the receiving app`() {
        val intent = ShareExport.buildSendIntent(exportUri, "export.sqlite3")

        assertThat(intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    @Test
    fun `send intent repeats the uri in clip data so the grant propagates`() {
        val intent = ShareExport.buildSendIntent(exportUri, "export.sqlite3")

        val clipData = intent.clipData
        assertThat(clipData).isNotNull()
        assertThat(clipData!!.itemCount).isEqualTo(1)
        assertThat(clipData.getItemAt(0).uri).isEqualTo(exportUri)
    }

    @Test
    fun `send intent advertises a generic binary type rather than the sqlite vendor type`() {
        // application/vnd.sqlite3 is non-standard and gets filtered out of the chooser by
        // common targets such as WhatsApp, so both variants are shared as octet-stream.
        val plain = ShareExport.buildSendIntent(exportUri, "export.sqlite3")
        val compressed = ShareExport.buildSendIntent(exportUri, "export.sqlite3.gz")

        assertThat(plain.type).isEqualTo("application/octet-stream")
        assertThat(compressed.type).isEqualTo("application/octet-stream")
    }

    @Test
    fun `chooser wraps the send intent and keeps the read grant`() {
        val chooser = ShareExport.buildChooser(context, exportUri, "export.sqlite3")

        assertThat(chooser.action).isEqualTo(Intent.ACTION_CHOOSER)
        assertThat(chooser.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .isEqualTo(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val wrapped = chooser.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertThat(wrapped).isNotNull()
        assertThat(wrapped!!.action).isEqualTo(Intent.ACTION_SEND)
        assertThat(wrapped.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)).isEqualTo(exportUri)
    }

    @Test
    fun `chooser is titled`() {
        val chooser = ShareExport.buildChooser(context, exportUri, "export.sqlite3")

        assertThat(chooser.getCharSequenceExtra(Intent.EXTRA_TITLE).toString())
            .isEqualTo(context.getString(R.string.export_share_chooser))
    }

    @Test
    fun `export result from a compressed export can be shared`() {
        val result = ExportResult(
            filename = "usageDirectPlus-2026-08-01_120000.sqlite3.gz",
            uri = exportUri,
            mimeType = "application/gzip"
        )

        val intent = ShareExport.buildSendIntent(result.uri, result.filename)

        assertThat(intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)).isEqualTo(result.uri)
        assertThat(intent.getStringExtra(Intent.EXTRA_SUBJECT)).isEqualTo(result.filename)
    }
}
