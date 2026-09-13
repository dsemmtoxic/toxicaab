package com.toxic.search;

import android.util.Log;

/** Logs failure categories without profile names, URLs or purchase tokens. */
final class AppDiagnostics {
    private AppDiagnostics() {}
    static void failure(String operation, Throwable error) {
        if (error instanceof InterruptedException
                || error instanceof java.util.concurrent.CancellationException) return;
        String category = error == null ? "unknown" : error.getClass().getSimpleName();
        if (error instanceof JsonHttpClient.HttpFailure) {
            category += " status=" + ((JsonHttpClient.HttpFailure) error).status;
        }
        Log.w("ToxicDiagnostics", operation + ": " + category);
    }
}
