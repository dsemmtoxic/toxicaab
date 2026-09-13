package com.toxic.search;

import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

final class FriendRecords {
    static void applyCurrentPresence(ArrayList<JSONObject> records, ArrayList<JSONObject> current) {
        HashMap<String, JSONObject> index = new HashMap<>();
        if (current != null) for (JSONObject record : current) {
            if (record == null) continue;
            String id = key(record, "uniqueId", "habboId", "id");
            String name = key(record, "name", "username", "habboName");
            if (!id.isEmpty()) index.put("id:" + id, record);
            if (!name.isEmpty()) index.put("name:" + name, record);
        }
        if (records == null) return;
        for (JSONObject record : records) {
            if (record == null) continue;
            JSONObject fresh = index.get("id:" + key(record, "uniqueId", "habboId", "id"));
            if (fresh == null) fresh = index.get("name:" + key(record, "name", "username", "habboName"));
            if (fresh == null) continue;
            PresenceRepository.State state = PresenceRepository.readState(fresh);
            try {
                record.remove("online");
                record.remove("isOnline");
                if (state != PresenceRepository.State.UNKNOWN) {
                    record.put("online", state == PresenceRepository.State.ONLINE);
                }
            } catch (Exception error) { AppDiagnostics.failure("friend_presence_merge", error); }
        }
    }

    private static String key(JSONObject value, String... keys) {
        for (String key : keys) {
            String text = value.optString(key, "").trim();
            if (!text.isEmpty() && !"null".equals(text)) return text.toLowerCase(Locale.ROOT);
        }
        return "";
    }
}
