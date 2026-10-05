package com.grabthesevehicles.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import java.util.Random;

public final class RequestScheduler {
    public static final String ACTION = "com.grabthesevehicles.app.DELIVER_REQUEST";
    static final int RECOVERY_JOB = 701;
    private static final Random RANDOM = new Random();
    private RequestScheduler() {}

    public static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences("simeon", Context.MODE_PRIVATE);
    }
    public static boolean enabled(Context c) { return prefs(c).getBoolean("enabled", false); }
    public static boolean exactAllowed(Context c) {
        return Build.VERSION.SDK_INT < 31 || c.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }
    private static PendingIntent alarmIntent(Context c) {
        return PendingIntent.getBroadcast(c, 10, new Intent(c, RequestReceiver.class).setAction(ACTION),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    public static void cancel(Context c) {
        c.getSystemService(AlarmManager.class).cancel(alarmIntent(c));
        c.getSystemService(JobScheduler.class).cancel(RECOVERY_JOB);
        prefs(c).edit().remove("next_at").remove("next_elapsed").remove("scheduled_boot")
            .remove("scheduled_exact").commit();
    }
    public static void setEnabled(Context c, boolean enabled) {
        prefs(c).edit().putBoolean("enabled", enabled).commit();
        if (enabled) scheduleNew(c); else cancel(c);
    }
    public static void scheduleNew(Context c) {
        if (!enabled(c)) { cancel(c); return; }
        SharedPreferences p = prefs(c);
        int min = p.getInt("min", IntervalPolicy.DEFAULT_MIN), max = p.getInt("max", IntervalPolicy.DEFAULT_MAX);
        if (!IntervalPolicy.valid(min, max)) { min = IntervalPolicy.DEFAULT_MIN; max = IntervalPolicy.DEFAULT_MAX; }
        scheduleAtElapsed(c, SystemClock.elapsedRealtime() + IntervalPolicy.delayMillis(min, max, RANDOM));
    }
    public static void restore(Context c) {
        ensureScheduled(c);
    }
    public static void restoreAfterBoot(Context c) {
        if (!enabled(c)) { cancel(c); return; }
        // Uptime starts again after a reboot. Preserve a future request time or draw a new wait.
        long remaining = prefs(c).getLong("next_at", 0) - System.currentTimeMillis();
        if (remaining <= 0) scheduleNew(c);
        else scheduleAtElapsed(c, SystemClock.elapsedRealtime() + Math.min(remaining, 86_400_000L));
    }
    public static void ensureScheduled(Context c) {
        if (!enabled(c)) { cancel(c); return; }
        SharedPreferences p = prefs(c);
        long elapsed = p.getLong("next_elapsed", 0);
        if (elapsed > 0 && p.getInt("scheduled_boot", -1) == bootCount(c)) {
            // Re-register the same deadline even if Android removed the alarm. Past deadlines
            // run as soon as allowed; reopening the app never postpones an overdue request.
            scheduleAtElapsed(c, elapsed);
        } else {
            // Upgrade from the old wall-clock schedule, or recover after an unobserved reboot.
            restoreAfterBoot(c);
        }
    }
    static int bootCount(Context c) {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.BOOT_COUNT, 0);
    }
    public static long nextWallTime(Context c) {
        SharedPreferences p = prefs(c);
        long elapsed = p.getLong("next_elapsed", 0);
        if (elapsed > 0 && p.getInt("scheduled_boot", -1) == bootCount(c)) {
            return System.currentTimeMillis() + elapsed - SystemClock.elapsedRealtime();
        }
        return p.getLong("next_at", 0);
    }
    private static void scheduleAtElapsed(Context c, long elapsed) {
        AlarmManager manager = c.getSystemService(AlarmManager.class);
        PendingIntent pending = alarmIntent(c);
        boolean exact = exactAllowed(c);
        if (exact) {
            try { manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsed, pending); }
            catch (SecurityException denied) { exact = false; }
        }
        if (!exact) manager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, elapsed, pending);
        prefs(c).edit().putLong("next_elapsed", elapsed).putInt("scheduled_boot", bootCount(c))
            .putLong("next_at", System.currentTimeMillis() + elapsed - SystemClock.elapsedRealtime())
            .putBoolean("scheduled_exact", exact).commit();
        JobScheduler jobs = c.getSystemService(JobScheduler.class);
        if (jobs.getPendingJob(RECOVERY_JOB) == null) {
            jobs.schedule(new JobInfo.Builder(RECOVERY_JOB, new ComponentName(c, RecoveryJobService.class))
                .setPeriodic(3_600_000L).setPersisted(true).build());
        }
    }
    public static String nextMessage(Context c) {
        SharedPreferences p = prefs(c);
        String stored = p.getString("bag", "");
        int last = p.getInt("last_index", -1);
        if (stored.isEmpty()) {
            StringBuilder bag = new StringBuilder();
            for (int i : IntervalPolicy.freshBag(VehicleMessages.count(), last, RANDOM)) {
                if (bag.length() > 0) bag.append(',');
                bag.append(i);
            }
            stored = bag.toString();
        }
        String[] parts = stored.split(",", 2);
        int index = Integer.parseInt(parts[0]);
        String message = VehicleMessages.message(index);
        p.edit().putString("bag", parts.length > 1 ? parts[1] : "").putInt("last_index", index)
            .putString("last_message", message).putLong("last_at", System.currentTimeMillis()).commit();
        return message;
    }
}
