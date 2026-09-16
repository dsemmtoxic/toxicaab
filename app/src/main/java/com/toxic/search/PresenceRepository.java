package com.toxic.search;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;

/** Current official presence, independent of historical friendship records. */
final class PresenceRepository {
    enum State { ONLINE, OFFLINE, UNKNOWN }

    interface Transport { String get(String address) throws Exception; }
    private final Transport transport;

    PresenceRepository() {
        this(address -> JsonHttpClient.get(address, 4_000, 5_000));
    }

    PresenceRepository(Transport transport) { this.transport = transport; }

    static final class Result {
        final State state;
        final JSONObject profile;
        Result(State state, JSONObject profile) { this.state = state; this.profile = profile; }
    }

    Result fetch(String hotel, String uniqueId, String nick) {
        try {
            String reference = uniqueId == null ? "" : uniqueId.trim();
            String path = reference.isEmpty()
                    ? "?name=" + URLEncoder.encode(nick == null ? "" : nick.trim(), "UTF-8")
                    : "/" + URLEncoder.encode(reference, "UTF-8");
            JSONObject root = new JSONObject(transport.get(
                    "https://" + domain(hotel) + "/api/public/users" + path));
            JSONObject profile = root.optJSONObject("user");
            if (profile == null) profile = root;
            String actualId = profile.optString("uniqueId", "");
            if (actualId.isEmpty() || (!reference.isEmpty() && !reference.equalsIgnoreCase(actualId))) {
                return new Result(State.UNKNOWN, null);
            }
            return new Result(readState(profile), profile);
        } catch (Exception error) {
            AppDiagnostics.failure("presence", error);
            return new Result(State.UNKNOWN, null);
        }
    }

    /** One fresh list can report presence for friends whose individual profile is private. */
    FriendsSnapshot fetchFriends(String hotel, String ownerId) {
        if (ownerId == null || ownerId.trim().isEmpty()) return new FriendsSnapshot(null);
        try {
            Object payload = new JSONTokener(transport.get("https://" + domain(hotel)
                    + "/api/public/users/" + URLEncoder.encode(ownerId.trim(), "UTF-8")
                    + "/friends")).nextValue();
            return new FriendsSnapshot(friendsArray(payload, 0));
        } catch (Exception error) {
            AppDiagnostics.failure("friends_presence", error);
            return new FriendsSnapshot(null);
        }
    }

    private static JSONArray friendsArray(Object payload, int depth) {
        if (payload instanceof JSONArray) return (JSONArray) payload;
        if (!(payload instanceof JSONObject) || depth > 3) return null;
        JSONObject object = (JSONObject) payload;
        for (String key : new String[]{"friends", "data", "items", "results"}) {
            JSONArray result = friendsArray(object.opt(key), depth + 1);
            if (result != null) return result;
        }
        return null;
    }

    static final class FriendsSnapshot {
        private final HashMap<String, FriendState> byId = new HashMap<>();
        private final HashMap<String, FriendState> byName = new HashMap<>();

        FriendsSnapshot(JSONArray records) {
            if (records == null) return;
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.optJSONObject(i);
                if (record == null) continue;
                FriendState friend = new FriendState(identity(record, "uniqueId", "habboId", "id"),
                        readState(record));
                String name = identity(record, "name", "username", "habboName");
                if (!friend.id.isEmpty()) byId.put(friend.id, friend);
                if (!name.isEmpty()) byName.put(name, friend);
            }
        }

        State stateFor(String id, String nick) {
            String key = normalized(id);
            FriendState friend = key.isEmpty() ? null : byId.get(key);
            if (friend != null) return friend.state;
            friend = byName.get(normalized(nick));
            // A reused name must not transfer presence to a different known identity.
            if (friend == null || (!key.isEmpty() && !friend.id.isEmpty())) return State.UNKNOWN;
            return friend.state;
        }
    }

    private static final class FriendState {
        final String id;
        final State state;
        FriendState(String id, State state) { this.id = id; this.state = state; }
    }

    private static String identity(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = normalized(object.optString(key, ""));
            if (!value.isEmpty()) return value;
        }
        return "";
    }

    private static String normalized(String value) {
        String clean = value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
        return "null".equals(clean) ? "" : clean;
    }

    static State readState(JSONObject profile) {
        if (profile == null) return State.UNKNOWN;
        for (String key : new String[]{"online", "isOnline"}) {
            Object value = profile.opt(key);
            if (Boolean.TRUE.equals(value) || "true".equalsIgnoreCase(String.valueOf(value))
                    || "1".equals(String.valueOf(value))) return State.ONLINE;
            if (Boolean.FALSE.equals(value) || "false".equalsIgnoreCase(String.valueOf(value))
                    || "0".equals(String.valueOf(value))) return State.OFFLINE;
        }
        return State.UNKNOWN;
    }

    static String domain(String hotel) {
        String h = hotel == null ? "br" : hotel.toLowerCase(Locale.ROOT);
        if ("com".equals(h) || "us".equals(h)) return "www.habbo.com";
        if ("tr".equals(h)) return "www.habbo.com.tr";
        if ("br".equals(h)) return "www.habbo.com.br";
        for (String value : new String[]{"es", "de", "fr", "fi", "it", "nl"}) {
            if (value.equals(h)) return "www.habbo." + h;
        }
        return "www.habbo.com.br";
    }
}
