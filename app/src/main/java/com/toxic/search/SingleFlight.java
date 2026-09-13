package com.toxic.search;

import java.util.concurrent.atomic.AtomicBoolean;

/** Shared by foreground and scheduled checks; released only by its owner. */
final class SingleFlight {
    private final AtomicBoolean running = new AtomicBoolean();
    boolean tryStart() { return running.compareAndSet(false, true); }
    void finish() { running.set(false); }
    boolean isRunning() { return running.get(); }
}
