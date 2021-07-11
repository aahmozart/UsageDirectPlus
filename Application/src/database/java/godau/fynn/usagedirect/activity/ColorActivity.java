package godau.fynn.usagedirect.activity;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.thread.icon.IconThread;
import godau.fynn.usagedirect.view.adapter.ColorAdapter;

import java.util.Map;

public class ColorActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color);

        new Thread(() -> {

            HistoryDatabase database = HistoryDatabase.get(this);

            Map<String, Long> timePerApp = database.getUsageStatsDao().getTotalTimePerApp();
            database.close();

            runOnUiThread(() -> {

                RecyclerView recyclerView = findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new ColorAdapter(timePerApp));

                new IconThread(
                        timePerApp.keySet().toArray(new String[0]), recyclerView.getLayoutManager(), this
                ).start();

            });
        }).start();
    }
}
