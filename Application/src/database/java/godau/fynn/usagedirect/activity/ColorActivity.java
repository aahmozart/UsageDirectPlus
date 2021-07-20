package godau.fynn.usagedirect.activity;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.persistence.HistoryDatabase;
import godau.fynn.usagedirect.persistence.combined.TimeAppColor;
import godau.fynn.usagedirect.thread.icon.IconThread;
import godau.fynn.usagedirect.view.adapter.ColorAdapter;

import java.util.Arrays;

public class ColorActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color);

        new Thread(() -> {

            HistoryDatabase database = HistoryDatabase.get(this);

            TimeAppColor[] timeAppColors = database.getAppColorDao().getTimeAppColors();
            database.close();

            runOnUiThread(() -> {

                RecyclerView recyclerView = findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(new ColorAdapter(timeAppColors));

                new IconThread(
                        Arrays.stream(timeAppColors)
                                .map(TimeAppColor::getApplicationId)
                                .toArray(String[]::new),
                        recyclerView.getLayoutManager(), this
                ).start();

            });
        }).start();
    }
}
