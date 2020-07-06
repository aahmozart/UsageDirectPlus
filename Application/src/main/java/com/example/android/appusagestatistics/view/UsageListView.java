package com.example.android.appusagestatistics.view;

import android.app.usage.UsageStats;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android.appusagestatistics.UsageListAdapter;

import java.util.List;

public class UsageListView extends RecyclerView {

    private UsageListAdapter adapter;

    public UsageListView(@NonNull Context context) {
        super(context);

        adapter = new UsageListAdapter(context);

        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(context));
        addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

    }

    public void setUsageStatsList(List<UsageStats> usageStatsList) {
        adapter.setUsageStatsList(usageStatsList);
    }
}
