package com.grabthesevehicles.app;

import android.app.job.JobParameters;
import android.app.job.JobService;

/** Deadline backups deliver overdue requests; hourly jobs repair missing schedules. */
public final class RecoveryJobService extends JobService {
    @Override public boolean onStartJob(JobParameters parameters) {
        recover(this,parameters==null?-1:parameters.getJobId());
        return false;
    }
    static void recover(android.content.Context context,int jobId) {
        if (jobId == RequestScheduler.DEADLINE_JOB) {
            if (RequestScheduler.enabled(context)) {
                context.getSystemService(android.app.job.JobScheduler.class).cancel(jobId);
                RequestScheduler.ensureScheduled(context);
                if (RequestScheduler.prefs(context).getLong("next_elapsed",Long.MAX_VALUE) <= android.os.SystemClock.elapsedRealtime())
                    new RequestReceiver().onReceive(context,new android.content.Intent(RequestScheduler.ACTION));
            }
        } else if (jobId == CallScheduler.DEADLINE_JOB) {
            if (CallScheduler.enabled(context)) {
                context.getSystemService(android.app.job.JobScheduler.class).cancel(jobId);
                CallScheduler.ensureScheduled(context);
                if (CallScheduler.prefs(context).getLong("next_elapsed",Long.MAX_VALUE) <= android.os.SystemClock.elapsedRealtime())
                    new CallReceiver().onReceive(context,new android.content.Intent(CallScheduler.ACTION));
            }
        } else {
            RequestScheduler.ensureScheduled(context);
            CallScheduler.ensureScheduled(context);
        }
    }
    @Override public boolean onStopJob(JobParameters parameters) { return true; }
}
