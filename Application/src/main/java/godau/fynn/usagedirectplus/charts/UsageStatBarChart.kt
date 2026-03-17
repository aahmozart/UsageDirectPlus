package godau.fynn.usagedirectplus.charts

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import godau.fynn.usagedirectplus.R
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import im.dacer.androidcharts.bar.BarView
import im.dacer.androidcharts.bar.Line
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class UsageStatBarChart : Fragment() {

    private lateinit var textView: TextView
    protected lateinit var barView: BarView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(getLayout(), container, false)

        textView = view.findViewById(R.id.bar_chart_label)
        barView = view.findViewById(R.id.bar_chart)

        return view
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setText(getText())

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = HistoryDatabase.get(context!!)

            getData(database)

            database.close()

            withContext(Dispatchers.Main) { onDataLoaded() }
        }
    }

    @LayoutRes
    protected open fun getLayout(): Int {
        return R.layout.content_bar_view
    }

    protected fun setText(@StringRes text: Int) {
        textView.setText(text)
    }

    /**
     * Run outside of the main thread before [onDataLoaded] is run.
     * This is the time for the subclass to gather the data it needs from the
     * database. The database need not be closed here.
     */
    protected abstract fun getData(database: HistoryDatabase)

    @StringRes
    protected abstract fun getText(): Int

    /**
     * Responsible for displaying the data loaded from database in view.
     * Run on UI thread.
     */
    protected abstract fun onDataLoaded()

    /**
     * Calculate positions of vertical lines and their texts for scale
     *
     * @param chartMax Maximum value (upper border) in the chart
     */
    protected fun addScale(chartMax: Int) {
        // Calculate vertical line frequency
        val maxHours = (chartMax / 60 / 60) + 1
        var frequency = 1
        while (maxHours / frequency > 10) {
            // If a power of 10, increase by the factor 2
            if (Math.log10(frequency.toDouble()) % 1 == 0.0) {
                frequency *= 2
            } else {
                // If last step was a multiplication with 2, go back and multiply with 5
                if (Math.log10((frequency / 2).toDouble()) % 1 == 0.0) {
                    frequency = frequency / 2 * 5
                } else {
                    // If last step was this multiplication with 5, go to next power of 10
                    frequency *= 2
                }
            }
        }

        // Add lines
        // Note: maxHours is overestimating the total hours by up to one
        val lines = arrayOfNulls<Line>((maxHours - 1) / frequency)

        var counter = frequency
        var i = 0
        while (counter < maxHours) {
            lines[i] = Line(counter * 60 * 60, counter.toString())
            counter += frequency
            i++
        }

        barView.setHorizontalLines(lines, chartMax)

        // Get window background
        val a = TypedValue()
        context!!.theme.resolveAttribute(android.R.attr.windowBackground, a, true)

        barView.setScrollHorizontalLines(
            // Is a color if this condition is true - don't scroll background otherwise
            a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT,
            a.data
        )
    }
}
