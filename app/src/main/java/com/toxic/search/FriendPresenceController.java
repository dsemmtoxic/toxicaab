package com.toxic.search;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;

/** Only attached, visible cards request presence; the controller owns its workers. */
final class FriendPresenceController {
    interface Listener { void changed(PresenceRepository.State state); }
    private static final long INTERVAL_MS = 30_000L;
    private final Handler main = new Handler(Looper.getMainLooper());
    private final ProfileTaskScope workers = new ProfileTaskScope(3);
    private final PresenceRepository repository = new PresenceRepository();
    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();
    private boolean foreground, closed;
    private int generation;
    private String pageKey = "";
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (!foreground || closed) return;
            refreshVisible();
            main.postDelayed(this, 1_000L);
        }
    };

    private static final class Entry {
        final String hotel, id, nick;
        View card;
        Listener listener;
        boolean inFlight;
        long checkedAt;
        PresenceRepository.State state = PresenceRepository.State.UNKNOWN;
        Entry(String hotel, String id, String nick) { this.hotel = hotel; this.id = id; this.nick = nick; }
    }

    void beginPage(String key) {
        if (!pageKey.equals(key)) {
            generation++;
            workers.cancelPending();
            entries.clear();
            pageKey = key;
        } else {
            for (Entry entry : entries.values()) { entry.card = null; entry.listener = null; }
        }
    }

    void bind(View card, String hotel, String id, String nick, Listener listener) {
        if (closed) return;
        String key = hotel + ":" + (id.isEmpty() ? nick : id).toLowerCase(Locale.ROOT);
        Entry entry = entries.get(key);
        if (entry == null) {
            entry = new Entry(hotel, id, nick);
            entries.put(key, entry);
        }
        entry.card = card;
        entry.listener = listener;
        if (SystemClock.elapsedRealtime() - entry.checkedAt >= INTERVAL_MS) entry.state = PresenceRepository.State.UNKNOWN;
        listener.changed(entry.state);
        requestSoon();
    }

    void resume() {
        if (closed) return;
        foreground = true;
        refreshNow();
    }

    void pause() {
        foreground = false;
        main.removeCallbacks(tick);
        generation++;
        workers.cancelPending();
        for (Entry entry : entries.values()) entry.inFlight = false;
    }

    void refreshNow() {
        generation++;
        workers.cancelPending();
        for (Entry entry : entries.values()) {
            entry.inFlight = false;
            entry.checkedAt = 0L;
            entry.state = PresenceRepository.State.UNKNOWN;
            if (entry.listener != null) entry.listener.changed(entry.state);
        }
        requestSoon();
    }

    void requestSoon() {
        if (!foreground || closed) return;
        main.removeCallbacks(tick);
        main.postDelayed(tick, 180L);
    }

    private void refreshVisible() {
        final int requestGeneration = generation;
        long now = SystemClock.elapsedRealtime();
        for (Entry entry : new ArrayList<>(entries.values())) {
            if (entry.card == null || !entry.card.isAttachedToWindow() || !entry.card.isShown()
                    || !entry.card.getGlobalVisibleRect(new Rect()) || entry.inFlight
                    || (entry.checkedAt > 0 && now - entry.checkedAt < INTERVAL_MS)) continue;
            entry.inFlight = true;
            entry.state = PresenceRepository.State.UNKNOWN;
            if (entry.listener != null) entry.listener.changed(entry.state);
            workers.execute(() -> {
                PresenceRepository.Result result = repository.fetch(entry.hotel, entry.id, entry.nick);
                main.post(() -> {
                    if (closed || !foreground || generation != requestGeneration) return;
                    entry.inFlight = false;
                    entry.checkedAt = SystemClock.elapsedRealtime();
                    entry.state = result.state;
                    if (entry.listener != null) entry.listener.changed(result.state);
                });
            });
        }
    }

    void close() {
        pause();
        closed = true;
        entries.clear();
        workers.shutdownNow();
    }
}
