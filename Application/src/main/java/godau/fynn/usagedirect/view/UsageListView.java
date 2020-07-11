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

import android.app.usage.UsageStats;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirect.UsageListAdapter;

import java.util.List;

public class UsageListView extends RecyclerView {

    private UsageListAdapter adapter;

    public UsageListView(@NonNull Context context) {
        super(context);

        adapter = new UsageListAdapter(context);

        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(context));
        addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        // Reduce lag upon initial scroll
        setItemViewCacheSize(6);

    }

    public void setUsageStatsList(List<UsageStats> usageStatsList) {
        adapter.setUsageStatsList(usageStatsList);
    }
}
