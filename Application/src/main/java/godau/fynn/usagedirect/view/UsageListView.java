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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirect.view.adapter.UsageListAdapter;
import godau.fynn.usagedirect.SimpleUsageStat;

import java.util.List;
import java.util.Map;

public class UsageListView extends RecyclerView {

    private final UsageListAdapter adapter;

    public UsageListView(@NonNull Context context) {
        super(context);

        adapter = new UsageListAdapter();

        setAdapter(adapter);
        setLayoutManager(new LinearLayoutManager(context));
        addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));

        // Reduce lag upon initial scroll
        setItemViewCacheSize(6);

    }

    public void setUsageStatsList(List<SimpleUsageStat> usageStatsList) {
        adapter.setUsageStatsList(usageStatsList);
    }

    public void setLastUsedMap(Map<String, Long> map) {
        adapter.setLastUsedMap(map);
    }

    public void setColorMap(Map<String, Integer> colorMap) {
        adapter.setColorMap(colorMap);
    }
}
