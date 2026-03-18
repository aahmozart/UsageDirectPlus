package godau.fynn.usagedirectplus.view.dialog

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class ExportDialog(
    private val activity: SourceAppUsageStatisticsActivity,
    private val directoryPickerLauncher: ActivityResultLauncher<Uri?>
) : MaterialAlertDialogBuilder(activity) {

    private val binding = DialogExportBinding.inflate(LayoutInflater.from(context))
    private var selectedDirectoryUri: Uri? = null

    init {
        setTitle(R.string.export_title)
        setView(binding.root)

        binding.exportDirectoryButton.setOnClickListener {
            directoryPickerLauncher.launch(null)
        }

        setPositiveButton(R.string.go, null)
        setNegativeButton(R.string.cancel, null)
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
                    val filename = Export.exportToDirectory(activity, directory, compress)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity, activity.getString(R.string.export_okay), Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
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

    fun onDirectorySelected(uri: Uri) {
        selectedDirectoryUri = uri
        val directory = DocumentFile.fromTreeUri(activity, uri)
        binding.exportDirectoryLabel.text = directory?.name ?: uri.lastPathSegment
    }
}
