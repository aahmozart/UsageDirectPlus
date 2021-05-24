package godau.fynn.usagedirect.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import androidx.viewpager.widget.ViewPager;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

/**
 * Similar to https://github.com/ogaclejapan/SmartTabLayout/issues/222#issuecomment-345828780:
 *
 * Invoke <code>onSizeChanged</code> after attaching a view pager. Beforehand, the first and
 * last tab are measured because otherwise everything is off a bit.
 */
public class LateInitSmartTabLayout extends SmartTabLayout {
    public LateInitSmartTabLayout(Context context) {
        super(context);
    }

    public LateInitSmartTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LateInitSmartTabLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setViewPager(ViewPager viewPager) {
        super.setViewPager(viewPager);

        View firstTab = ((ViewGroup) tabStrip).getChildAt(0);
        View lastTab = ((ViewGroup) tabStrip).getChildAt(((ViewGroup) tabStrip).getChildCount() - 1);
        firstTab.measure(0, 0);
        lastTab.measure(0, 0);
        onSizeChanged(getMeasuredWidth(), getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight());

    }
}
