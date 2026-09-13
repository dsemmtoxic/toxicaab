package com.toxic.search;

import org.json.JSONObject;
import java.util.Locale;

/** Stable enum mapping independent of the interface language. */
final class GroupMetadata {
    static final String OWNER_MARKER = "__habbodex_group_owner";
    static final int UNKNOWN = 0, OPEN = 1, REQUEST = 2, CLOSED = 3;
    static final int MEMBER = 1, ADMIN = 2, OWNER = 3;

    static int access(JSONObject group) {
        String value = first(group, "accessType", "groupAccess", "joinType", "membershipType",
                "type", "groupType", "status").toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (value.contains("EXCLUSIVE") || value.contains("REQUEST") || value.contains("APPROVAL") || value.contains("LOCKED")) return REQUEST;
        if (value.contains("PRIVATE") || value.contains("CLOSED") || value.contains("CLOSE")) return CLOSED;
        if (value.contains("NORMAL") || value.contains("REGULAR") || value.contains("OPEN")
                || value.contains("FREE") || value.contains("PUBLIC")) return OPEN;
        return UNKNOWN;
    }

    static int role(JSONObject group) {
        if (group != null && group.optBoolean(OWNER_MARKER, false)) return OWNER;
        JSONObject membership = group == null ? null : group.optJSONObject("membership");
        if (membership == null && group != null) membership = group.optJSONObject("member");
        if (membership == null && group != null) membership = group.optJSONObject("userMembership");
        String role = (first(group, "memberRole", "membershipRole", "userRole", "role", "rank", "membershipStatus")
                + " " + first(membership, "memberRole", "membershipRole", "userRole", "role", "rank", "status")).toUpperCase(Locale.ROOT);
        String[] flags = {"isAdmin", "admin", "administrator", "isAdministrator", "groupAdmin", "userIsAdmin", "isGroupAdmin"};
        if (anyTrue(group, flags) || anyTrue(membership, flags) || role.contains("ADMIN")
                || role.contains("MODERATOR") || role.contains("STAFF")) return ADMIN;
        return MEMBER;
    }

    static boolean anyTrue(JSONObject object, String... keys) {
        if (object == null) return false;
        for (String key : keys) {
            Object raw = object.opt(key);
            if (Boolean.TRUE.equals(raw) || (raw instanceof Number && ((Number) raw).doubleValue() != 0d)) return true;
            String value = String.valueOf(raw).trim().toLowerCase(Locale.ROOT);
            if ("true".equals(value) || "1".equals(value) || "yes".equals(value) || "sim".equals(value)
                    || "owner".equals(value) || "creator".equals(value) || "founder".equals(value)) return true;
        }
        return false;
    }

    private static String first(JSONObject object, String... keys) {
        if (object != null) for (String key : keys) {
            String value = object.optString(key, "");
            if (!value.trim().isEmpty() && !"null".equals(value)) return value.trim();
        }
        return "";
    }
}
