/*
* Copyright 2014 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.appusagestatistics;

import android.app.AlertDialog;
import android.app.usage.UsageStats;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.Collections;
import java.util.List;

/**
 * Fragment that demonstrates how to use App Usage Statistics API.
 */
public class AppUsageStatisticsFragment extends Fragment {

    private static final String TAG = AppUsageStatisticsFragment.class.getSimpleName();

    private UsageStatsWrapper usageStatsWrapper;

    private UsageListAdapter mUsageListAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private Spinner mSpinner;

    private static final int REQUEST_SETTINGS = 0;

    public AppUsageStatisticsFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment {@link AppUsageStatisticsFragment}.
     */
    public static AppUsageStatisticsFragment newInstance() {
        return new AppUsageStatisticsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        usageStatsWrapper = new UsageStatsWrapper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_app_usage_statistics, container, false);
    }

    @Override
    public void onViewCreated(View rootView, Bundle savedInstanceState) {
        super.onViewCreated(rootView, savedInstanceState);

        mUsageListAdapter = new UsageListAdapter(getActivity());

        mRecyclerView = rootView.findViewById(R.id.recyclerview_app_usage);
        mLayoutManager = mRecyclerView.getLayoutManager();
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(mUsageListAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mSpinner = rootView.findViewById(R.id.spinner_time_span);
        SpinnerAdapter spinnerAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.action_list, android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(spinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                UsageStatsWrapper.StatsUsageInterval statsUsageInterval = UsageStatsWrapper.StatsUsageInterval
                        .values()[position];

                if (usageStatsWrapper.isPermissionGranted()) {
                    List<UsageStats> usageStatsList =
                            usageStatsWrapper.getUsageStatistics(statsUsageInterval, 0);

                    // Filter unused apps
                    for (int i = usageStatsList.size() - 1; i >= 0; i--) {
                        UsageStats usageStats = usageStatsList.get(i);
                        if (usageStats.getTotalTimeInForeground() <= 0)
                            usageStatsList.remove(i);
                    }

                    Collections.sort(usageStatsList, new Comparator.TimeInForegroundComparatorDesc());

                    updateAppsList(usageStatsList);
                } else {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.explanation_access_appusage_title)
                            .setMessage(R.string.explanation_access_appusage_message)
                            .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_SETTINGS);
                                }
                            })
                            .setNegativeButton(R.string.leave_app, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                }
                            })
                            .setCancelable(false)
                            .show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Updates the {@link #mRecyclerView} with the list of {@link UsageStats} passed as an argument.
     *
     * @param usageStatsList A list of {@link UsageStats} from which update the
     *                       {@link #mRecyclerView}.
     */
    private void updateAppsList(List<UsageStats> usageStatsList) {
        mUsageListAdapter.setCustomUsageStatsList(usageStatsList);
        mUsageListAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);

        new IconThread(usageStatsList, mLayoutManager, getActivity()).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_SETTINGS)
            getActivity().recreate();
    }
}
