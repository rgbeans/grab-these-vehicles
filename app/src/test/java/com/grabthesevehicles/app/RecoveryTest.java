package com.grabthesevehicles.app;

import static org.junit.Assert.*;
import android.Manifest;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Intent;
import android.os.SystemClock;
import android.provider.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk={26,36})
public class RecoveryTest {
    private Application app;
    private AlarmManager alarms;
    @Before public void setUp() {
        app=RuntimeEnvironment.getApplication();
        alarms=app.getSystemService(AlarmManager.class);
        RequestScheduler.prefs(app).edit().clear().commit();
        Shadows.shadowOf(app).grantPermissions(Manifest.permission.POST_NOTIFICATIONS);
    }

    @Test public void reopeningRepairsMissingAlarmWithoutChangingItsDeadline() {
        RequestScheduler.setEnabled(app,true);
        long deadline=RequestScheduler.prefs(app).getLong("next_elapsed",0);
        removeAlarmOnly();
        try(ActivityController<MainActivity> activity=Robolectric.buildActivity(MainActivity.class).setup()) {
            assertEquals(deadline,RequestScheduler.prefs(app).getLong("next_elapsed",0));
            assertEquals(1,Shadows.shadowOf(alarms).getScheduledAlarms().size());
            assertEquals(AlarmManager.ELAPSED_REALTIME_WAKEUP,Shadows.shadowOf(alarms).getScheduledAlarms().get(0).type);
            assertEquals(deadline,Shadows.shadowOf(alarms).getScheduledAlarms().get(0).triggerAtTime);
        }
    }

    @Test public void recoveryDoesNotPostponeAnOverdueRequest() {
        RequestScheduler.setEnabled(app,true);
        removeAlarmOnly();
        org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMinutes(2));
        long past=SystemClock.elapsedRealtime()-60_000;
        assertTrue(past>0);
        RequestScheduler.prefs(app).edit().putLong("next_elapsed",past).commit();
        RequestScheduler.ensureScheduled(app);
        assertEquals(past,Shadows.shadowOf(alarms).getScheduledAlarms().get(0).triggerAtTime);
        assertEquals(past,RequestScheduler.prefs(app).getLong("next_elapsed",0));
    }

    @Test public void phoneClockChangeKeepsTheElapsedDeadline() {
        RequestScheduler.setEnabled(app,true);
        long deadline=RequestScheduler.prefs(app).getLong("next_elapsed",0);
        RequestScheduler.prefs(app).edit().putLong("next_at",System.currentTimeMillis()+7_200_000).commit();
        new RestoreReceiver().onReceive(app,new Intent(Intent.ACTION_TIME_CHANGED));
        assertEquals(deadline,RequestScheduler.prefs(app).getLong("next_elapsed",0));
        long remaining=RequestScheduler.nextWallTime(app)-System.currentTimeMillis();
        assertTrue(remaining>=1_795_000 && remaining<=3_600_000);
        assertEquals(AlarmManager.ELAPSED_REALTIME_WAKEUP,Shadows.shadowOf(alarms).getScheduledAlarms().get(0).type);
    }

    @Test public void persistedBackgroundJobRepairsTheAlarmWithoutOpeningTheApp() {
        RequestScheduler.setEnabled(app,true);
        JobInfo job=app.getSystemService(JobScheduler.class).getPendingJob(RequestScheduler.RECOVERY_JOB);
        assertNotNull(job);
        assertTrue(job.isPersisted());
        assertEquals(3_600_000,job.getIntervalMillis());
        removeAlarmOnly();
        ServiceController<RecoveryJobService> service=Robolectric.buildService(RecoveryJobService.class).create();
        try {
            assertFalse(service.get().onStartJob(null));
        } finally { service.destroy(); }
        assertFalse(Shadows.shadowOf(alarms).getScheduledAlarms().isEmpty());
    }

    @Test public void pausingCancelsTheAlarmAndRecoveryJob() {
        RequestScheduler.setEnabled(app,true);
        RequestScheduler.setEnabled(app,false);
        assertTrue(Shadows.shadowOf(alarms).getScheduledAlarms().isEmpty());
        assertNull(app.getSystemService(JobScheduler.class).getPendingJob(RequestScheduler.RECOVERY_JOB));
        RequestScheduler.ensureScheduled(app);
        assertTrue(Shadows.shadowOf(alarms).getScheduledAlarms().isEmpty());
    }

    @Test public void oldVersionScheduleAndRebootAreMigratedToCurrentUptime() {
        long wall=System.currentTimeMillis()+1_800_000;
        RequestScheduler.prefs(app).edit().putBoolean("enabled",true).putLong("next_at",wall).commit();
        RequestScheduler.ensureScheduled(app);
        assertTrue(Math.abs(wall-RequestScheduler.nextWallTime(app))<2000);
        Settings.Global.putInt(app.getContentResolver(),Settings.Global.BOOT_COUNT,RequestScheduler.bootCount(app)+1);
        RequestScheduler.ensureScheduled(app);
        assertEquals(RequestScheduler.bootCount(app),RequestScheduler.prefs(app).getInt("scheduled_boot",-1));
        assertTrue(Math.abs(wall-RequestScheduler.nextWallTime(app))<2000);
    }

    private void removeAlarmOnly() {
        alarms.cancel(PendingIntent.getBroadcast(app,10,new Intent(app,RequestReceiver.class).setAction(RequestScheduler.ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE));
        assertTrue(Shadows.shadowOf(alarms).getScheduledAlarms().isEmpty());
    }
}
