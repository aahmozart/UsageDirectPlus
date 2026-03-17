package godau.fynn.usagedirectplus.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.ogaclejapan.smarttablayout.SmartTabLayout

/**
 * Similar to https://github.com/ogaclejapan/SmartTabLayout/issues/222#issuecomment-345828780:
 *
 * Invoke `onSizeChanged` after attaching a view pager. Beforehand, the first and
 * last tab are measured because otherwise everything is off a bit.
 */
class LateInitSmartTabLayout : SmartTabLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun setViewPager(viewPager: ViewPager) {
        super.setViewPager(viewPager)

        val strip = tabStrip as ViewGroup
        val firstTab = strip.getChildAt(0)
        val lastTab = strip.getChildAt(strip.childCount - 1)
        firstTab.measure(0, 0)
        lastTab.measure(0, 0)
        onSizeChanged(measuredWidth, measuredHeight, measuredWidth, measuredHeight)
    }
}
