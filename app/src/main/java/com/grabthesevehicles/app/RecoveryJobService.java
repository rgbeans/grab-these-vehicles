package com.grabthesevehicles.app;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** A batched, hourly fallback repairs the alarm without keeping a foreground service running. */
public final class RecoveryJobService extends JobService {
    @Override public boolean onStartJob(JobParameters parameters) {
        RequestScheduler.ensureScheduled(this);
        CallScheduler.ensureScheduled(this);
        return false;
    }
    @Override public boolean onStopJob(JobParameters parameters) { return true; }
}
