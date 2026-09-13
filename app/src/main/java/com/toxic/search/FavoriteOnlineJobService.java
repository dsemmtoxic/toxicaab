package com.toxic.search;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/** Background network checks are scheduled by Android and never run inside a receiver. */
public final class FavoriteOnlineJobService extends JobService {
    private static final int PERIODIC_JOB_ID = 2608;
    private static final int LEGACY_JOB_ID = 2609;
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private Future<?> running;
    private int generation;

    static void schedule(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null) return;
        for (JobInfo job : scheduler.getAllPendingJobs()) if (job.getId() == PERIODIC_JOB_ID) return;
        JobInfo job = new JobInfo.Builder(PERIODIC_JOB_ID,
                new ComponentName(context, FavoriteOnlineJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(15 * 60_000L)
                .setPersisted(true)
                .setBackoffCriteria(30_000L, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                .build();
        scheduler.schedule(job);
    }

    static void enqueue(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler == null || FavoriteRefreshCoordinator.foreground) return;
        scheduler.schedule(new JobInfo.Builder(LEGACY_JOB_ID,
                new ComponentName(context, FavoriteOnlineJobService.class))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1_000L).build());
    }

    static void cancel(Context context) {
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (scheduler != null) { scheduler.cancel(PERIODIC_JOB_ID); scheduler.cancel(LEGACY_JOB_ID); }
    }

    @Override public boolean onStartJob(JobParameters parameters) {
        if (FavoriteRefreshCoordinator.foreground || (running != null && !running.isDone())) return false;
        final int request = ++generation;
        running = worker.submit(() -> {
            if (!FavoriteRefreshCoordinator.checks.tryStart()) {
                new android.os.Handler(getMainLooper()).post(() -> {
                    if (request == generation) jobFinished(parameters, false);
                });
                return;
            }
            try { FavoriteNotifications.checkFavoritesInBackground(getApplicationContext()); }
            finally {
                FavoriteRefreshCoordinator.checks.finish();
                new android.os.Handler(getMainLooper()).post(() -> {
                    if (request == generation) jobFinished(parameters, false);
                });
            }
        });
        return true;
    }

    @Override public boolean onStopJob(JobParameters parameters) {
        generation++;
        if (running != null) running.cancel(true);
        return true;
    }

    @Override public void onDestroy() {
        generation++;
        worker.shutdownNow();
        super.onDestroy();
    }
}
