package godau.fynn.usagedirectplus.view.adapter

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.RowColorBinding
import godau.fynn.usagedirectplus.persistence.AppColor
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor
import godau.fynn.usagedirectplus.thread.icon.IconThread
import godau.fynn.usagedirectplus.view.dialog.ColorPickerDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Collections

class ColorAdapter(
    timePerApp: Array<TimeAppColor>,
    private val touchHelper: ItemTouchHelper
) : SimpleRecyclerViewAdapter<TimeAppColor, ColorAdapter.ViewHolder>() {

    private lateinit var historyDatabase: HistoryDatabase

    init {
        content.addAll(timePerApp)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        historyDatabase = HistoryDatabase.get(context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, content, touchHelper)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: TimeAppColor, position: Int) {
        val name = IconThread.nameMap[item.applicationId]
        holder.mPackageName.text = name ?: item.applicationId
        holder.mAppIcon.setImageDrawable(IconThread.iconMap[item.applicationId])

        val hours = item.totalTimeUsed / 1000 / 60 / 60
        holder.mTimeUsed.text = context.resources.getQuantityString(
            R.plurals.time_used_total_hours,
            hours, hours
        )
        holder.mTimeUsed.visibility = if (hours > 0) View.VISIBLE else View.GONE

        if (item.appColor == null) {
            holder.mColorDisplay.background = context.getDrawable(R.drawable.custom_light_square)
            holder.mHandle.visibility = View.INVISIBLE
        } else {
            holder.mColorDisplay.setBackgroundColor(item.appColor!!.color)
            holder.mHandle.visibility = View.VISIBLE
        }

        holder.item = item
    }

    /**
     * @param from Position from which the item was moved
     * @param to   Position to which the item was moved
     * @return Whether the move should succeed
     */
    fun onMove(from: Int, to: Int): Boolean {
        if (to > 1) {
            if (content[to].appColor == null) {
                // Moved out of range
                return false
            }
        }

        // Move
        if (to > from) {
            Collections.rotate(content.subList(from, to + 1), -1)
        } else {
            Collections.rotate(content.subList(to, from + 1), 1)
        }
        notifyItemMoved(from, to)

        MainScope().launch(Dispatchers.IO) {
            val appColors = setPriorities()
            historyDatabase.getAppColorDao().updateExclusive(appColors)
        }

        return true
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        historyDatabase.close()
        super.onDetachedFromRecyclerView(recyclerView)
    }

    /**
     * Set priorities according to the current order of items in content list
     *
     * @return Array of all [AppColor]s
     */
    internal fun setPriorities(): Array<AppColor> {
        val appColors = content
            .mapNotNull { it.appColor }
            .toTypedArray()

        var priority = appColors.size

        for (appColor in appColors) {
            appColor.priority = priority--
        }

        return appColors
    }

    class ViewHolder(
        private val binding: RowColorBinding,
        private val content: MutableList<TimeAppColor>,
        touchHelper: ItemTouchHelper
    ) : RecyclerView.ViewHolder(binding.root) {
        val mContentLayout: LinearLayout = binding.content
        val mPackageName: TextView = binding.textviewPackageName
        val mTimeUsed: TextView = binding.textviewTimeUsed
        val mAppIcon: ImageView = binding.appIcon
        val mColorDisplay: View = binding.colorDisplay
        val mHandle: ImageView = binding.dragHandle

        lateinit var item: TimeAppColor

        init {
            mHandle.setOnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    touchHelper.startDrag(this@ViewHolder)
                }
                false
            }

            mContentLayout.setOnClickListener {
                object : ColorPickerDialog(
                    itemView.context,
                    item
                ) {
                    override fun onColorSet(color: TimeAppColor) {
                        mColorDisplay.setBackgroundColor(color.appColor!!.color)
                        mHandle.visibility = View.VISIBLE

                        // Move upwards in content
                        moveToFirstUncolored(item)
                    }

                    override fun onColorRemoved() {
                        mColorDisplay.background =
                            itemView.context.getDrawable(R.drawable.custom_light_square)
                        mHandle.visibility = View.INVISIBLE

                        // Move downwards
                        moveToFirstUncolored(item)

                        if (item.appColor != null)
                            MainScope().launch(Dispatchers.IO) {
                                (bindingAdapter as ColorAdapter).historyDatabase
                                    .getAppColorDao().delete(item.appColor!!)
                            }
                    }

                    /**
                     * @return New position of `item`
                     */
                    private fun moveToFirstUncolored(item: TimeAppColor): Int {
                        val oldPosition = content.indexOf(item)
                        content.remove(item)

                        var firstUncoloredApp = 0
                        while (firstUncoloredApp < content.size) {
                            if (content[firstUncoloredApp].appColor == null) break
                            firstUncoloredApp++
                        }

                        content.add(firstUncoloredApp, item)
                        bindingAdapter!!.notifyItemMoved(oldPosition, firstUncoloredApp)

                        // Set priorities and update database
                        MainScope().launch(Dispatchers.IO) {
                            (bindingAdapter as ColorAdapter).historyDatabase
                                .getAppColorDao().updateExclusive(
                                    (bindingAdapter as ColorAdapter).setPriorities()
                                )
                        }

                        return firstUncoloredApp
                    }
                }.show()
            }
        }
    }
}
