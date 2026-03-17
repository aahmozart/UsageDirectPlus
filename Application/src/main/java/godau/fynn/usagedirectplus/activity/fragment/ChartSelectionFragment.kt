package godau.fynn.usagedirectplus.activity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import godau.fynn.usagedirectplus.charts.ChartProviders
import godau.fynn.usagedirectplus.databinding.FragmentChartSelectionBinding
import godau.fynn.usagedirectplus.view.adapter.ChartSelectionAdapter

class ChartSelectionFragment : Fragment() {

    private var _binding: FragmentChartSelectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChartSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerview.layoutManager = LinearLayoutManager(context)
        binding.recyclerview.adapter = ChartSelectionAdapter(ChartProviders.getChartProviders())

        binding.recyclerview.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
