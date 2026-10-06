package com.grabthesevehicles.app;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;

public final class CallActivity extends Activity {
    private String token;
    private TextView status;
    private Button answer;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable refresh=new Runnable() { public void run() {
        if(!CallNotifications.matches(CallActivity.this,token) || !CallNotifications.active(CallActivity.this)) { finish(); return; }
        boolean ringing="ringing".equals(CallNotifications.phase(CallActivity.this));
        status.setText(ringing?"Incoming call":"Connected · listening"); answer.setVisibility(ringing?View.VISIBLE:View.GONE);
        handler.postDelayed(this,250);
    }};
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); token=getIntent().getStringExtra("token");
        if(!CallNotifications.matches(this,token) || !CallNotifications.active(this)) { finish(); return; }
        if(Build.VERSION.SDK_INT>=27) { setShowWhenLocked(true); setTurnScreenOn(true); } else getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(Color.rgb(16,16,20)); getWindow().setNavigationBarColor(Color.rgb(16,16,20));
        int contact=CallScheduler.prefs(this).getInt("active_contact",0);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(40,80,40,80); root.setBackgroundColor(Color.rgb(16,16,20));
        root.setOnApplyWindowInsetsListener((v,insets)-> { v.setPadding(40,insets.getSystemWindowInsetTop()+40,40,insets.getSystemWindowInsetBottom()+40); return insets; });
        int icon=CallContacts.icon(contact);
        if(icon!=0) { ImageView portrait=new ImageView(this); portrait.setImageResource(icon); root.addView(portrait,new LinearLayout.LayoutParams(dp(140),dp(140))); }
        TextView name=new TextView(this); name.setText(CallContacts.NAMES[contact]); name.setTextColor(Color.WHITE); name.setTextSize(30); name.setGravity(Gravity.CENTER); name.setPadding(0,32,0,24); root.addView(name);
        status=new TextView(this); status.setTextColor(Color.rgb(255,176,71)); status.setTextSize(19); status.setPadding(0,0,0,40); root.addView(status);
        TextView hint=new TextView(this); hint.setText("Simulated call · your microphone is off"); hint.setTextColor(Color.LTGRAY); hint.setPadding(0,0,0,40); root.addView(hint);
        answer=new Button(this); answer.setText("Answer"); answer.setOnClickListener(v -> pickUp()); root.addView(answer,new LinearLayout.LayoutParams(-1,dp(56)));
        Button decline=new Button(this); decline.setText("Hang up"); decline.setOnClickListener(v -> { CallNotifications.end(this,token); finish(); }); root.addView(decline,new LinearLayout.LayoutParams(-1,dp(56)));
        setContentView(root);
        if("answer".equals(getIntent().getAction())) pickUp();
    }
    private int dp(int n) { return Math.round(n*getResources().getDisplayMetrics().density); }
    private void pickUp() {
        if(!CallNotifications.answer(this,token)) return;
        try { startForegroundService(new Intent(this,CallPlaybackService.class).putExtra("token",token)); }
        catch(RuntimeException failure) { CallNotifications.end(this,token); Toast.makeText(this,"Could not start call playback",Toast.LENGTH_SHORT).show(); finish(); }
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); String incoming=intent.getStringExtra("token"); if(!java.util.Objects.equals(token,incoming)) { finish(); startActivity(intent); return; } if("answer".equals(intent.getAction())) pickUp(); }
    @Override protected void onResume() { super.onResume(); if(status!=null) refresh.run(); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }
    @Override public void onBackPressed() { CallNotifications.end(this,token); super.onBackPressed(); }
}
