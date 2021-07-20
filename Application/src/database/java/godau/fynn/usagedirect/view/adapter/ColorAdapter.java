package godau.fynn.usagedirect.view.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.AppColor;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.thread.icon.IconThread;
import godau.fynn.usagedirect.view.dialog.ColorPickerDialog;

import java.util.Map;

public class ColorAdapter extends SimpleRecyclerViewAdapter<String, ColorAdapter.ViewHolder> {

    private final Map<String, Long> timePerApp;

    public ColorAdapter(Map<String, Long> timePerApp) {
        this.timePerApp = timePerApp;
        content.addAll(timePerApp.keySet());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row_color, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, String item, int position) {
        holder.mPackageName.setText(item);

        String name = IconThread.nameMap.get(item);
        holder.mPackageName.setText(
                name == null?
                        item : name
        );
        holder.mAppIcon.setImageDrawable(IconThread.iconMap.get(item));

        int hours = (int) (timePerApp.get(item) / 1000 / 60 / 60);
        holder.mTimeUsed.setText(context.getResources().getQuantityString(
                R.plurals.time_used_total_hours,
                hours, hours
        ));
        holder.mTimeUsed.setVisibility(
                hours > 0 ?
                        View.VISIBLE : View.GONE
        );

        holder.mColorDisplay.setTag(item);

        holder.mColorDisplay.setBackground(context.getDrawable(R.drawable.custom_light_square));
        holder.currentColor = null;

        new Thread(() -> {
            HistoryDatabase database = HistoryDatabase.get(context);

            AppColor color = database.getAppColorDao().getAppColor(item);

            database.close();

            new Handler(Looper.getMainLooper()).post(() -> {
                if (holder.mColorDisplay.getTag() == item) {
                    if (color != null) {
                        holder.mColorDisplay.setBackgroundColor(color.getColor());
                        holder.currentColor = color;
                    }
                } // else we are too late
            });
        }).start();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView mPackageName;
        public final TextView mTimeUsed;
        public final ImageView mAppIcon;
        public final View mColorDisplay;
        public AppColor currentColor;

        public ViewHolder(View v) {
            super(v);
            mPackageName = v.findViewById(R.id.textview_package_name);
            mTimeUsed = v.findViewById(R.id.textview_time_used);
            mAppIcon = v.findViewById(R.id.app_icon);
            mColorDisplay = v.findViewById(R.id.color_display);

            mColorDisplay.setOnClickListener(view -> {

                new ColorPickerDialog(
                        view.getContext(),
                        currentColor == null ?
                                new AppColor(
                                        (String) mColorDisplay.getTag(),
                                        extractDefaultColor(drawableToBitmap(mAppIcon.getDrawable()), view.getContext()),
                                        0
                                ) : currentColor
                ).show();

            });
        }
    }

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
