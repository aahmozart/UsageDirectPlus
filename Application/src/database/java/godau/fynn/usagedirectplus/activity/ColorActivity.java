package godau.fynn.usagedirectplus.activity;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.usagedirectplus.R;
import godau.fynn.usagedirectplus.persistence.HistoryDatabase;
import godau.fynn.usagedirectplus.persistence.combined.TimeAppColor;
import godau.fynn.usagedirectplus.thread.icon.IconThread;
import godau.fynn.usagedirectplus.view.adapter.ColorAdapter;
import godau.fynn.usagedirectplus.view.adapter.ColorAdapterTouchCallback;

import java.util.Arrays;

public class ColorActivity extends Activity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_color);

        new Thread(() -> {

            HistoryDatabase database = HistoryDatabase.get(this);

            TimeAppColor[] timeAppColors = database.getAppColorDao().getTimeAppColors();
            database.close();

            runOnUiThread(() -> {

                recyclerView = findViewById(R.id.recyclerview);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));

                ItemTouchHelper touchHelper = new ItemTouchHelper(new ColorAdapterTouchCallback());
                touchHelper.attachToRecyclerView(recyclerView);

                recyclerView.setAdapter(new ColorAdapter(timeAppColors, touchHelper));


                new IconThread(
                        Arrays.stream(timeAppColors)
                                .map(TimeAppColor::getApplicationId)
                                .toArray(String[]::new),
                        recyclerView.getLayoutManager(), this
                ).start();

                // Refresh calling activity after database call
                setResult(RESULT_OK);

            });
        }).start();
    }

    @Override
    protected void onDestroy() {

        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }

        super.onDestroy();
    }
}
