package com.grabthesevehicles.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class RequestReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!RequestScheduler.ACTION.equals(intent.getAction()) || !RequestScheduler.enabled(context)) return;
        try { SimeonNotifications.deliver(context); }
        finally { RequestScheduler.scheduleNew(context); }
    }
}
