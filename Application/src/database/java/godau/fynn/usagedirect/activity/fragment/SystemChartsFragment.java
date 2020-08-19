package godau.fynn.usagedirect.activity.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import godau.fynn.usagedirect.R;

public class SystemChartsFragment extends Fragment {

    private static final String SYSTEM_FLAVOR_PACKAGE_NAME = "godau.fynn.usagedirect.system";

    private View systemBox;
    private TextView systemText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_system, container, false);
        systemBox = view.findViewById(R.id.system_layout);
        systemText = view.findViewById(R.id.text_system);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        Intent databaseIntent = getContext().getPackageManager().getLaunchIntentForPackage(SYSTEM_FLAVOR_PACKAGE_NAME);
        boolean systemInstalled = databaseIntent != null;

        if (systemInstalled) {
            systemText.setText(R.string.help_system_flavor_installed);
            systemBox.setElevation(0f);
            systemBox.setClickable(false);
        } else {
            systemBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://f-droid.org/packages/" + SYSTEM_FLAVOR_PACKAGE_NAME)));
                }
            });
        }
    }
}
