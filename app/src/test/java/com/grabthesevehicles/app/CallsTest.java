package com.grabthesevehicles.app;
import static org.junit.Assert.*;
import android.Manifest;
import android.app.*;
import android.content.Intent;
import android.os.SystemClock;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={26,36})
public class CallsTest {
    private Application app;
    @Before public void setup() {
        app=RuntimeEnvironment.getApplication();
        RequestScheduler.prefs(app).edit().clear().commit();
        CallScheduler.prefs(app).edit().clear().commit();
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }
    @Test public void callsStartOffAndRemainOffWhenNotificationTimerRuns() {
        RequestScheduler.setEnabled(app,true); CallScheduler.ensureScheduled(app);
        assertFalse(CallScheduler.enabled(app));
        assertFalse(CallScheduler.prefs(app).contains("next_elapsed"));
        assertEquals(1,Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void timersAndPauseControlsAreIndependent() {
        RequestScheduler.setEnabled(app,true);
        long notification=RequestScheduler.prefs(app).getLong("next_elapsed",0);
        CallScheduler.prefs(app).edit().putInt("min",90).putInt("max",90).commit();
        CallScheduler.setEnabled(app,true);
        assertEquals(5_400_000,CallScheduler.prefs(app).getLong("next_elapsed",0)-SystemClock.elapsedRealtime());
        assertEquals(notification,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        assertEquals(2,Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
        CallScheduler.setEnabled(app,false);
        assertTrue(RequestScheduler.enabled(app));
        assertEquals(notification,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        assertEquals(1,Shadows.shadowOf(app.getSystemService(AlarmManager.class)).getScheduledAlarms().size());
    }
    @Test public void reopeningAndRecoveryPreserveBothDeadlines() {
        RequestScheduler.setEnabled(app,true); CallScheduler.setEnabled(app,true);
        long notification=RequestScheduler.prefs(app).getLong("next_elapsed",0),call=CallScheduler.prefs(app).getLong("next_elapsed",0);
        new RestoreReceiver().onReceive(app,new Intent(Intent.ACTION_TIME_CHANGED));
        assertEquals(notification,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        assertEquals(call,CallScheduler.prefs(app).getLong("next_elapsed",0));
        assertNotNull(app.getSystemService(android.app.job.JobScheduler.class).getPendingJob(CallScheduler.RECOVERY_JOB));
    }
    @Test public void bundledCallersMatchTheirOwnVoicesAndUseCustomRingtone() {
        assertEquals(8,CallClips.callers(app).size());
        assertFalse(CallClips.available(app,1)); // Warstock has no invented voice.
        assertTrue(CallNotifications.ring(app,3)); // Lester
        assertFalse(CallNotifications.ring(app,0)); // Only one call at a time.
        assertEquals(3,CallScheduler.prefs(app).getInt("active_contact",-1));
        NotificationManager manager=app.getSystemService(NotificationManager.class);
        assertEquals(SoundProvider.RINGTONE_URI,manager.getNotificationChannel(CallNotifications.CHANNEL).getSound());
        assertEquals(45_000,manager.getActiveNotifications()[0].getNotification().getTimeoutAfter());
        CallNotifications.end(app,null);
        assertEquals(0,manager.getActiveNotifications().length);
    }
    @Test public void notificationContactTogglesDoNotDisableCallers() {
        RequestScheduler.prefs(app).edit().putBoolean("contact_lester",false).commit();
        assertTrue(CallClips.callers(app).contains(3));
        CallScheduler.prefs(app).edit().putBoolean("contact_lester",false).commit();
        assertFalse(CallClips.callers(app).contains(3));
    }
    private void ringing(String token) {
        CallScheduler.prefs(app).edit().putString("active_token",token).putString("active_phase","ringing")
            .putInt("active_boot",CallScheduler.bootCount(app)).putLong("active_until",SystemClock.elapsedRealtime()+45_000).commit();
    }
    @Test public void onlyCurrentCallCanAnswerOrHangUp() {
        ringing("current");
        assertFalse(CallNotifications.answer(app,"old"));
        CallNotifications.end(app,"old"); assertTrue(CallNotifications.active(app));
        assertTrue(CallNotifications.answer(app,"current"));
        assertEquals("playing",CallNotifications.phase(app));
        assertFalse(CallNotifications.answer(app,"current"));
        CallNotifications.end(app,"current"); assertFalse(CallNotifications.active(app));
    }
    @Test public void expiredAndPreviousBootCallsCannotAnswer() {
        ringing("expired");
        CallScheduler.prefs(app).edit().putLong("active_until",SystemClock.elapsedRealtime()).commit();
        assertFalse(CallNotifications.answer(app,"expired"));
        ringing("oldboot"); CallScheduler.prefs(app).edit().putInt("active_boot",-1).commit();
        assertFalse(CallNotifications.answer(app,"oldboot"));
    }
    @Test public void ringtoneIsReadOnlyAndPrivateRecordingsAreNotExposed() throws Exception {
        assertEquals("audio/mpeg",app.getContentResolver().getType(SoundProvider.RINGTONE_URI));
        try(java.io.InputStream input=app.getContentResolver().openInputStream(SoundProvider.RINGTONE_URI)) {
            int bytes=0,n; byte[] buffer=new byte[4096]; while((n=input.read(buffer))!=-1) bytes+=n;
            assertEquals(350040,bytes);
        }
        for(android.net.Uri uri:new android.net.Uri[]{SoundProvider.RINGTONE_URI.buildUpon().appendQueryParameter("path","files/call-lester.audio").build(),android.net.Uri.parse("content://"+SoundProvider.AUTHORITY+"/call-lester.audio")}) {
            try(java.io.InputStream input=app.getContentResolver().openInputStream(uri)) { fail("Only bundled sounds are public"); }
            catch(java.io.FileNotFoundException expected) {}
        }
    }
}
