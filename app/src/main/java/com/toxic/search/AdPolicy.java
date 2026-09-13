package com.toxic.search;

/** Shared, bounded retry rules; ad placements and display cooldown stay unchanged. */
final class AdPolicy {
    static long retryDelay(int failures, long base, int maxShift, long maximum) {
        int shift = Math.max(0, Math.min(maxShift, Math.max(1, failures) - 1));
        return Math.min(base << shift, maximum);
    }
    static boolean expired(long loadedAt, long now, long maximumAge) {
        return loadedAt > 0 && (now < loadedAt || now - loadedAt >= maximumAge);
    }
    private AdPolicy() {}
}
