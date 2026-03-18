package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.documentfile.provider.DocumentFile
import godau.fynn.usagedirectplus.R
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.GZIPOutputStream

object Export {

    private const val MIME_TYPE = "application/vnd.sqlite3"
    private const val MIME_TYPE_GZ = "application/gzip"

    @JvmStatic
    @Throws(IOException::class)
    fun exportHistoryDatabase(saveToData: Intent, context: Context) {
        val uri = saveToData.data

        val parcelFileDescriptor = context.contentResolver
            .openFileDescriptor(uri!!, "w")
        val outputStream = FileOutputStream(parcelFileDescriptor!!.fileDescriptor)

        val databaseFile = context.getDatabasePath(HistoryDatabase.DATABASE_NAME)
        val inputStream = FileInputStream(databaseFile)

        val buffer = ByteArray(1024)
        var len = inputStream.read(buffer)
        while (len != -1) {
            outputStream.write(buffer, 0, len)
            len = inputStream.read(buffer)
        }

        inputStream.close()
        outputStream.close()

        Toast.makeText(context, R.string.export_okay, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    @Throws(IOException::class)
    fun exportToDirectory(context: Context, directory: DocumentFile, compress: Boolean): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.US).format(Date())
        val ext = if (compress) "sqlite3.gz" else "sqlite3"
        val filename = "usageDirectPlus-$timestamp.$ext"
        val mimeType = if (compress) MIME_TYPE_GZ else MIME_TYPE

        val outputFile = directory.createFile(mimeType, filename)
            ?: throw IOException("Could not create output file")

        val databaseFile = context.getDatabasePath(HistoryDatabase.DATABASE_NAME)

        FileInputStream(databaseFile).use { inputStream ->
            context.contentResolver.openOutputStream(outputFile.uri, "w").use { rawOutputStream ->
                if (rawOutputStream == null) {
                    throw IOException("Could not open output stream")
                }

                val outputStream: OutputStream = if (compress) GZIPOutputStream(rawOutputStream) else rawOutputStream

                val buffer = ByteArray(4096)
                var len: Int
                while (inputStream.read(buffer).also { len = it } != -1) {
                    outputStream.write(buffer, 0, len)
                }
                outputStream.flush()
                if (outputStream is GZIPOutputStream) {
                    outputStream.finish()
                }
            }
        }

        return filename
    }
}
