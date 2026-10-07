package com.grabthesevehicles.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class RequestReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!RequestScheduler.ACTION.equals(intent.getAction()) || !RequestScheduler.enabled(context)) return;
        if (RequestScheduler.prefs(context).getInt("scheduled_boot",-1) == RequestScheduler.bootCount(context)
            && RequestScheduler.prefs(context).getLong("next_elapsed",0) > android.os.SystemClock.elapsedRealtime()) return;
        try { SimeonNotifications.deliver(context); }
        finally { RequestScheduler.scheduleNew(context); }
    }
}
