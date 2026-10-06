package com.grabthesevehicles.app;
import android.app.*;
import android.content.*;
import android.os.*;
import android.telecom.DisconnectCause;
import java.util.UUID;

/** Session state and timeout only. Android's Phone app owns incoming and connected call UI. */
public final class CallNotifications {
    public static final String CHANNEL="gta_calls_v1", PLAYBACK="gta_call_playback_v1";
    static final int ID=801;
    private CallNotifications() {}
    static PendingIntent timeout(Context c,String token) {
        return PendingIntent.getBroadcast(c,22,new Intent(c,CallReceiver.class).setAction(CallReceiver.END)
            .setData(android.net.Uri.parse("gtv-call:"+token)).putExtra("token",token).putExtra("missed",true),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void channels(Context c) {
        NotificationChannel playback=new NotificationChannel(PLAYBACK,"Answered GTA calls",NotificationManager.IMPORTANCE_LOW);
        playback.setSound(null,null); playback.enableVibration(false);
        c.getSystemService(NotificationManager.class).createNotificationChannel(playback);
    }
    public static boolean allowed(Context c) { return CallTelecom.ready(c); }
    static boolean matches(Context c,String token) { return token!=null && token.equals(CallScheduler.prefs(c).getString("active_token",null)); }
    static String phase(Context c) { return CallScheduler.prefs(c).getString("active_phase",""); }
    static boolean active(Context c) {
        return !phase(c).isEmpty() && CallScheduler.prefs(c).getInt("active_boot",-1)==CallScheduler.bootCount(c)
            && CallScheduler.prefs(c).getLong("active_until",0)>SystemClock.elapsedRealtime();
    }
    public static synchronized boolean ring(Context c,int contact) {
        if(contact<0 || contact>=CallContacts.IDS.length || !CallClips.available(c,contact) || !allowed(c) || active(c) || CallPlaybackService.running) return false;
        end(c,null); channels(c);
        String token=UUID.randomUUID().toString(); long until=SystemClock.elapsedRealtime()+45_000;
        CallScheduler.prefs(c).edit().putString("active_token",token).putString("active_phase","ringing").putInt("active_contact",contact)
            .putInt("active_boot",CallScheduler.bootCount(c)).putLong("active_until",until).commit();
        AlarmManager alarms=c.getSystemService(AlarmManager.class); PendingIntent timeout=timeout(c,token);
        try {
            if(CallScheduler.exactAllowed(c)) alarms.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,until,timeout);
            else alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,until,timeout);
        } catch(SecurityException denied) { alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,until,timeout); }
        try { CallTelecom.incoming(c,contact,token); return true; }
        catch(Exception denied) { android.util.Log.e("GtaCalls","Native incoming call failed",denied); end(c,token,DisconnectCause.ERROR); return false; }
    }
    static synchronized boolean answer(Context c,String token) {
        if(!matches(c,token) || !active(c) || !"ringing".equals(phase(c))) return false;
        c.getSystemService(AlarmManager.class).cancel(timeout(c,token));
        CallScheduler.prefs(c).edit().putString("active_phase","playing").putBoolean("active_held",false).putLong("active_until",SystemClock.elapsedRealtime()+300_000).commit();
        return true;
    }
    public static synchronized void end(Context c,String token) { end(c,token,DisconnectCause.LOCAL); }
    static synchronized void end(Context c,String token,int cause) {
        if(token!=null && !matches(c,token)) return;
        // Also removes the old app-owned incoming notification during upgrades.
        c.getSystemService(NotificationManager.class).cancel(ID);
        String current=CallScheduler.prefs(c).getString("active_token",null);
        if(current!=null) c.getSystemService(AlarmManager.class).cancel(timeout(c,current));
        CallScheduler.prefs(c).edit().remove("active_token").remove("active_phase").remove("active_contact").remove("active_until").remove("active_boot").remove("active_held").commit();
        CallTelecom.end(current,cause); c.stopService(new Intent(c,CallPlaybackService.class));
    }
    static Notification playing(Context c,String token,int contact) {
        channels(c);
        PendingIntent end=PendingIntent.getBroadcast(c,25,new Intent(c,CallReceiver.class).setAction(CallReceiver.END)
            .setData(android.net.Uri.parse("gtv-call:"+token)).putExtra("token",token),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(c,PLAYBACK).setSmallIcon(R.drawable.ic_message).setContentTitle(CallContacts.NAMES[contact])
            .setContentText("GTA call in progress").setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_SERVICE)
            .addAction(new Notification.Action.Builder(null,"Hang up",end).build()).build();
    }
}
