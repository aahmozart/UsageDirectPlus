package godau.fynn.usagedirectplus.charts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import godau.fynn.usagedirectplus.databinding.FragmentUsageTimelineBinding
import godau.fynn.usagedirectplus.persistence.HistoryDatabase
import godau.fynn.usagedirectplus.view.adapter.UnlockTimelineViewPagerAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UnlockTimelineChart : Fragment() {

    private var _binding: FragmentUsageTimelineBinding? = null
    private val binding get() = _binding!!
    private var dayCount = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUsageTimelineBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.timelineNavPrev.setOnClickListener {
            val current = binding.timelineViewPager.currentItem
            if (current > 0) binding.timelineViewPager.currentItem = current - 1
        }
        binding.timelineNavNext.setOnClickListener {
            val current = binding.timelineViewPager.currentItem
            if (current < dayCount - 1) binding.timelineViewPager.currentItem = current + 1
        }

        binding.timelineViewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                updateDateLabel(position)
                updateArrowVisibility(position)
            }
        })

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val database = HistoryDatabase.get(context!!)
            val screenEventDao = database.getScreenEventDao()
            val firstTimestamp = screenEventDao.getFirstScreenEventTimestamp()
            database.close()

            val today = LocalDate.now().toEpochDay()
            val firstDay = if (firstTimestamp > 0) {
                java.time.Instant.ofEpochMilli(firstTimestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay()
            } else {
                today
            }
            dayCount = maxOf((today - firstDay + 1).toInt(), 1)

            withContext(Dispatchers.Main) {
                binding.timelineViewPager.adapter = UnlockTimelineViewPagerAdapter(
                    context!!,
                    HistoryDatabase.get(context!!).getScreenEventDao(),
                    dayCount
                )
                binding.timelineViewPager.currentItem = dayCount - 1
                updateDateLabel(dayCount - 1)
                updateArrowVisibility(dayCount - 1)
            }
        }
    }

    private fun updateDateLabel(position: Int) {
        val dayOffset = dayCount - 1 - position
        val date = LocalDate.now().minusDays(dayOffset.toLong())
        binding.timelineDateLabel.text = DATE_FORMAT.format(date)
    }

    private fun updateArrowVisibility(position: Int) {
        binding.timelineNavPrev.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
        binding.timelineNavNext.visibility = if (position < dayCount - 1) View.VISIBLE else View.INVISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy")
    }
}
