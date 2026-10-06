package com.grabthesevehicles.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class RestoreReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            RequestScheduler.restoreAfterBoot(context);
            CallNotifications.end(context,null);
            CallScheduler.restoreAfterBoot(context);
        } else if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)
            || Intent.ACTION_TIME_CHANGED.equals(action) || Intent.ACTION_TIMEZONE_CHANGED.equals(action)
            || "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED".equals(action)) {
            if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) CallNotifications.end(context,null);
            RequestScheduler.restore(context);
            CallScheduler.restore(context);
        }
    }
}
