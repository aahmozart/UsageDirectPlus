/*
 * usageDirect
 * Copyright (C) 2020 Fynn Godau
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package godau.fynn.usagedirectplus.view.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirectplus.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Parent pager, pages {@link UsageListViewPagerAdapter}
 */
public abstract class TimespanPagerAdapter extends PagerAdapter implements ViewPager.OnPageChangeListener {

    protected final Activity context;
    private Map<Integer, ViewPager> viewPagerMap = new HashMap<>();


    public TimespanPagerAdapter(Activity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        final View view = context.getLayoutInflater().inflate(R.layout.content_timespan, container, false);
        container.addView(view);

        new Thread(new Runnable() {
            @Override
            public void run() {

                prepare(position);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SmartTabLayout tabLayout = view.findViewById(R.id.viewpagertab);

                        ViewPager viewPager = view.findViewById(R.id.viewpager);
                        viewPager.setAdapter(
                                getUsageListViewPagerAdapter(position)
                        );

                        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
                        viewPager.setOffscreenPageLimit(2);

                        tabLayout.setViewPager(viewPager);

                        viewPagerMap.put(position, viewPager);
                    }
                });
            }
        }).start();


        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
        viewPagerMap.remove(position);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object || ((View) object).getParent() == object;
    }

    @Override
    public void notifyDataSetChanged() {
        for (ViewPager pager : viewPagerMap.values()) {
            pager.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onPageSelected(int position) {
        // Scroll back to initial position
        if (viewPagerMap.containsKey(position)) {
            ViewPager viewPager = viewPagerMap.get(position);
            viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public abstract void prepare(int position);

    public abstract UsageListViewPagerAdapter getUsageListViewPagerAdapter(int position);
}
