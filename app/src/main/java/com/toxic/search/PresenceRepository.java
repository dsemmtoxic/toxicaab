package com.toxic.search;

import org.json.JSONObject;
import java.net.URLEncoder;
import java.util.Locale;

/** Presence is read from the current public profile, independently of historical lists. */
final class PresenceRepository {
    enum State { ONLINE, OFFLINE, UNKNOWN }

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
            JSONObject root = new JSONObject(JsonHttpClient.get(
                    "https://" + domain(hotel) + "/api/public/users" + path, 4_000, 5_000));
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
