package godau.fynn.usagedirectplus.persistence

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import godau.fynn.usagedirectplus.R

/**
 * Builds the share sheet for a finished export.
 *
 * Exports are written into a directory the user picked through the storage access framework, so
 * the resulting document URI can be handed to another app directly — no FileProvider and no second
 * copy of the database are needed.
 */
object ShareExport {

    /**
     * Exports are opaque binaries. The vendor type the file is created with
     * ([Export] uses `application/vnd.sqlite3`) is non-standard and gets filtered out of the
     * chooser by common targets, so the share intent advertises the generic binary type instead.
     */
    const val SHARE_MIME_TYPE = "application/octet-stream"

    @JvmStatic
    fun buildSendIntent(uri: Uri, filename: String): Intent =
        Intent(Intent.ACTION_SEND).apply {
            type = SHARE_MIME_TYPE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, filename)
            putExtra(Intent.EXTRA_TITLE, filename)
            // Some receivers read the URI from the clip data rather than the extra, and it is what
            // carries the permission grant across on several OEM share implementations.
            clipData = ClipData.newRawUri(filename, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

    @JvmStatic
    fun buildChooser(context: Context, uri: Uri, filename: String): Intent =
        Intent.createChooser(
            buildSendIntent(uri, filename),
            context.getString(R.string.export_share_chooser)
        ).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
