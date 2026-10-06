package com.grabthesevehicles.app;
import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.os.*;
import java.util.UUID;

public final class CallNotifications {
    public static final String CHANNEL="gta_calls_v1", PLAYBACK="gta_call_playback_v1";
    static final int ID=801;
    private CallNotifications() {}
    static PendingIntent timeout(Context c,String token) {
        return PendingIntent.getBroadcast(c,22,new Intent(c,CallReceiver.class).setAction(CallReceiver.END).setData(android.net.Uri.parse("gtv-call:"+token)).putExtra("token",token),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void channels(Context c) {
        NotificationManager manager=c.getSystemService(NotificationManager.class);
        NotificationChannel incoming=new NotificationChannel(CHANNEL,"Incoming GTA calls",NotificationManager.IMPORTANCE_HIGH);
        incoming.setDescription("Optional simulated calls with the GTA phone ringtone");
        incoming.setSound(SoundProvider.RINGTONE_URI,new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
        incoming.enableVibration(true); manager.createNotificationChannel(incoming);
        NotificationChannel playback=new NotificationChannel(PLAYBACK,"Answered GTA calls",NotificationManager.IMPORTANCE_LOW);
        playback.setSound(null,null); playback.enableVibration(false); manager.createNotificationChannel(playback);
    }
    public static boolean allowed(Context c) {
        if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) return false;
        NotificationManager manager=c.getSystemService(NotificationManager.class);
        NotificationChannel channel=manager.getNotificationChannel(CHANNEL);
        return manager.areNotificationsEnabled() && (channel==null || channel.getImportance()!=NotificationManager.IMPORTANCE_NONE);
    }
    static boolean matches(Context c,String token) { return token!=null && token.equals(CallScheduler.prefs(c).getString("active_token",null)); }
    static String phase(Context c) { return CallScheduler.prefs(c).getString("active_phase",""); }
    static boolean active(Context c) {
        return !phase(c).isEmpty() && CallScheduler.prefs(c).getInt("active_boot",-1)==CallScheduler.bootCount(c)
            && CallScheduler.prefs(c).getLong("active_until",0)>SystemClock.elapsedRealtime();
    }
    static PendingIntent screen(Context c,String token,boolean answer) {
        return PendingIntent.getActivity(c,answer?24:23,new Intent(c,CallActivity.class).setAction(answer?"answer":"view").setData(android.net.Uri.parse("gtv-call:"+token)).putExtra("token",token).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    public static synchronized boolean ring(Context c,int contact) {
        if(contact<0 || contact>=CallContacts.IDS.length || !CallClips.available(c,contact) || !allowed(c) || active(c) || CallPlaybackService.running) return false;
        end(c,null); channels(c);
        String token=UUID.randomUUID().toString(); long until=SystemClock.elapsedRealtime()+45_000;
        CallScheduler.prefs(c).edit().putString("active_token",token).putString("active_phase","ringing").putInt("active_contact",contact).putInt("active_boot",CallScheduler.bootCount(c)).putLong("active_until",until).commit();
        PendingIntent decline=timeout(c,token), answer=screen(c,token,true), open=screen(c,token,false);
        Notification.Builder b=new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_message).setContentTitle(CallContacts.NAMES[contact])
            .setContentText("Incoming simulated call").setCategory(Notification.CATEGORY_CALL).setContentIntent(open).setDeleteIntent(decline)
            .setVisibility(Notification.VISIBILITY_PUBLIC).setColor(Color.rgb(255,176,71)).setTimeoutAfter(45_000);
        int icon=CallContacts.icon(contact);
        if(icon!=0) b.setLargeIcon(Icon.createWithResource(c,icon));
        if(Build.VERSION.SDK_INT>=31) {
            Person.Builder person=new Person.Builder().setName(CallContacts.NAMES[contact]).setImportant(true);
            if(icon!=0) person.setIcon(Icon.createWithResource(c,icon));
            b.setStyle(Notification.CallStyle.forIncomingCall(person.build(),decline,answer));
        } else { b.addAction(new Notification.Action.Builder(null,"Decline",decline).build()); b.addAction(new Notification.Action.Builder(null,"Answer",answer).build()); }
        NotificationManager manager=c.getSystemService(NotificationManager.class);
        if(Build.VERSION.SDK_INT<34 || manager.canUseFullScreenIntent()) b.setFullScreenIntent(open,true);
        Notification n=b.build(); n.flags|=Notification.FLAG_INSISTENT;
        try { manager.notify(ID,n); }
        catch(SecurityException denied) { end(c,token); return false; }
        AlarmManager alarms=c.getSystemService(AlarmManager.class);
        try {
            if(CallScheduler.exactAllowed(c)) alarms.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,until,decline);
            else alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,until,decline);
        } catch(SecurityException denied) { alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP,until,decline); }
        return true;
    }
    static synchronized boolean answer(Context c,String token) {
        if(!matches(c,token) || !active(c) || !"ringing".equals(phase(c))) return false;
        c.getSystemService(NotificationManager.class).cancel(ID);
        c.getSystemService(AlarmManager.class).cancel(timeout(c,token));
        CallScheduler.prefs(c).edit().putString("active_phase","playing").putLong("active_until",SystemClock.elapsedRealtime()+300_000).commit();
        return true;
    }
    public static synchronized void end(Context c,String token) {
        if(token!=null && !matches(c,token)) return;
        c.getSystemService(NotificationManager.class).cancel(ID);
        String current=CallScheduler.prefs(c).getString("active_token",null);
        if(current!=null) c.getSystemService(AlarmManager.class).cancel(timeout(c,current));
        CallScheduler.prefs(c).edit().remove("active_token").remove("active_phase").remove("active_contact").remove("active_until").remove("active_boot").commit();
        c.stopService(new Intent(c,CallPlaybackService.class));
    }
    static Notification playing(Context c,String token,int contact) {
        channels(c);
        return new Notification.Builder(c,PLAYBACK).setSmallIcon(R.drawable.ic_message).setContentTitle(CallContacts.NAMES[contact])
            .setContentText("Playing call recording").setContentIntent(screen(c,token,false)).setOngoing(true).setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_SERVICE).addAction(new Notification.Action.Builder(null,"Hang up",timeout(c,token)).build()).build();
    }
}
