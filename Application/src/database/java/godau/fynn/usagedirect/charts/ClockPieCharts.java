package godau.fynn.usagedirect.charts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.view.adapter.ClockPieViewPagerAdapter;
import godau.fynn.usagedirect.wrapper.EventLogWrapper;

import java.util.Map;

public class ClockPieCharts extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_clock_pie_charts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final ViewPager clockPager = view.findViewById(R.id.clock_pie_view_pager);

        new Thread(() -> {
            HistoryDatabase database = HistoryDatabase.get(getContext());
            Map<String, Integer> colorMap = database.getAppColorDao().getAppColorMap();
            database.close();

            new Handler(Looper.getMainLooper()).post(() -> {
                clockPager.setAdapter(new ClockPieViewPagerAdapter(
                        getContext(), new EventLogWrapper(getContext()),
                        colorMap
                ));
                clockPager.setCurrentItem(9);

                clockPager.setOffscreenPageLimit(3);

                SmartTabLayout chartTabLayout = view.findViewById(R.id.clock_pie_view_pager_tab);
                chartTabLayout.setViewPager(clockPager);
            });
        }).start();

    }
}
