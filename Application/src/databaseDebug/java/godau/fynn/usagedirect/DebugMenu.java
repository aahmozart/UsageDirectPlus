package godau.fynn.usagedirect;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import godau.fynn.usagedirect.view.dialog.DatabaseDebugDialog;

public class DebugMenu {

    public static void addTo(Menu menu) {
        SubMenu subMenu = menu.addSubMenu(R.string.menu_debug);

        subMenu.add(R.string.menu_debug_database);
    }

    public static boolean onOptionsItemSelected(MenuItem item, Context context) {

        if (item.getTitle().equals(context.getString(R.string.menu_debug_database))) {
            new DatabaseDebugDialog(context).show();
            return true;
        } else return false;

    }

}
