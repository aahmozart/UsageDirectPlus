package godau.fynn.usagedirect.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.AppColor;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.thread.icon.IconThread;

public class ColorPickerDialog extends AlertDialog.Builder {

    private final ColorPicker colorPicker;

    private final AppColor appColor;

    public ColorPickerDialog(Context context, AppColor appColor) {
        super(context);
        this.appColor = appColor;

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_color_picker, null, false);

        setIcon(view.findViewById(R.id.app_icon));

        colorPicker = view.findViewById(R.id.color_picker);
        colorPicker.setColor(appColor.getColor());

        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                appColor.setColor(color);
            }
        });

        setView(view);


        setPositiveButton(R.string.confirm, (dialog, which) ->
                new Thread(() -> {
                    HistoryDatabase database = HistoryDatabase.get(context);
                    database.getAppColorDao().insert(appColor);
                    database.close();
                }).start()
        );

        setNeutralButton(R.string.cancel, null);

        setNegativeButton(R.string.uncolor, (dialog, which) ->
                new Thread(() -> {
                    HistoryDatabase database = HistoryDatabase.get(context);
                    database.getAppColorDao().delete(appColor);
                    database.close();
                }).start()
        );
    }

    private void setIcon(ImageView iconView) {
        if (IconThread.iconMap.containsKey(appColor.getApplicationId())) {
            iconView.setImageDrawable(IconThread.iconMap.get(appColor.getApplicationId()));
        } else {
            new IconThread(new String[]{appColor.getApplicationId()}, null, getContext()) {
                @Override
                protected void onIconLoaded(int position, String applicationId) {
                    iconView.setImageDrawable(IconThread.iconMap.get(applicationId));
                }
            };
        }
    }
}
