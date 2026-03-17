package godau.fynn.usagedirectplus.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.databinding.RowChartSelectionBinding

class ChartSelectionAdapter(content: List<ChartProvider>) :
    SimpleRecyclerViewAdapter<ChartSelectionAdapter.ChartProvider, ChartSelectionAdapter.ViewHolder>(content.toMutableList()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = RowChartSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: ChartProvider, position: Int) {
        holder.title.setText(item.title)
        holder.setOnClickFragment(item.fragment)
    }

    inner class ViewHolder(private val binding: RowChartSelectionBinding) : RecyclerView.ViewHolder(binding.root) {

        val title: TextView = binding.title
        private var onClickFragment: Class<out Fragment>? = null

        init {
            binding.row.setOnClickListener {
                (context as FragmentActivity).supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(
                        R.anim.slide_in_left, R.anim.slide_out_left,
                        R.anim.slide_in_right, R.anim.slide_out_right
                    )
                    .replace(R.id.fragment, onClickFragment!!, null)
                    .addToBackStack(null)
                    .commit()
            }
        }

        fun setOnClickFragment(fragment: Class<out Fragment>) {
            this.onClickFragment = fragment
        }
    }

    class ChartProvider(
        @JvmField @StringRes val title: Int,
        @JvmField val fragment: Class<out Fragment>
    )
}
