package godau.fynn.usagedirect.view.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import godau.fynn.typedrecyclerview.SimpleRecyclerViewAdapter;
import godau.fynn.usagedirect.R;

import java.util.List;

public class ChartSelectionAdapter extends SimpleRecyclerViewAdapter<ChartSelectionAdapter.ChartProvider, ChartSelectionAdapter.ViewHolder> {


    public ChartSelectionAdapter(List<ChartProvider> content) {
        super(content);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.row_chart_selection, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChartSelectionAdapter.ViewHolder holder, ChartProvider item, int position) {
        holder.title.setText(item.title);
        holder.setOnClickFragment(item.fragment);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        final TextView title;
        private Class<? extends Fragment> onClickFragment;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            itemView.findViewById(R.id.row).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((FragmentActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_right, R.anim.slide_out_right)
                            .replace(R.id.fragment, onClickFragment, null)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        public void setOnClickFragment(Class<? extends Fragment> fragment) {
            this.onClickFragment = fragment;
        }
    }

    public static class ChartProvider {

        final @StringRes int title;
        final Class<? extends Fragment> fragment;

        public ChartProvider(@StringRes int title, Class<? extends Fragment> fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }
}
