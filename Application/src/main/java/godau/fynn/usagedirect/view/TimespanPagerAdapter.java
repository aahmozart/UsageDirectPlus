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

package godau.fynn.usagedirect.view;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.wrapper.Interval;

public class TimespanPagerAdapter extends PagerAdapter {

    private final Activity context;

    public TimespanPagerAdapter(Activity context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view = context.getLayoutInflater().inflate(R.layout.content_timespan, container, false);
        container.addView(view);

        SmartTabLayout tabLayout = view.findViewById(R.id.viewpagertab);

        ViewPager viewPager = view.findViewById(R.id.viewpager);
        viewPager.setAdapter(
                new UsageListViewPagerAdapter(Interval.values()[position], context)
        );

        viewPager.setCurrentItem(viewPager.getAdapter().getCount() - 1);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.setViewPager(viewPager);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object || ((View) object).getParent() == object;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return context.getString(R.string.span_daily);
            case 1: return context.getString(R.string.span_weekly);
            case 2: return context.getString(R.string.span_monthly);
            case 3: return context.getString(R.string.span_yearly);
            default: return null;
        }
    }
}
