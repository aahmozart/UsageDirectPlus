package godau.fynn.usagedirect.view.adapter;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.combined.TimeAppColor;
import godau.fynn.usagedirect.thread.icon.IconThread;
import godau.fynn.usagedirect.view.dialog.ColorPickerDialog;

import java.util.Arrays;
import java.util.List;

public class ColorAdapter extends SimpleRecyclerViewAdapter<TimeAppColor, ColorAdapter.ViewHolder> {

    private final ItemTouchHelper touchHelper;

    public ColorAdapter(TimeAppColor[] timePerApp, ItemTouchHelper touchHelper) {
        content.addAll(Arrays.asList(timePerApp));
        this.touchHelper = touchHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                inflater.inflate(R.layout.row_color, parent, false),
                content, touchHelper
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, TimeAppColor item, int position) {

        String name = IconThread.nameMap.get(item.getApplicationId());
        holder.mPackageName.setText(
                name == null?
                        item.getApplicationId() : name
        );
        holder.mAppIcon.setImageDrawable(IconThread.iconMap.get(item.getApplicationId()));

        int hours = item.getTotalTimeUsed() / 1000 / 60 / 60;
        holder.mTimeUsed.setText(context.getResources().getQuantityString(
                R.plurals.time_used_total_hours,
                hours, hours
        ));
        holder.mTimeUsed.setVisibility(
                hours > 0 ?
                        View.VISIBLE : View.GONE
        );

        if (item.getAppColor() == null) {
            holder.mColorDisplay.setBackground(context.getDrawable(R.drawable.custom_light_square));
            holder.mHandle.setVisibility(View.INVISIBLE);
        } else {
            holder.mColorDisplay.setBackgroundColor(item.getAppColor().getColor());
            holder.mHandle.setVisibility(View.VISIBLE);
        }

        holder.item = item;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final LinearLayout mContentLayout;
        public final TextView mPackageName;
        public final TextView mTimeUsed;
        public final ImageView mAppIcon;
        public final View mColorDisplay;
        public final ImageView mHandle;

        public TimeAppColor item;

        public ViewHolder(View v, List<TimeAppColor> content, ItemTouchHelper touchHelper) {
            super(v);
            mContentLayout = v.findViewById(R.id.content);
            mPackageName = v.findViewById(R.id.textview_package_name);
            mTimeUsed = v.findViewById(R.id.textview_time_used);
            mAppIcon = v.findViewById(R.id.app_icon);
            mColorDisplay = v.findViewById(R.id.color_display);
            mHandle = v.findViewById(R.id.drag_handle);

            mHandle.setOnTouchListener((v1, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(ViewHolder.this);
                }
                return false;
            });

            mContentLayout.setOnClickListener(view -> new ColorPickerDialog(
                    itemView.getContext(),
                    item
            ) {
                @Override
                protected void onColorSet(TimeAppColor color) {
                    mColorDisplay.setBackgroundColor(color.getAppColor().getColor());
                    mHandle.setVisibility(View.VISIBLE);

                    // Move upwards in content
                    int position = moveToFirstUncolored(item);

                    // Written to database by parent class
                    if (position > 0) {
                        color.getAppColor().setPriority(
                                content.get(position - 1)
                                        .getAppColor()
                                        .getPriority()
                        );
                    }
                }

                @Override
                protected void onColorRemoved() {
                    mColorDisplay.setBackground(itemView.getContext().getDrawable(R.drawable.custom_light_square));
                    mHandle.setVisibility(View.INVISIBLE);

                    // Move downwards
                    moveToFirstUncolored(item);
                }

                /**
                 * @return New position of `item`
                 */
                private int moveToFirstUncolored(TimeAppColor item) {
                    int oldPosition = content.indexOf(item);
                    content.remove(item);

                    int firstUncoloredApp;
                    for (firstUncoloredApp = 0; firstUncoloredApp < content.size(); firstUncoloredApp++) {
                        if (content.get(firstUncoloredApp).getAppColor() == null) break;
                    }

                    content.add(firstUncoloredApp, item);
                    getBindingAdapter().notifyItemMoved(oldPosition, firstUncoloredApp);

                    return firstUncoloredApp;
                }
            }.show());
        }
    }
}
