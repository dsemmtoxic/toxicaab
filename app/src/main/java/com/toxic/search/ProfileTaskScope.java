package com.toxic.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/** Cancels queued and running work when the user leaves a profile. */
final class ProfileTaskScope extends AbstractExecutorService {
    private final ThreadPoolExecutor pool;
    private final Set<FutureTask<?>> tasks = new HashSet<>();

    ProfileTaskScope(int threads) {
        pool = new ThreadPoolExecutor(threads, threads, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        pool.allowCoreThreadTimeOut(true);
    }

    @Override public void execute(Runnable command) {
        FutureTask<Void> task = new FutureTask<Void>(command, null) {
            @Override protected void done() {
                synchronized (tasks) { tasks.remove(this); }
                // Wake callers awaiting a submitted task that never got to start.
                if (isCancelled() && command instanceof java.util.concurrent.Future<?>) {
                    ((java.util.concurrent.Future<?>) command).cancel(true);
                }
            }
        };
        synchronized (tasks) {
            tasks.add(task);
            try { pool.execute(task); }
            catch (RuntimeException error) { task.cancel(false); throw error; }
        }
    }

    void cancelPending() {
        synchronized (tasks) {
            for (FutureTask<?> task : new ArrayList<>(tasks)) task.cancel(true);
            pool.purge();
        }
    }

    @Override public void shutdown() { pool.shutdown(); }
    @Override public List<Runnable> shutdownNow() { cancelPending(); return pool.shutdownNow(); }
    @Override public boolean isShutdown() { return pool.isShutdown(); }
    @Override public boolean isTerminated() { return pool.isTerminated(); }
    @Override public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return pool.awaitTermination(timeout, unit);
    }
}
