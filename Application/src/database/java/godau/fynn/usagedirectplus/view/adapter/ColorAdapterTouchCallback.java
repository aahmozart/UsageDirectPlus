package godau.fynn.usagedirectplus.view.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class ColorAdapterTouchCallback extends ItemTouchHelper.SimpleCallback {

    public ColorAdapterTouchCallback() {
        super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {

        return ((ColorAdapter) recyclerView.getAdapter()).onMove(
                viewHolder.getBindingAdapterPosition(),
                target.getBindingAdapterPosition()
        );
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public int getDragDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {

        ColorAdapter.ViewHolder holder = (ColorAdapter.ViewHolder) viewHolder;

        if (holder.item.getAppColor() == null) {
            return makeMovementFlags(0, 0);
        } else {
            return super.getDragDirs(recyclerView, viewHolder);
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }
}
