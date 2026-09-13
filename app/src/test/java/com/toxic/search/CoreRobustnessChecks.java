package com.toxic.search;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Also executable with a plain JDK, without the Android SDK. */
public final class CoreRobustnessChecks {
    private static int passed;
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) throws Exception {
        runAll();
        System.out.println("Passed " + passed + " core robustness checks");
    }

    static void runAll() throws Exception {
        passed = 0;
        completionHasOneWinner();
        waitUnblocksOnCompletion();
        timeoutDoesNotDestroyFuture();
        cancellationDoesNotBecomeSuccess();
        failureKeepsItsCause();
        simultaneousChecksHaveOneOwner();
        newProfileCancelsOldQueue();
        retriesStayBounded();
        backwardClockExpiresAds();
    }

    private static void completionHasOneWinner() throws Exception {
        SettableFuture<Integer> future = new SettableFuture<>();
        CountDownLatch ready = new CountDownLatch(24), start = new CountDownLatch(1), done = new CountDownLatch(24);
        AtomicInteger wins = new AtomicInteger();
        for (int i = 0; i < 24; i++) {
            final int value = i;
            new Thread(() -> {
                ready.countDown();
                try { start.await(); if (future.complete(value)) wins.incrementAndGet(); }
                catch (InterruptedException error) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            }).start();
        }
        require(ready.await(2, TimeUnit.SECONDS), "writers did not start");
        start.countDown();
        require(done.await(2, TimeUnit.SECONDS), "writers did not complete");
        require(wins.get() == 1 && future.get() >= 0, "completion must have exactly one winner");
        passed++;
    }

    private static void waitUnblocksOnCompletion() throws Exception {
        SettableFuture<String> future = new SettableFuture<>();
        require("pending".equals(future.getNow("pending")), "nonblocking read did not return the fallback");
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<String> waiting = executor.submit(() -> future.get());
            future.complete("ready");
            require("ready".equals(future.getNow("pending")), "nonblocking read lost the completed value");
            require("ready".equals(waiting.get(2, TimeUnit.SECONDS)), "waiting reader did not wake");
        } finally { executor.shutdownNow(); }
        passed++;
    }

    private static void timeoutDoesNotDestroyFuture() throws Exception {
        SettableFuture<String> future = new SettableFuture<>();
        try { future.get(1, TimeUnit.MILLISECONDS); throw new AssertionError("missing timeout"); }
        catch (TimeoutException expected) {}
        require(!future.isDone() && future.complete("later") && "later".equals(future.get()), "timeout settled the request");
        passed++;
    }

    private static void cancellationDoesNotBecomeSuccess() throws Exception {
        SettableFuture<String> future = new SettableFuture<>();
        require(future.cancel(true), "cancellation was rejected");
        require(!future.complete("stale"), "a cancelled request accepted stale data");
        try { future.get(); throw new AssertionError("missing cancellation"); }
        catch (CancellationException expected) {}
        try { future.getNow("incorrect"); throw new AssertionError("nonblocking read ignored cancellation"); }
        catch (CancellationException expected) {}
        passed++;
    }

    private static void failureKeepsItsCause() throws Exception {
        SettableFuture<String> future = new SettableFuture<>();
        Exception cause = new Exception("expected");
        future.completeExceptionally(cause);
        try { future.get(); throw new AssertionError("missing failure"); }
        catch (ExecutionException expected) { require(expected.getCause() == cause, "cause was lost"); }
        try { future.getNow("incorrect"); throw new AssertionError("nonblocking read ignored failure"); }
        catch (ExecutionException expected) { require(expected.getCause() == cause, "nonblocking read lost the cause"); }
        require(!future.complete("incorrect"), "failed request became successful");
        passed++;
    }

    private static void simultaneousChecksHaveOneOwner() throws Exception {
        SingleFlight flight = new SingleFlight();
        CountDownLatch start = new CountDownLatch(1), done = new CountDownLatch(32);
        AtomicInteger owners = new AtomicInteger();
        for (int i = 0; i < 32; i++) new Thread(() -> {
            try { start.await(); if (flight.tryStart()) owners.incrementAndGet(); }
            catch (InterruptedException error) { Thread.currentThread().interrupt(); }
            finally { done.countDown(); }
        }).start();
        start.countDown();
        require(done.await(2, TimeUnit.SECONDS), "checks did not complete");
        require(owners.get() == 1 && flight.isRunning(), "overlapping checks were admitted");
        flight.finish();
        require(flight.tryStart(), "later refresh cannot start");
        flight.finish();
        passed++;
    }

    private static void newProfileCancelsOldQueue() throws Exception {
        ProfileTaskScope scope = new ProfileTaskScope(1);
        CountDownLatch started = new CountDownLatch(1), hold = new CountDownLatch(1);
        AtomicInteger staleRuns = new AtomicInteger();
        try {
            Future<?> old = scope.submit(() -> { started.countDown(); hold.await(); return null; });
            require(started.await(2, TimeUnit.SECONDS), "old request did not start");
            Future<?> queued = scope.submit(() -> staleRuns.incrementAndGet());
            scope.cancelPending();
            require(old.isCancelled() && queued.isCancelled(), "old requests were not cancelled");
            require(scope.submit(() -> 42).get(2, TimeUnit.SECONDS) == 42, "new search was blocked");
            require(staleRuns.get() == 0, "a stale queued request executed");
        } finally { hold.countDown(); scope.shutdownNow(); }
        passed++;
    }

    private static void retriesStayBounded() {
        long previous = 0;
        for (int failures = 0; failures < 1_000; failures++) {
            long delay = AdPolicy.retryDelay(failures, 1_000L, 6, 60_000L);
            require(delay >= 1_000L && delay <= 60_000L && delay >= previous, "retry interval escaped bounds");
            previous = delay;
        }
        passed++;
    }

    private static void backwardClockExpiresAds() {
        require(AdPolicy.expired(10_000L, 9_000L, 60_000L), "clock rollback kept an old ad valid");
        require(!AdPolicy.expired(10_000L, 20_000L, 60_000L), "fresh ad expired");
        require(AdPolicy.expired(10_000L, 70_000L, 60_000L), "old ad did not expire");
        passed++;
    }
}
