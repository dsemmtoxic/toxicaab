package com.toxic.search;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/** Small manually completed Future using APIs available on Android 6. */
final class SettableFuture<T> implements Future<T> {
    private final CountDownLatch ready = new CountDownLatch(1);
    private T value;
    private Throwable failure;
    private boolean cancelled;

    static <T> SettableFuture<T> completedFuture(T value) {
        SettableFuture<T> result = new SettableFuture<>();
        result.complete(value);
        return result;
    }

    synchronized boolean complete(T result) {
        if (isDone()) return false;
        value = result;
        ready.countDown();
        return true;
    }

    synchronized boolean completeExceptionally(Throwable error) {
        if (error == null) throw new NullPointerException("error");
        if (isDone()) return false;
        failure = error;
        ready.countDown();
        return true;
    }

    @Override public synchronized boolean cancel(boolean mayInterruptIfRunning) {
        if (isDone()) return false;
        cancelled = true;
        ready.countDown();
        return true;
    }

    @Override public synchronized boolean isCancelled() { return cancelled; }
    @Override public boolean isDone() { return ready.getCount() == 0; }
    synchronized boolean isCompletedExceptionally() { return failure != null || cancelled; }

    synchronized T getNow(T fallback) throws ExecutionException {
        return isDone() ? result() : fallback;
    }

    @Override public T get() throws InterruptedException, ExecutionException {
        ready.await();
        return result();
    }

    @Override public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        if (!ready.await(timeout, unit)) throw new TimeoutException("Request timed out");
        return result();
    }

    private synchronized T result() throws ExecutionException {
        if (cancelled) throw new CancellationException();
        if (failure != null) throw new ExecutionException(failure);
        return value;
    }
}
