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
    private AudioFocusRequest focus;
    private String token;
    private final Handler handler=new Handler(Looper.getMainLooper());
    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        String incoming=intent==null?null:intent.getStringExtra("token");
        if(!CallNotifications.matches(this,incoming) || !"playing".equals(CallNotifications.phase(this))) { stopSelf(); return START_NOT_STICKY; }
        if(player!=null) return START_NOT_STICKY;
        token=incoming; running=true;
        int contact=CallScheduler.prefs(this).getInt("active_contact",-1);
        startForeground(CallNotifications.ID,CallNotifications.playing(this,token,contact));
        AudioAttributes attributes=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build();
        AudioManager audio=getSystemService(AudioManager.class);
        focus=new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener(change -> { if(change<0) finishCall(); },handler).build();
        if(audio.requestAudioFocus(focus)!=AudioManager.AUDIOFOCUS_REQUEST_GRANTED) { finishCall(); return START_NOT_STICKY; }
        try {
            player=new MediaPlayer(); player.setAudioAttributes(attributes);
            CallClips.configure(this,contact,player);
            player.setOnCompletionListener(p -> finishCall());
            player.setOnErrorListener((p,what,extra) -> { Toast.makeText(this,"Could not play this call recording",Toast.LENGTH_SHORT).show(); finishCall(); return true; });
            player.setOnPreparedListener(p -> p.start()); player.prepareAsync();
            handler.postDelayed(this::finishCall,300_000);
        } catch(Exception error) { Toast.makeText(this,"Could not play this call recording",Toast.LENGTH_SHORT).show(); finishCall(); }
        return START_NOT_STICKY;
    }
    private void finishCall() { CallNotifications.end(this,token); stopSelf(); }
    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null); running=false;
        if(player!=null) { player.release(); player=null; }
        if(focus!=null) getSystemService(AudioManager.class).abandonAudioFocusRequest(focus);
        stopForeground(STOP_FOREGROUND_REMOVE);
        if(CallNotifications.matches(this,token)) CallNotifications.end(this,token);
        super.onDestroy();
    }
}
