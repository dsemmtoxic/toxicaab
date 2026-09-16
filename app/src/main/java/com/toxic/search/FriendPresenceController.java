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
    private final PresenceRepository repository;
    private final LinkedHashMap<String, Entry> entries = new LinkedHashMap<>();
    private boolean foreground, closed;
    private int generation;
    private String pageKey = "";
    private String friendsHotel = "", friendsOwnerId = "";
    private boolean friendsInFlight;
    private long friendsCheckedAt;
    private PresenceRepository.FriendsSnapshot currentFriends;

    FriendPresenceController() { this(new PresenceRepository()); }
    FriendPresenceController(PresenceRepository repository) { this.repository = repository; }

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
        beginPage(key, "", "");
    }

    void beginPage(String key, String hotel, String ownerId) {
        String cleanOwnerId = ownerId == null ? "" : ownerId.trim();
        if (!pageKey.equals(key) || !friendsHotel.equals(hotel) || !friendsOwnerId.equals(cleanOwnerId)) {
            generation++;
            workers.cancelPending();
            entries.clear();
            pageKey = key;
            friendsHotel = hotel;
            friendsOwnerId = cleanOwnerId;
            resetFriends();
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
        friendsInFlight = false;
        for (Entry entry : entries.values()) entry.inFlight = false;
    }

    void refreshNow() {
        generation++;
        workers.cancelPending();
        resetFriends();
        for (Entry entry : entries.values()) {
            entry.inFlight = false;
            entry.checkedAt = 0L;
            entry.state = PresenceRepository.State.UNKNOWN;
            if (entry.listener != null) entry.listener.changed(entry.state);
        }
        requestSoon();
    }

    private void resetFriends() {
        friendsInFlight = false;
        friendsCheckedAt = 0L;
        currentFriends = null;
    }

    void requestSoon() {
        if (!foreground || closed) return;
        main.removeCallbacks(tick);
        main.postDelayed(tick, 180L);
    }

    private void refreshVisible() {
        final int requestGeneration = generation;
        long now = SystemClock.elapsedRealtime();
        ArrayList<Entry> due = new ArrayList<>();
        for (Entry entry : new ArrayList<>(entries.values())) {
            if (entry.card == null || !entry.card.isAttachedToWindow() || !entry.card.isShown()
                    || !entry.card.getGlobalVisibleRect(new Rect()) || entry.inFlight
                    || (entry.checkedAt > 0 && now - entry.checkedAt < INTERVAL_MS)) continue;
            due.add(entry);
            if (entry.state != PresenceRepository.State.UNKNOWN) {
                entry.state = PresenceRepository.State.UNKNOWN;
                if (entry.listener != null) entry.listener.changed(entry.state);
            }
        }
        if (due.isEmpty()) return;

        // The current owner's official list is authoritative for the Friends tab.
        // Share one request across visible cards instead of querying each private profile.
        if (!friendsOwnerId.isEmpty()
                && (friendsCheckedAt == 0L || now - friendsCheckedAt >= INTERVAL_MS)) {
            if (!friendsInFlight) {
                friendsInFlight = true;
                final String hotel = friendsHotel, ownerId = friendsOwnerId;
                workers.execute(() -> {
                    PresenceRepository.FriendsSnapshot result = repository.fetchFriends(hotel, ownerId);
                    main.post(() -> {
                        if (closed || !foreground || generation != requestGeneration) return;
                        friendsInFlight = false;
                        friendsCheckedAt = SystemClock.elapsedRealtime();
                        currentFriends = result;
                        refreshVisible();
                    });
                });
            }
            return;
        }

        for (Entry entry : due) {
            PresenceRepository.State listed = currentFriends == null
                    ? PresenceRepository.State.UNKNOWN : currentFriends.stateFor(entry.id, entry.nick);
            if (listed != PresenceRepository.State.UNKNOWN) {
                entry.state = listed;
                entry.checkedAt = friendsCheckedAt;
                if (entry.listener != null) entry.listener.changed(listed);
                continue;
            }
            // Removed friends and entries without a current list status use the public profile.
            entry.inFlight = true;
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
