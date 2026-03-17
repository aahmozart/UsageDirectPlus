package godau.fynn.usagedirectplus.persistence

import android.content.Context
import android.content.Intent
import android.widget.Toast
import godau.fynn.usagedirectplus.R
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object Export {

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
}
