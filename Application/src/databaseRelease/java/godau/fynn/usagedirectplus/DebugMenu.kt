package godau.fynn.usagedirectplus

import android.content.Context
import android.view.Menu
import android.view.MenuItem

/**
 * Dummy implementation in place of debug class
 */
object DebugMenu {

    @JvmStatic
    fun addTo(menu: Menu) {}

    @JvmStatic
    fun onOptionsItemSelected(menuItem: MenuItem, context: Context): Boolean {
        return false
    }
}
