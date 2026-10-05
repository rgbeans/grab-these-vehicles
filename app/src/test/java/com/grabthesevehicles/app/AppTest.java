package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.provider.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=36)
public class AppTest {
    private Application app;
    @Before public void setUp() {
        app=RuntimeEnvironment.getApplication();
        RequestScheduler.prefs(app).edit().clear().commit();
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }

    @Test public void firstLaunchSchedulesDefaultInterval() {
        try(ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup()) {
            assertTrue(RequestScheduler.enabled(app));
            long delay=RequestScheduler.prefs(app).getLong("next_at",0)-System.currentTimeMillis();
            assertTrue(delay>=1_795_000 && delay<=3_600_000);
            assertNotNull(app.getSystemService(NotificationManager.class).getNotificationChannel(SimeonNotifications.CHANNEL));
            assertFalse(Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().isEmpty());
        }
    }

    @Test public void notificationTapCopiesItsExactMessage() {
        assertTrue(SimeonNotifications.deliver(app));
        Notification n=app.getSystemService(NotificationManager.class).getActiveNotifications()[0].getNotification();
        String body=n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
        assertTrue(body.startsWith("Grab these vehicles: "));
        assertEquals(5,body.split(", ").length);
        Intent tap=Shadows.shadowOf(n.contentIntent).getSavedIntent();
        assertEquals(CopyMessageActivity.class.getName(),tap.getComponent().getClassName());
        // A later message must not change the text carried by this tap intent.
        RequestScheduler.prefs(app).edit().putString("last_message","a different message").commit();
        try(ActivityController<CopyMessageActivity> controller=Robolectric.buildActivity(CopyMessageActivity.class,tap).setup()) {
            controller.get().onWindowFocusChanged(true);
            ClipboardManager clipboard=app.getSystemService(ClipboardManager.class);
            assertEquals(body,clipboard.getPrimaryClip().getItemAt(0).getText().toString());
            assertTrue(controller.get().isFinishing());
        }
    }

    @Test public void requestsRunAfterActivityClosesAndRestoreAfterReboot() {
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
        controller.pause().stop().destroy();
        new RequestReceiver().onReceive(app,new Intent(RequestScheduler.ACTION));
        assertEquals(1,app.getSystemService(NotificationManager.class).getActiveNotifications().length);
        long next=RequestScheduler.prefs(app).getLong("next_at",0);
        assertTrue(next>System.currentTimeMillis());
        AlarmManager alarms=app.getSystemService(AlarmManager.class);
        alarms.cancel(PendingIntent.getBroadcast(app,10,new Intent(app,RequestReceiver.class).setAction(RequestScheduler.ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        assertTrue(Shadows.shadowOf(alarms).getScheduledAlarms().isEmpty());
        new RestoreReceiver().onReceive(app,new Intent(Intent.ACTION_BOOT_COMPLETED));
        assertFalse(Shadows.shadowOf(alarms).getScheduledAlarms().isEmpty());
        assertEquals(next,RequestScheduler.prefs(app).getLong("next_at",0));
    }

    @Test public void pausedOrBlockedRequestsDoNotDeliver() {
        RequestScheduler.setEnabled(app,false);
        new RequestReceiver().onReceive(app,new Intent(RequestScheduler.ACTION));
        assertEquals(0,app.getSystemService(NotificationManager.class).getActiveNotifications().length);
        Shadows.shadowOf(app).denyPermissions(Manifest.permission.POST_NOTIFICATIONS);
        assertFalse(SimeonNotifications.deliver(app));
        assertFalse(RequestScheduler.prefs(app).contains("last_message"));
    }

    @Test public void gtaSoundChannelMigratesVersionOne() throws Exception {
        NotificationManager manager=app.getSystemService(NotificationManager.class);
        // Simulate the channel left by app version 1.0.
        NotificationChannel previous=new NotificationChannel("simeon_requests","Simeon",NotificationManager.IMPORTANCE_HIGH);
        previous.enableVibration(true);
        manager.createNotificationChannel(previous);
        SimeonNotifications.createChannel(app);
        NotificationChannel channel=manager.getNotificationChannel(SimeonNotifications.CHANNEL);
        assertNotEquals("simeon_requests",channel.getId());
        assertEquals(SimeonNotifications.soundUri(app),channel.getSound());
        assertEquals(AudioAttributes.USAGE_NOTIFICATION,channel.getAudioAttributes().getUsage());
        assertTrue(channel.shouldVibrate());
        try(java.io.InputStream clip=app.getResources().openRawResource(R.raw.gta_notification)) {
            assertTrue(clip.available()>1_000);
        }
        assertTrue(SimeonNotifications.deliver(app));
        assertEquals(SimeonNotifications.CHANNEL,manager.getActiveNotifications()[0].getNotification().getChannelId());
        SimeonNotifications.createChannel(app);
        assertEquals(SimeonNotifications.soundUri(app),manager.getNotificationChannel(SimeonNotifications.CHANNEL).getSound());
    }

    @Test public void soundUpdateReplacesVersionOnePointOneDefaultSoundChannel() {
        NotificationManager manager=app.getSystemService(NotificationManager.class);
        NotificationChannel previous=new NotificationChannel("simeon_requests_gta","Simeon · GTA Online",NotificationManager.IMPORTANCE_HIGH);
        previous.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION).build());
        previous.setVibrationPattern(new long[]{0,100,50,100});
        previous.enableVibration(false);
        previous.setShowBadge(false);
        manager.createNotificationChannel(previous);
        RequestScheduler.prefs(app).edit().putInt("min",45).putInt("max",90).commit();
        SimeonNotifications.createChannel(app);
        NotificationChannel repaired=manager.getNotificationChannel(SimeonNotifications.CHANNEL);
        assertNotEquals(previous.getId(),repaired.getId());
        assertEquals("content",repaired.getSound().getScheme());
        assertEquals(SoundProvider.SOUND_URI,repaired.getSound());
        assertEquals(NotificationManager.IMPORTANCE_HIGH,repaired.getImportance());
        assertFalse(repaired.shouldVibrate());
        assertArrayEquals(previous.getVibrationPattern(),repaired.getVibrationPattern());
        assertFalse(repaired.canShowBadge());
        assertEquals(45,RequestScheduler.prefs(app).getInt("min",0));
        assertEquals(90,RequestScheduler.prefs(app).getInt("max",0));
    }

    @Test public void soundUpdatePreservesPreviouslyDisabledNotifications() {
        NotificationManager manager=app.getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel("simeon_requests_gta","Simeon",NotificationManager.IMPORTANCE_NONE));
        SimeonNotifications.createChannel(app);
        assertEquals(NotificationManager.IMPORTANCE_NONE,manager.getNotificationChannel(SimeonNotifications.CHANNEL).getImportance());
        assertFalse(SimeonNotifications.deliver(app));
    }
}
