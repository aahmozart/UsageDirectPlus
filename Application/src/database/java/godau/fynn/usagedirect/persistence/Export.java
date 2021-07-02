package godau.fynn.usagedirect.persistence;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;
import godau.fynn.usagedirect.R;

import java.io.*;

public class Export {

    private Export() {}

    public static void exportHistoryDatabase(Intent saveToData, Context context) throws IOException {

        Uri uri = saveToData.getData();

        ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().
                openFileDescriptor(uri, "w");
        FileOutputStream outputStream =
                new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

        File databaseFile = context.getDatabasePath(HistoryDatabase.DATABASE_NAME);
        FileInputStream inputStream = new FileInputStream(databaseFile);

        byte[] buffer = new byte[1024];
        int len = inputStream.read(buffer);
        while (len != -1) {
            outputStream.write(buffer, 0, len);
            len = inputStream.read(buffer);
        }

        inputStream.close();
        outputStream.close();

        Toast.makeText(context, R.string.export_okay, Toast.LENGTH_SHORT).show();
    }
}
