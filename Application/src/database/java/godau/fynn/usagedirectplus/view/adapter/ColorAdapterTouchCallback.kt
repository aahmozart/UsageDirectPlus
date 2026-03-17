package godau.fynn.usagedirectplus.view.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ColorAdapterTouchCallback : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return (recyclerView.adapter as ColorAdapter).onMove(
            viewHolder.bindingAdapterPosition,
            target.bindingAdapterPosition
        )
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun getDragDirs(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val holder = viewHolder as ColorAdapter.ViewHolder

        return if (holder.item.appColor == null) {
            makeMovementFlags(0, 0)
        } else {
            super.getDragDirs(recyclerView, viewHolder)
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}
