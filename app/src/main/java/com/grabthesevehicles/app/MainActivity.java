package com.grabthesevehicles.app;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DateFormat;
import java.util.Date;

public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(16,16,20), CARD = Color.rgb(28,28,34);
    private static final int TEXT = Color.rgb(245,243,238), MUTED = Color.rgb(165,165,174), AMBER = Color.rgb(255,176,71);
    private Switch enabledSwitch;
    private EditText minInput, maxInput;
    private TextView deliveryStatus, requestBody, requestLabel, timingStatus, backgroundStatus, copyHint, requestSender;
    private LinearLayout messageCard;
    private ImageView requestAvatar;
    private Button notificationAccess, preciseAccess;
    private boolean updating, testAfterPermission;
    private final Handler handler = new Handler();
    private final Runnable refresh = new Runnable() {
        @Override public void run() { refreshState(); handler.postDelayed(this, 30_000); }
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        SimeonNotifications.createChannel(this);
        SharedPreferences prefs = RequestScheduler.prefs(this);
        boolean firstOpen = !prefs.contains("enabled");
        if (firstOpen) prefs.edit().putBoolean("enabled", true).commit();
        buildScreen();
        if (firstOpen && Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 20);
        }
    }
    @Override protected void onResume() {
        super.onResume();
        RequestScheduler.ensureScheduled(this);
        if (testAfterPermission && SimeonNotifications.allowed(this)) {
            testAfterPermission = false;
            sendTest();
        }
        handler.removeCallbacks(refresh);
        refresh.run();
    }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private TextView text(String value, float size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value); v.setTextSize(size); v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        v.setIncludeFontPadding(false);
        return v;
    }
    private GradientDrawable surface(int color, int radius, boolean border) {
        GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if (border) d.setStroke(dp(1), Color.rgb(51,51,59));
        return d;
    }
    private LinearLayout column() { LinearLayout v = new LinearLayout(this); v.setOrientation(LinearLayout.VERTICAL); return v; }
    private LinearLayout.LayoutParams params(int width, int height) { return new LinearLayout.LayoutParams(width < 0 ? width : dp(width), height < 0 ? height : dp(height)); }
    private void space(LinearLayout parent, int height) { parent.addView(new View(this), params(-1,height)); }
    private Button button(String label, boolean primary) {
        Button b = new Button(this); b.setText(label); b.setTextSize(15); b.setAllCaps(false);
        b.setTextColor(primary ? BG : AMBER); b.setMinHeight(dp(52));
        b.setPadding(dp(12),dp(10),dp(12),dp(10)); b.setStateListAnimator(null);
        b.setBackground(surface(primary ? AMBER : CARD,12,!primary));
        return b;
    }

    private void buildScreen() {
        getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG);
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(BG); scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                view.setPadding(bars.left,bars.top,bars.right,bars.bottom);
            } else view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        LinearLayout root = column(); root.setPadding(dp(24),dp(24),dp(24),dp(28));
        scroll.addView(root,new ScrollView.LayoutParams(-1,-2));
        TextView eyebrow = text("LOS SANTOS CONTACTS",11,AMBER,true); eyebrow.setLetterSpacing(0.12f); root.addView(eyebrow);
        space(root,10);
        root.addView(text("grab these vehicles",30,TEXT,true));
        space(root,8); root.addView(text("Your contacts have a job for you. Again.",14,MUTED,false));
        space(root,24);

        LinearLayout contact = new LinearLayout(this); contact.setGravity(Gravity.CENTER_VERTICAL);
        contact.setPadding(dp(16),dp(18),dp(16),dp(18)); contact.setBackground(surface(CARD,16,true));
        ImageView avatar = new ImageView(this); avatar.setImageResource(R.drawable.contact_simeon);
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP); avatar.setContentDescription("Simeon Yetarian");
        avatar.setBackground(surface(CARD,32,false)); avatar.setClipToOutline(true);
        contact.addView(avatar,params(56,56));
        LinearLayout name = column(); name.setPadding(dp(14),0,dp(8),0);
        name.addView(text("GTA Online contacts",21,TEXT,true)); space(name,6);
        name.addView(text("Scheduled messages",13,MUTED,false));
        contact.addView(name,new LinearLayout.LayoutParams(0,-2,1));
        enabledSwitch = new Switch(this); enabledSwitch.setContentDescription("Enable scheduled notifications");
        enabledSwitch.setThumbTintList(ColorStateList.valueOf(AMBER));
        enabledSwitch.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{Color.rgb(120,82,34),Color.rgb(65,65,73)}));
        contact.addView(enabledSwitch,params(-2,48)); root.addView(contact,params(-1,-2));
        enabledSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (updating) return;
            RequestScheduler.setEnabled(this,checked);
            if (checked && !SimeonNotifications.allowed(this)) askNotificationAccess(false);
            refreshState();
        });
        space(root,12);
        deliveryStatus = text("",13,MUTED,false); root.addView(deliveryStatus);
        space(root,24);
        root.addView(text("Contacts",20,TEXT,true));
        space(root,8);
        root.addView(text("Choose who can message you. Purchase reminders and heist-ready notices use your chosen interval.",13,MUTED,false));
        for (int i = 0; i < ContactMessages.IDS.length; i++) {
            final int contactIndex = i;
            space(root,10);
            Switch toggle = new Switch(this);
            toggle.setText(ContactMessages.NAMES[i] + "\n" + ContactMessages.DESCRIPTIONS[i]);
            toggle.setTextColor(TEXT); toggle.setTextSize(14); toggle.setPadding(dp(14),dp(12),dp(14),dp(12));
            toggle.setBackground(surface(CARD,12,true));
            toggle.setChecked(RequestScheduler.contactEnabled(this,i));
            toggle.setOnCheckedChangeListener((view,checked) -> {
                RequestScheduler.prefs(this).edit().putBoolean("contact_" + ContactMessages.IDS[contactIndex], checked).commit();
                refreshState();
            });
            LinearLayout row = new LinearLayout(this); row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10),0,0,0); row.setBackground(surface(CARD,12,true));
            int icon = ContactIcons.resource(i);
            if (icon != 0) {
                ImageView picture = new ImageView(this); picture.setImageResource(icon);
                picture.setContentDescription(ContactMessages.NAMES[i] + " phone icon");
                picture.setScaleType(ImageView.ScaleType.FIT_CENTER);
                row.addView(picture,params(40,40));
            }
            toggle.setBackgroundColor(Color.TRANSPARENT);
            row.addView(toggle,new LinearLayout.LayoutParams(0,-2,1));
            root.addView(row,params(-1,-2));
        }
        space(root,24);

        LinearLayout message = column(); messageCard = message; message.setPadding(dp(18),dp(18),dp(18),dp(18));
        message.setBackground(surface(Color.rgb(37,31,24),16,false));
        message.setForeground(new RippleDrawable(ColorStateList.valueOf(Color.argb(45,255,176,71)),null,surface(Color.WHITE,16,false)));
        message.setOnClickListener(v -> {
            if (RequestScheduler.prefs(this).getLong("last_at",0) > 0) MessageClipboard.copy(this,requestBody.getText());
        });
        requestLabel = text("NOTIFICATION PREVIEW",10,AMBER,true); requestLabel.setLetterSpacing(0.12f);
        message.addView(requestLabel); space(message,12);
        LinearLayout senderRow = new LinearLayout(this); senderRow.setGravity(Gravity.CENTER_VERTICAL);
        requestAvatar = new ImageView(this); requestAvatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        senderRow.addView(requestAvatar,params(40,40));
        requestSender = text("Simeon",16,TEXT,true); requestSender.setPadding(dp(12),0,0,0);
        senderRow.addView(requestSender,new LinearLayout.LayoutParams(0,-2,1));
        message.addView(senderRow); space(message,7);
        requestBody = text(VehicleMessages.preview(),15,TEXT,false); requestBody.setLineSpacing(dp(3),1);
        message.addView(requestBody); root.addView(message,params(-1,-2));
        space(root,10); copyHint = text("Your latest request will appear here.",12,MUTED,false); root.addView(copyHint);
        space(root,26);

        root.addView(text("How often?",20,TEXT,true));
        space(root,8); root.addView(text("A random wait between these two intervals.",13,MUTED,false));
        space(root,16);
        LinearLayout intervals = new LinearLayout(this); intervals.setGravity(Gravity.CENTER_VERTICAL);
        minInput = intervalField("Minimum interval, minutes",RequestScheduler.prefs(this).getInt("min",30));
        maxInput = intervalField("Maximum interval, minutes",RequestScheduler.prefs(this).getInt("max",60));
        LinearLayout minColumn = column(); minColumn.addView(text("MINIMUM",10,MUTED,true)); space(minColumn,8); minColumn.addView(minInput,params(-1,56));
        LinearLayout maxColumn = column(); maxColumn.addView(text("MAXIMUM",10,MUTED,true)); space(maxColumn,8); maxColumn.addView(maxInput,params(-1,56));
        intervals.addView(minColumn,new LinearLayout.LayoutParams(0,-2,1));
        TextView dash = text("–",22,MUTED,false); dash.setGravity(Gravity.CENTER); intervals.addView(dash,params(32,64));
        intervals.addView(maxColumn,new LinearLayout.LayoutParams(0,-2,1)); root.addView(intervals);
        space(root,8); root.addView(text("Minutes · 15 to 1,440 (24 hours)",12,MUTED,false));
        space(root,18);
        Button save = button("Save intervals",true); save.setOnClickListener(v -> saveIntervals()); root.addView(save,params(-1,-2));
        space(root,10);
        Button test = button("Send a test notification",false);
        test.setOnClickListener(v -> { if (SimeonNotifications.allowed(this)) chooseTestContact(); else askNotificationAccess(true); });
        root.addView(test,params(-1,-2));
        space(root,10);
        Button sound = button("Sound & notification settings",false);
        sound.setOnClickListener(v -> launchSettings(new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()).putExtra(Settings.EXTRA_CHANNEL_ID,SimeonNotifications.CHANNEL)));
        root.addView(sound,params(-1,-2));
        space(root,22);

        notificationAccess = button("Allow notifications",false); notificationAccess.setOnClickListener(v -> askNotificationAccess(false));
        root.addView(notificationAccess,params(-1,-2));
        timingStatus = text("",12,MUTED,false); timingStatus.setLineSpacing(dp(3),1); root.addView(timingStatus,params(-1,-2));
        space(root,10);
        preciseAccess = button("Improve timing",false); preciseAccess.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 31) launchSettings(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));
        }); root.addView(preciseAccess,params(-1,-2));
        space(root,14);
        backgroundStatus = text("",12,MUTED,false); backgroundStatus.setLineSpacing(dp(3),1); root.addView(backgroundStatus,params(-1,-2));
        space(root,10);
        if (Build.VERSION.SDK_INT >= 30) {
            Button unused = button("Unused-app settings",false);
            unused.setOnClickListener(v -> {
                new AlertDialog.Builder(this).setTitle("Keep contact messages running")
                    .setMessage("In App info, turn off “Pause app activity if unused” (or “Remove permissions if app isn't used”). This allows the app to keep working even when you don't open it for a long time.")
                    .setPositiveButton("Open settings",(dialog,which) -> startActivityForResult(
                        new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())),21))
                    .setNegativeButton("Cancel",null).show();
            });
            root.addView(unused,params(-1,-2)); space(root,10);
        }
        Button battery = button("Background & battery settings",false);
        battery.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Allow background activity")
            .setMessage("In your phone's app battery settings, allow background activity. On OnePlus, look under App battery management. If requests are delayed, choose Unrestricted or turn off battery optimization for this app. The available options depend on your phone.")
            .setPositiveButton("Open settings",(dialog,which) -> launchSettings(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))))
            .setNegativeButton("Cancel",null).show());
        root.addView(battery,params(-1,-2));
        space(root,20);
        TextView footer = text(ContactMessages.IDS.length + " contacts · texts & emails.\nHeist reminders, purchase offers & introductions.\nWorks offline. No live game connection.\nUnofficial fan app.",12,MUTED,false);
        footer.setGravity(Gravity.CENTER); footer.setLineSpacing(dp(4),1); root.addView(footer,params(-1,-2));
        space(root,10);
        Button privacy = button("Privacy",false);
        privacy.setOnClickListener(v -> new AlertDialog.Builder(this).setTitle("Privacy")
            .setMessage(AppPrivacy.TEXT).setPositiveButton("Close",null).show());
        root.addView(privacy,params(-1,-2));
        setContentView(scroll); scroll.requestApplyInsets();
    }
    private EditText intervalField(String description, int value) {
        EditText e = new EditText(this); e.setSingleLine(true); e.setText(String.valueOf(value));
        e.setInputType(InputType.TYPE_CLASS_NUMBER); e.setTextColor(TEXT); e.setTextSize(22);
        e.setPadding(dp(14),dp(8),dp(14),dp(8)); e.setContentDescription(description);
        e.setBackground(surface(CARD,12,true)); e.setSelectAllOnFocus(true); return e;
    }
    private void saveIntervals() {
        try {
            int min = Integer.parseInt(minInput.getText().toString().trim());
            int max = Integer.parseInt(maxInput.getText().toString().trim());
            minInput.setError(null); maxInput.setError(null);
            if (!IntervalPolicy.valid(min,max)) {
                if (min < 15 || min > 1440) minInput.setError("Use 15–1,440 minutes");
                if (max < 15 || max > 1440) maxInput.setError("Use 15–1,440 minutes");
                if (min > max) maxInput.setError("Must be at least the minimum");
                return;
            }
            RequestScheduler.prefs(this).edit().putInt("min",min).putInt("max",max).commit();
            if (RequestScheduler.enabled(this)) RequestScheduler.scheduleNew(this);
            View focused = getCurrentFocus();
            if (focused != null) { getSystemService(InputMethodManager.class).hideSoftInputFromWindow(focused.getWindowToken(),0); focused.clearFocus(); }
            Toast.makeText(this,"Intervals saved",Toast.LENGTH_SHORT).show(); refreshState();
        } catch (NumberFormatException bad) {
            Toast.makeText(this,"Enter a minimum and maximum in minutes",Toast.LENGTH_SHORT).show();
        }
    }
    private void sendTest() {
        chooseTestContact();
    }
    private void chooseTestContact() {
        new AlertDialog.Builder(this).setTitle("Who should contact you?")
            .setItems(ContactMessages.NAMES, (dialog,which) -> {
                if (SimeonNotifications.deliver(this,which)) Toast.makeText(this,ContactMessages.NAMES[which] + " sent you a message",Toast.LENGTH_SHORT).show();
                else Toast.makeText(this,"Enable notifications in Android settings",Toast.LENGTH_LONG).show();
                refreshState();
            }).setNegativeButton("Cancel",null).show();
    }
    private void askNotificationAccess(boolean thenTest) {
        testAfterPermission = thenTest;
        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (!RequestScheduler.prefs(this).getBoolean("asked_again",false) || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                RequestScheduler.prefs(this).edit().putBoolean("asked_again",true).commit();
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},20); return;
            }
        }
        Intent settings = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName());
        launchSettings(settings);
    }
    private void launchSettings(Intent intent) {
        try { startActivity(intent); }
        catch (ActivityNotFoundException unavailable) {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));
        }
    }
    @Override public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(code,permissions,results);
        if (code == 20) {
            boolean send = results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED && testAfterPermission;
            testAfterPermission = false;
            if (send) sendTest();
            refreshState();
        }
    }
    private void refreshState() {
        if (enabledSwitch == null) return;
        SharedPreferences p = RequestScheduler.prefs(this);
        boolean enabled = RequestScheduler.enabled(this), allowed = SimeonNotifications.allowed(this);
        updating = true; enabledSwitch.setChecked(enabled); updating = false;
        long next = RequestScheduler.nextWallTime(this), last = p.getLong("last_at",0);
        if (!enabled) deliveryStatus.setText("Paused · your contacts will wait.");
        else if (!RequestScheduler.hasContacts(this)) deliveryStatus.setText("No contacts selected. Enable a contact above.");
        else if (!allowed) deliveryStatus.setText("Notifications are blocked. Allow them below.");
        else if (next > System.currentTimeMillis()) deliveryStatus.setText("Active · next request around " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(next)));
        else deliveryStatus.setText("Active · waiting for Android to deliver the next request.");
        deliveryStatus.setTextColor(enabled && allowed ? Color.rgb(131,209,163) : MUTED);
        notificationAccess.setVisibility(allowed ? View.GONE : View.VISIBLE);
        notificationAccess.setPadding(dp(12),dp(10),dp(12),dp(10));
        requestLabel.setText(last == 0 ? "NOTIFICATION PREVIEW" : "LAST REQUEST · " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(last)));
        requestSender.setText(p.getString("last_sender","Simeon"));
        int contact = p.getInt("last_contact",0), icon = ContactIcons.resource(contact);
        requestAvatar.setVisibility(icon == 0 ? View.GONE : View.VISIBLE);
        if (icon != 0) requestAvatar.setImageResource(icon);
        requestAvatar.setContentDescription(requestSender.getText() + " phone icon");
        requestBody.setText(p.getString("last_message",VehicleMessages.preview()));
        boolean hasMessage = last > 0 && p.contains("last_message");
        messageCard.setClickable(hasMessage); messageCard.setFocusable(hasMessage);
        messageCard.setContentDescription(hasMessage ? "Copy last message: " + requestBody.getText() : null);
        copyHint.setText(hasMessage ? "Tap this card or a notification to copy the message." : "Your latest request will appear here.");
        boolean exact = RequestScheduler.exactAllowed(this);
        timingStatus.setText(exact
            ? "Timing access enabled. If your phone restricts background apps, allow background activity in battery settings."
            : "Android may delay requests while idle. Enable Alarms & reminders for more reliable intervals.");
        preciseAccess.setVisibility(exact ? View.GONE : View.VISIBLE);
        String unused = Build.VERSION.SDK_INT < 30 ? ""
            : getPackageManager().isAutoRevokeWhitelisted() ? "Unused-app permission reset is off.\n"
            : "For set-and-forget use, turn off unused-app pausing below.\n";
        boolean restricted = Build.VERSION.SDK_INT >= 28 && getSystemService(ActivityManager.class).isBackgroundRestricted();
        boolean optimized = !getSystemService(PowerManager.class).isIgnoringBatteryOptimizations(getPackageName());
        backgroundStatus.setText(unused + (restricted ? "Android is restricting background activity. " : "")
            + (optimized ? "Battery optimization is on. Check background settings if requests are delayed." : "Battery optimization is off for this app."));
    }
}
