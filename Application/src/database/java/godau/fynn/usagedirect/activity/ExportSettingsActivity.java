package godau.fynn.usagedirect.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.DatabaseExportWorker;
import godau.fynn.usagedirect.persistence.HistoryDatabase;

public class ExportSettingsActivity extends Activity {

    private static final int REQUEST_PICK_DIRECTORY = 42001;

    private SharedPreferences prefs;
    private Switch toggleSwitch;
    private Button directoryButton;
    private TextView directoryLabel;
    private Spinner intervalSpinner;
    private Button exportNowButton;

    // Hours for each spinner position
    private static final long[] INTERVAL_VALUES = {6, 12, 24, 48, 168};
    private static final String[] INTERVAL_LABELS = {"6 hours", "12 hours", "24 hours", "48 hours", "7 days"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_settings);

        prefs = getSharedPreferences(HistoryDatabase.DATABASE_NAME, MODE_PRIVATE);

        toggleSwitch = findViewById(R.id.export_toggle);
        directoryButton = findViewById(R.id.export_directory_button);
        directoryLabel = findViewById(R.id.export_directory_label);
        intervalSpinner = findViewById(R.id.export_interval_spinner);
        exportNowButton = findViewById(R.id.export_now_button);

        // Setup interval spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, INTERVAL_LABELS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(adapter);

        // Restore saved interval
        long savedInterval = prefs.getLong(DatabaseExportWorker.PREF_EXPORT_INTERVAL_HOURS, 24);
        for (int i = 0; i < INTERVAL_VALUES.length; i++) {
            if (INTERVAL_VALUES[i] == savedInterval) {
                intervalSpinner.setSelection(i);
                break;
            }
        }

        // Restore saved directory display
        updateDirectoryLabel();

        // Restore toggle state
        boolean enabled = prefs.getBoolean(DatabaseExportWorker.PREF_EXPORT_ENABLED, false);
        toggleSwitch.setChecked(enabled);
        setControlsEnabled(enabled);

        toggleSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean(DatabaseExportWorker.PREF_EXPORT_ENABLED, isChecked).apply();
            setControlsEnabled(isChecked);

            if (isChecked) {
                String uri = prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null);
                if (uri != null) {
                    long interval = INTERVAL_VALUES[intervalSpinner.getSelectedItemPosition()];
                    DatabaseExportWorker.schedule(this, interval);
                } else {
                    Toast.makeText(this, R.string.export_settings_pick_directory_first, Toast.LENGTH_SHORT).show();
                    toggleSwitch.setChecked(false);
                }
            } else {
                DatabaseExportWorker.cancel(this);
            }
        });

        directoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_PICK_DIRECTORY);
        });

        intervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long interval = INTERVAL_VALUES[position];
                prefs.edit().putLong(DatabaseExportWorker.PREF_EXPORT_INTERVAL_HOURS, interval).apply();

                if (toggleSwitch.isChecked()) {
                    DatabaseExportWorker.schedule(ExportSettingsActivity.this, interval);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        exportNowButton.setOnClickListener(v -> {
            String uri = prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null);
            if (uri == null) {
                Toast.makeText(this, R.string.export_settings_pick_directory_first, Toast.LENGTH_SHORT).show();
                return;
            }

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DatabaseExportWorker.class).build();
            WorkManager.getInstance(this).enqueue(request);
            Toast.makeText(this, R.string.export_settings_export_queued, Toast.LENGTH_SHORT).show();
        });
    }

    private void setControlsEnabled(boolean enabled) {
        directoryButton.setEnabled(enabled);
        intervalSpinner.setEnabled(enabled);
        exportNowButton.setEnabled(enabled);
    }

    private void updateDirectoryLabel() {
        String uriString = prefs.getString(DatabaseExportWorker.PREF_EXPORT_URI, null);
        if (uriString != null) {
            Uri uri = Uri.parse(uriString);
            DocumentFile dir = DocumentFile.fromTreeUri(this, uri);
            if (dir != null && dir.getName() != null) {
                directoryLabel.setText(dir.getName());
            } else {
                directoryLabel.setText(uri.getLastPathSegment());
            }
        } else {
            directoryLabel.setText(R.string.export_settings_no_directory);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PICK_DIRECTORY && resultCode == RESULT_OK && data != null) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                // Persist permission across reboots
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                prefs.edit().putString(DatabaseExportWorker.PREF_EXPORT_URI, treeUri.toString()).apply();
                updateDirectoryLabel();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
