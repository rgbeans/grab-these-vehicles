package com.grabthesevehicles.app;
import android.app.Service;
import android.content.Intent;
import android.media.*;
import android.os.*;
import android.widget.Toast;

/** Only replays a local recording; no microphone, speech processing, or real phone call. */
public final class CallPlaybackService extends Service {
    static volatile boolean running;
    private MediaPlayer player;
    private static CallPlaybackService instance;
    private boolean held,prepared;
    private String token;
    private final Handler handler=new Handler(Looper.getMainLooper());
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        String incoming=intent==null?null:intent.getStringExtra("token");
        if(!CallNotifications.matches(this,incoming) || !"playing".equals(CallNotifications.phase(this))) { stopSelf(); return START_NOT_STICKY; }
        if(player!=null) return START_NOT_STICKY;
        token=incoming; running=true; instance=this;
        held=CallScheduler.prefs(this).getBoolean("active_held",false);
        int contact=CallScheduler.prefs(this).getInt("active_contact",-1);
        startForeground(CallNotifications.ID,CallNotifications.playing(this,token,contact));
        // Telecom owns call audio focus, mode, earpiece/speaker/Bluetooth routing, and call volume.
        AudioAttributes attributes=attributes();
        try {
            player=new MediaPlayer(); player.setAudioAttributes(attributes);
            CallClips.configure(this,contact,player);
            player.setOnCompletionListener(p -> finishCall());
            player.setOnErrorListener((p,what,extra) -> { Toast.makeText(this,"Could not play this call recording",Toast.LENGTH_SHORT).show(); finishCall(); return true; });
            player.setOnPreparedListener(p -> { prepared=true; if(!held) p.start(); }); player.prepareAsync();
            handler.postDelayed(this::finishCall,300_000);
        } catch(Exception error) { Toast.makeText(this,"Could not play this call recording",Toast.LENGTH_SHORT).show(); finishCall(); }
        return START_NOT_STICKY;
    }
    static AudioAttributes attributes() { return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(); }
    static void hold(boolean held) {
        CallPlaybackService service=instance; if(service==null) return;
        service.held=held;
        if(service.player!=null && service.prepared) { if(held) service.player.pause(); else service.player.start(); }
    }
    private void finishCall() { CallNotifications.end(this,token,android.telecom.DisconnectCause.REMOTE); stopSelf(); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null); running=false; if(instance==this) instance=null;
        if(player!=null) { player.release(); player=null; }
        stopForeground(STOP_FOREGROUND_REMOVE);
        if(CallNotifications.matches(this,token)) CallNotifications.end(this,token);
        super.onDestroy();
    }
}
