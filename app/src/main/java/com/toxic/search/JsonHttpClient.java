package com.toxic.search;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/** Fresh JSON transport. Provider throttling never becomes cached profile data. */
final class JsonHttpClient {
    private static final ConcurrentHashMap<String, Long> pausedUntil = new ConcurrentHashMap<>();
    private static final int MAX_RESPONSE_BYTES = 24 * 1024 * 1024;

    static final class HttpFailure extends IOException {
        final int status;
        HttpFailure(int status) { super("HTTP " + status); this.status = status; }
    }

    static String get(String address, int connectTimeout, int readTimeout) throws IOException {
        checkInterrupted();
        URL url = new URL(address);
        String host = url.getHost().toLowerCase(Locale.ROOT);
        long now = android.os.SystemClock.elapsedRealtime();
        Long until = pausedUntil.get(host);
        if (until != null && until > now) throw new HttpFailure(429);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            connection.setRequestProperty("Cache-Control", "no-cache, no-store, max-age=0");
            connection.setRequestProperty("Pragma", "no-cache");
            connection.setRequestProperty("User-Agent", "ToxicSearchTool/" + BuildConfig.VERSION_NAME + " Android");
            connection.setRequestProperty("X-Toxic-App", BuildConfig.VERSION_NAME);
            int code = connection.getResponseCode();
            checkInterrupted();
            if (code < 200 || code >= 300) {
                if (code == 429 || code == 503) {
                    long delay = retryAfterMillis(connection.getHeaderField("Retry-After"), System.currentTimeMillis());
                    pausedUntil.put(host, android.os.SystemClock.elapsedRealtime() + delay);
                }
                throw new HttpFailure(code);
            }
            try (InputStream input = connection.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[16 * 1024];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    checkInterrupted();
                    if (output.size() + count > MAX_RESPONSE_BYTES) throw new IOException("Response too large");
                    output.write(buffer, 0, count);
                }
                return output.toString("UTF-8");
            }
        } finally { connection.disconnect(); }
    }

    static long retryAfterMillis(String value, long now) {
        long delay = 30_000L;
        if (value != null) {
            try { delay = Math.max(1L, Math.min(300L, Long.parseLong(value.trim()))) * 1_000L; }
            catch (Exception notSeconds) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                    format.setTimeZone(TimeZone.getTimeZone("GMT"));
                    delay = format.parse(value).getTime() - now;
                } catch (Exception ignored) {}
            }
        }
        return Math.max(1_000L, Math.min(5 * 60_000L, delay));
    }

    private static void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("Request cancelled");
    }
}
