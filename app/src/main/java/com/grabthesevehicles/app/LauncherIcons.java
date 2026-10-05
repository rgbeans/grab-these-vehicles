package com.grabthesevehicles.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;

/** Switch launcher aliases without disabling the activity that receives alarms and copy intents. */
public final class LauncherIcons {
    private LauncherIcons() {}
    public static int selected(Context c) {
        String id = RequestScheduler.prefs(c).getString("app_icon", "simeon");
        for (int i=0;i<ContactMessages.IDS.length;i++)
            if (ContactMessages.IDS[i].equals(id) && ContactIcons.resource(i)!=0) return i;
        return 0;
    }
    static ComponentName component(Context c, int contact) {
        return new ComponentName(c.getPackageName(), c.getPackageName()+".Launcher_"+ContactMessages.IDS[contact]);
    }
    public static void select(Context c, int contact) {
        if (contact<0 || contact>=ContactMessages.IDS.length || ContactIcons.resource(contact)==0)
            throw new IllegalArgumentException("This contact has no verified icon");
        PackageManager manager=c.getPackageManager();
        if (Build.VERSION.SDK_INT>=33) {
            List<PackageManager.ComponentEnabledSetting> changes=new ArrayList<>();
            for(int i=0;i<ContactMessages.IDS.length;i++) if(ContactIcons.resource(i)!=0)
                changes.add(new PackageManager.ComponentEnabledSetting(component(c,i),
                    i==contact ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP));
            manager.setComponentEnabledSettings(changes);
        } else {
            // Enable the replacement first so older Android versions always keep a launcher entry.
            manager.setComponentEnabledSetting(component(c,contact),PackageManager.COMPONENT_ENABLED_STATE_ENABLED,PackageManager.DONT_KILL_APP);
            for(int i=0;i<ContactMessages.IDS.length;i++) if(i!=contact && ContactIcons.resource(i)!=0)
                manager.setComponentEnabledSetting(component(c,i),PackageManager.COMPONENT_ENABLED_STATE_DISABLED,PackageManager.DONT_KILL_APP);
        }
        RequestScheduler.prefs(c).edit().putString("app_icon",ContactMessages.IDS[contact]).commit();
    }
}
