package godau.fynn.usagedirectplus.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import godau.fynn.usagedirectplus.databinding.FragmentClockPieChartsBinding
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.view.adapter.ClockPieViewPagerAdapter
import godau.fynn.usagedirectplus.wrapper.HarmonizedEventLogWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClockPieCharts : Fragment() {

    private var _binding: FragmentClockPieChartsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClockPieChartsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = HistoryDatabase.get(context!!)
            val colorMap = database.getAppColorDao().getAppColorMap()
            database.close()

            withContext(Dispatchers.Main) {
                binding.clockPieViewPager.adapter = ClockPieViewPagerAdapter(
                    context!!, HarmonizedEventLogWrapper(context!!),
                    colorMap
                )
                binding.clockPieViewPager.currentItem = 9

                binding.clockPieViewPagerTab.setViewPager(binding.clockPieViewPager)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
