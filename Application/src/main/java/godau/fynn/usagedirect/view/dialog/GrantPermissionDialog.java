package godau.fynn.usagedirect.view.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import godau.fynn.usagedirect.R;

public class GrantPermissionDialog extends AlertDialog.Builder {

    public static final int REQUEST_CODE = 473; // GPD on a numpad

    public GrantPermissionDialog(final Activity context) {
        super(context);
        setTitle(R.string.explanation_access_appusage_title);
        setMessage(R.string.explanation_access_appusage_message);
        setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE);
            }
        });
        setNegativeButton(R.string.leave_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                context.finish();
            }
        });
        setCancelable(false);
    }
}
