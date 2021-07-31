package godau.fynn.usagedirect.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.palette.graphics.Palette;
import com.madrapps.pikolo.ColorPicker;
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.AppColor;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.combined.TimeAppColor;
import godau.fynn.usagedirect.thread.icon.IconThread;

public abstract class ColorPickerDialog extends AlertDialog.Builder {

    public ColorPickerDialog(Context context, TimeAppColor timeAppColor) {
        super(context);

        AppColor appColor;
        if (timeAppColor.getAppColor() == null) {
            appColor = new AppColor(
                    timeAppColor.getApplicationId(),
                    extractDefaultColor(drawableToBitmap(IconThread.iconMap.get(timeAppColor.getApplicationId())), context),
                    0
            );
        } else {
            // Don't edit existing object which is also used for rendering to support canceling
            appColor = new AppColor(timeAppColor.getAppColor());
        }

        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_color_picker, null, false);

        ((ImageView) view.findViewById(R.id.app_icon))
                .setImageDrawable(IconThread.iconMap.get(appColor.getApplicationId()));

        ColorPicker colorPicker = view.findViewById(R.id.color_picker);
        colorPicker.setColor(appColor.getColor());

        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                appColor.setColor(color);
            }
        });

        setView(view);


        setPositiveButton(R.string.confirm, (dialog, which) -> {
            timeAppColor.setAppColor(appColor);
            onColorSet(timeAppColor);
        });

        setNeutralButton(R.string.cancel, null);

        setNegativeButton(R.string.uncolor, (dialog, which) -> {
            timeAppColor.setAppColor(null);
            onColorRemoved();
        });
    }

    protected abstract void onColorSet(TimeAppColor color);

    protected abstract void onColorRemoved();

    private static @ColorInt
    int extractDefaultColor(Bitmap bitmap, Context context) {

        @ColorInt int color = Palette.from(bitmap)
                .generate()
                .getDominantColor(0xffffffff);

        if (color == 0xffffffff) {
            // Choose different default color at random
            int random = (int) (Math.random() * 4);
            @ColorRes int res;
            switch (random) {
                case 0:
                default:
                    res = R.color.notice_blue;
                    break;
                case 1:
                    res = R.color.notice_gray;
                    break;
                case 2:
                    res = R.color.notice_green;
                    break;
                case 3:
                    res = R.color.notice_red;
                    break;
            }

            return context.getResources().getColor(res);
        } else return color;
    }

    /**
     * @see <a href="https://android.googlesource.com/platform/frameworks/support/+/android-room-release/core/ktx/src/main/java/androidx/core/graphics/drawable/Drawable.kt#42">
     * Androidx implementation</a>
     */
    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888
        );
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(new Canvas(bitmap));

        return bitmap;
    }
}
