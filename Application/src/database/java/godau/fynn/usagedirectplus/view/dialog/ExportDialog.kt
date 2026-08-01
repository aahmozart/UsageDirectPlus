package godau.fynn.usagedirectplus.view.dialog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.activity.SourceAppUsageStatisticsActivity
import godau.fynn.usagedirectplus.databinding.DialogExportBinding
import godau.fynn.usagedirectplus.persistence.Export
import godau.fynn.usagedirectplus.persistence.ExportDirectoryMemory
import godau.fynn.usagedirectplus.persistence.ExportResult
import godau.fynn.usagedirectplus.persistence.ShareExport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ExportDialog(
    private val activity: SourceAppUsageStatisticsActivity,
    private val directoryPickerLauncher: ActivityResultLauncher<Uri?>
) : MaterialAlertDialogBuilder(activity) {

    private val binding = DialogExportBinding.inflate(LayoutInflater.from(context))
    private val directoryMemory = ExportDirectoryMemory.of(activity)
    private var selectedDirectoryUri: Uri? = null

    init {
        setTitle(R.string.export_title)
        setView(binding.root)

        restoreRememberedDirectory()

        binding.exportDirectoryButton.setOnClickListener {
            directoryPickerLauncher.launch(selectedDirectoryUri)
        }

        setPositiveButton(R.string.go, null)
        setNegativeButton(R.string.cancel, null)
    }

    /**
     * Preselects the directory of the previous export. A remembered directory can outlive its
     * permission grant or its storage volume, so an unusable one is dropped rather than offered.
     */
    private fun restoreRememberedDirectory() {
        val uri = directoryMemory.recall() ?: return
        val directory = DocumentFile.fromTreeUri(activity, uri)

        if (directory != null && directory.exists() && directory.canWrite()) {
            showSelectedDirectory(uri, directory)
        } else {
            directoryMemory.forget()
        }
    }

    override fun show(): androidx.appcompat.app.AlertDialog {
        val dialog = super.show()

        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val uri = selectedDirectoryUri
            if (uri == null) {
                Toast.makeText(activity, R.string.export_settings_pick_directory_first, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val directory = DocumentFile.fromTreeUri(activity, uri)
            if (directory == null || !directory.exists() || !directory.canWrite()) {
                Toast.makeText(activity, R.string.export_io_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val compress = binding.exportCompressSwitch.isChecked

            activity.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val result = Export.exportToDirectory(activity, directory, compress)
                    withContext(Dispatchers.Main) {
                        dialog.dismiss()
                        showShareDialog(result)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, R.string.export_io_error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return dialog
    }

    /**
     * Offers to hand the freshly written file to another app. The export lives in a directory the
     * user picked, so its document URI can go straight into the share sheet.
     */
    private fun showShareDialog(result: ExportResult) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.export_okay)
            .setMessage(activity.getString(R.string.export_share_message, result.filename))
            .setPositiveButton(R.string.export_share) { _, _ -> shareExport(result) }
            .setNegativeButton(R.string.export_share_done, null)
            .show()
    }

    private fun shareExport(result: ExportResult) {
        try {
            activity.startActivity(ShareExport.buildChooser(activity, result.uri, result.filename))
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
            Toast.makeText(activity, R.string.export_share_no_app, Toast.LENGTH_SHORT).show()
        }
    }

    fun onDirectorySelected(uri: Uri) {
        directoryMemory.remember(uri)
        showSelectedDirectory(uri, DocumentFile.fromTreeUri(activity, uri))
    }

    private fun showSelectedDirectory(uri: Uri, directory: DocumentFile?) {
        selectedDirectoryUri = uri
        binding.exportDirectoryLabel.text = directory?.name ?: uri.lastPathSegment
    }
}
