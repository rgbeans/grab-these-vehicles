package com.grabthesevehicles.app;
import android.content.*;
import java.util.List;
import java.util.Random;
public final class CallReceiver extends BroadcastReceiver {
    public static final String END="com.grabthesevehicles.app.END_CALL";
    @Override public void onReceive(Context c,Intent intent) {
        if(END.equals(intent.getAction())) { CallNotifications.end(c,intent.getStringExtra("token"),intent.getBooleanExtra("missed",false)?android.telecom.DisconnectCause.MISSED:android.telecom.DisconnectCause.LOCAL); return; }
        if(!CallScheduler.ACTION.equals(intent.getAction()) || !CallScheduler.enabled(c)) return;
        if(CallScheduler.prefs(c).getInt("scheduled_boot",-1)==CallScheduler.bootCount(c)
            && CallScheduler.prefs(c).getLong("next_elapsed",0)>android.os.SystemClock.elapsedRealtime()) return;
        List<Integer> callers=CallClips.callers(c);
        if(!callers.isEmpty()) CallNotifications.ring(c,callers.get(new Random().nextInt(callers.size())));
        CallScheduler.scheduleNew(c);
    }
}
