package godau.fynn.usagedirectplus.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.ActivityExportSettingsBinding
import godau.fynn.usagedirectplus.persistence.DatabaseExportWorker
import godau.fynn.usagedirectplus.persistence.HistoryDatabase

class ExportSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityExportSettingsBinding
    private lateinit var prefs: android.content.SharedPreferences

    companion object {
        // Hours for each spinner position
        private val INTERVAL_VALUES = longArrayOf(6, 12, 24, 48, 168)
        private val INTERVAL_LABELS = arrayOf("6 hours", "12 hours", "24 hours", "48 hours", "7 days")
    }

    private val pickDirectoryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val treeUri = result.data!!.data
            if (treeUri != null) {
                // Persist permission across reboots
                contentResolver.takePersistableUriPermission(
                    treeUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                prefs.edit().putString(DatabaseExportWorker.PREF_EXPORT_URI, treeUri.toString()).apply()
                updateDirectoryLabel()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExportSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(HistoryDatabase.DATABASE_NAME, MODE_PRIVATE)

        // Setup interval spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, INTERVAL_LABELS)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.exportIntervalSpinner.adapter = adapter

        // Restore saved interval
        val savedInterval = prefs.getLong(DatabaseExportWorker.PREF_EXPORT_INTERVAL_HOURS, 24)
        for (i in INTERVAL_VALUES.indices) {
            if (INTERVAL_VALUES[i] == savedInterval) {
                binding.exportIntervalSpinner.setSelection(i)
                break
            }
        }

        // Restore saved directory display
        updateDirectoryLabel()

        // Restore fixed filename toggle state
        val fixedFilenameChecked = prefs.getBoolean(DatabaseExportWorker.PREF_EXPORT_FIXED_FILENAME, false)
        binding.exportFixedFilenameSwitch.isChecked = fixedFilenameChecked
        binding.exportRemoveOldSwitch.visibility = if (fixedFilenameChecked) View.GONE else View.VISIBLE
        binding.exportFixedFilenameSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(DatabaseExportWorker.PREF_EXPORT_FIXED_FILENAME, isChecked).apply()
            binding.exportRemoveOldSwitch.visibility = if (isChecked) View.GONE else View.VISIBLE
        }

        // Restore remove old exports toggle state
        binding.exportRemoveOldSwitch.isChecked =
            prefs.getBoolean(DatabaseExportWorker.PREF_EXPORT_REMOVE_OLD, false)
        binding.exportRemoveOldSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(DatabaseExportWorker.PREF_EXPORT_REMOVE_OLD, isChecked).apply()
        }

        // Restore compress toggle state
        binding.exportCompressSwitch.isChecked =
            prefs.getBoolean(DatabaseExportWorker.PREF_EXPORT_COMPRESS, false)
        binding.exportCompressSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(DatabaseExportWorker.PREF_EXPORT_COMPRESS, isChecked).apply()
        }

        // Restore toggle state
        val enabled = prefs.getBoolean(DatabaseExportWorker.PREF_EXPORT_ENABLED, false)
        binding.exportToggle.isChecked = enabled
        setControlsEnabled(enabled)

        binding.exportToggle.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(DatabaseExportWorker.PREF_EXPORT_ENABLED, isChecked).apply()
            setControlsEnabled(isChecked)

            if (isChecked) {
                val uri = prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null)
                if (uri != null) {
                    val interval = INTERVAL_VALUES[binding.exportIntervalSpinner.selectedItemPosition]
                    DatabaseExportWorker.schedule(this, interval)
                } else {
                    Toast.makeText(this, R.string.export_settings_pick_directory_first, Toast.LENGTH_SHORT).show()
                    binding.exportToggle.isChecked = false
                }
            } else {
                DatabaseExportWorker.cancel(this)
            }
        }

        binding.exportDirectoryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
            pickDirectoryLauncher.launch(intent)
        }

        binding.exportIntervalSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val interval = INTERVAL_VALUES[position]
                prefs.edit().putLong(DatabaseExportWorker.PREF_EXPORT_INTERVAL_HOURS, interval).apply()

                if (binding.exportToggle.isChecked) {
                    DatabaseExportWorker.schedule(this@ExportSettingsActivity, interval)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.exportNowButton.setOnClickListener {
            val uri = prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null)
            if (uri == null) {
                Toast.makeText(this, R.string.export_settings_pick_directory_first, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = OneTimeWorkRequest.Builder(DatabaseExportWorker::class.java).build()
            WorkManager.getInstance(this).enqueue(request)
            Toast.makeText(this, R.string.export_settings_export_queued, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setControlsEnabled(enabled: Boolean) {
        binding.exportDirectoryButton.isEnabled = enabled
        binding.exportIntervalSpinner.isEnabled = enabled
        binding.exportFixedFilenameSwitch.isEnabled = enabled
        binding.exportRemoveOldSwitch.isEnabled = enabled
        binding.exportCompressSwitch.isEnabled = enabled
        binding.exportNowButton.isEnabled = enabled
    }

    private fun updateDirectoryLabel() {
        val uriString = prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null)
        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val dir = DocumentFile.fromTreeUri(this, uri)
            if (dir != null && dir.name != null) {
                binding.exportDirectoryLabel.text = dir.name
            } else {
                binding.exportDirectoryLabel.text = uri.lastPathSegment
            }
        } else {
            binding.exportDirectoryLabel.setText(R.string.export_settings_no_directory)
        }
    }
}
