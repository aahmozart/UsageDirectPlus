package godau.fynn.usagedirectplus

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import godau.fynn.usagedirectplus.view.dialog.CustomQueryDialog
import godau.fynn.usagedirectplus.view.dialog.DatabaseDebugDialog

object DebugMenu {

    @JvmStatic
    fun addTo(menu: Menu) {
        val subMenu = menu.addSubMenu(R.string.menu_debug)

        subMenu.add(R.string.menu_debug_database)
        subMenu.add(R.string.menu_debug_custom_query)
    }

    @JvmStatic
    fun onOptionsItemSelected(item: MenuItem, context: Context): Boolean {
        return when {
            item.title == context.getString(R.string.menu_debug_database) -> {
                DatabaseDebugDialog(context).show()
                true
            }
            item.title == context.getString(R.string.menu_debug_custom_query) -> {
                CustomQueryDialog(context).show()
                true
            }
            else -> false
        }
    }
}
