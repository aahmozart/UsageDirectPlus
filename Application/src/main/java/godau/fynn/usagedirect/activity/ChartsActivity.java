package godau.fynn.usagedirect.activity;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import godau.fynn.usagedirect.R;
import godau.fynn.usagedirect.activity.fragment.ChartSelectionFragment;

public class ChartsActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, new ChartSelectionFragment())
                .commit();
    }
}
