package com.toxic.search;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Decodes only the room photo collection, retaining the server's pagination. */
final class RoomPhotosPage {
    static final int PAGE_SIZE = 8;
    final ArrayList<JSONObject> items = new ArrayList<>();
    JSONObject room;
    int total, nextPage;
    boolean success;

    static boolean isResourceUrl(String url) {
        return url != null && url.matches("https://habbodex\\.com/api/v1/roominfo/[a-z]{2,3}/room/[1-9]\\d{0,11}/photos(?:\\?.*)?");
    }

    static boolean emptyHttpResult(String url, int status) {
        return (status == 404 || status == 204 || status == 410) && isResourceUrl(url);
    }

    static JSONObject emptyResult() {
        JSONObject result = new JSONObject();
        try { result.put("result", new JSONArray()).put("total", 0); } catch(Exception ignored) {}
        return result;
    }

    static RoomPhotosPage decode(Object response, int page, int limit, String roomId) {
        RoomPhotosPage out = new RoomPhotosPage();
        JSONObject root = response instanceof JSONObject ? (JSONObject) response : null;
        if (root != null && root.optInt("status", 200) >= 500) return out;
        // A valid JSON response with no collection is an empty album. HabboDex
        // also returns null results and messages for rooms it has not indexed.
        out.success = response instanceof JSONObject || response instanceof JSONArray || response == JSONObject.NULL;
        Collection collection = find(response, 0, false);
        if (collection == null) return out;
        out.success = true;
        out.room = findRoom(root, 0);
        JSONArray array = collection.items;
        for (int i = 0; i < array.length(); i++) {
            JSONObject photo = array.optJSONObject(i);
            if (photo == null) continue;
            String id = first(photo, "roomId", "room_id", "roomid");
            if (id.isEmpty()) id = first(photo.optJSONObject("room"), "id", "roomId", "room_id");
            if (!id.isEmpty() && !"0".equals(id) && !id.equals(roomId)) continue;
            try {
                JSONObject copy = new JSONObject(photo.toString());
                copy.put("roomId", roomId);
                out.items.add(copy);
            } catch (Exception ignored) {}
        }
        JSONObject scope = collection.container;
        JSONObject meta = scope == null ? null : scope.optJSONObject("pagination");
        if (meta == null && scope != null) meta = scope.optJSONObject("meta");
        if (meta == null && root != null) meta = root.optJSONObject("pagination");
        if (meta == null && root != null) meta = root.optJSONObject("meta");
        out.total = positive(meta, "total", "totalItems", "totalCount", "count");
        if (out.total == 0) out.total = positive(scope, "total", "totalItems", "totalCount", "totalPhotos", "photosCount", "photoCount", "photos_count");
        if (out.total == 0) out.total = positive(root, "totalPhotos", "photosCount", "photoCount", "photos_count");
        if (out.total == 0) out.total = positive(out.room, "totalPhotos", "photosCount", "photoCount", "photos_count");
        int next = next(scope);
        if (next <= page) next = next(meta);
        if (next <= page) next = next(root);
        int size = positive(meta, "limit", "perPage", "per_page", "pageSize");
        if (size == 0) size = positive(scope, "limit", "perPage", "per_page", "pageSize");
        if (size == 0) size = Math.max(1, array.length());
        int pages = Math.max(positive(meta, "totalPages", "lastPage", "last_page"), positive(scope, "totalPages", "pages"));
        boolean explicitEnd = Boolean.FALSE.equals(value(meta, "hasMore", "has_more"))
                || Boolean.FALSE.equals(value(scope, "hasMore", "has_more"))
                || hasNullNext(meta) || hasNullNext(scope);
        // This endpoint can report the number returned in "total". A full page
        // without pagination metadata still needs a request for the next page.
        boolean fullPageWithoutMetadata = meta == null && pages == 0
                && array.length() == limit && out.total == array.length();
        if (next <= page && !explicitEnd && array.length() > 0
                && (pages > page || (out.total > 0 && (long) page * size < out.total)
                || fullPageWithoutMetadata || (out.total == 0 && array.length() >= limit))) next = page + 1;
        out.nextPage = array.length() > 0 && next > page && !explicitEnd ? next : 0;
        return out;
    }

    static String url(String hotel, String roomId, int page) {
        if (hotel == null || !hotel.matches("[a-z]{2,3}") || roomId == null
                || !roomId.matches("[1-9]\\d{0,11}") || page < 1) throw new IllegalArgumentException("Invalid room photo request");
        return "https://habbodex.com/api/v1/roominfo/" + hotel + "/room/" + roomId
                + "/photos?page=" + page + "&limit=" + PAGE_SIZE;
    }

    private static final class Collection {
        final JSONArray items;
        final JSONObject container;
        Collection(JSONArray items, JSONObject container) { this.items = items; this.container = container; }
    }

    private static Collection find(Object raw, int depth, boolean insidePhotos) {
        if (depth > 6) return null;
        if (raw instanceof JSONArray) return new Collection((JSONArray) raw, null);
        if (!(raw instanceof JSONObject)) return null;
        JSONObject object = (JSONObject) raw;
        for (String key : new String[]{"photos", "roomPhotos", "photoList", "photo_list"}) {
            Object nested = object.opt(key);
            if (nested instanceof JSONArray) return new Collection((JSONArray) nested, object);
            Collection found = find(nested, depth + 1, true);
            if (found != null) return found;
        }
        for (String key : new String[]{"data", "result", "results", "payload", "room", "items"}) {
            Object nested = object.opt(key);
            if (nested instanceof JSONArray && (insidePhotos || !"room".equals(key))) return new Collection((JSONArray) nested, object);
            Collection found = find(nested, depth + 1, insidePhotos);
            if (found != null) return found;
        }
        return null;
    }

    private static JSONObject findRoom(JSONObject object, int depth) {
        if (object == null || depth > 5) return null;
        JSONObject room = object.optJSONObject("room");
        if (room != null) return room;
        if ((!first(object, "name", "roomName", "room_name").isEmpty())
                && (object.has("owner") || object.has("ownerName") || object.has("roomOwner"))) return object;
        for (String key : new String[]{"data", "result", "payload"}) {
            room = findRoom(object.optJSONObject(key), depth + 1);
            if (room != null) return room;
        }
        return null;
    }

    private static Object value(JSONObject object, String... keys) {
        if (object != null) for (String key : keys) if (object.has(key)) return object.opt(key);
        return null;
    }
    private static boolean hasNullNext(JSONObject object) {
        if (object == null) return false;
        for (String key : new String[]{"next", "nextPage", "next_page"}) {
            if (!object.has(key)) continue;
            Object value = object.opt(key);
            if (value == JSONObject.NULL || Boolean.FALSE.equals(value)) return true;
            if (value instanceof Number && ((Number) value).intValue() <= 0) return true;
            if (value instanceof String && ((String) value).trim().equals("0")) return true;
        }
        return false;
    }
    private static int next(JSONObject object) {
        Object raw = value(object, "nextPage", "next_page", "next");
        if (raw instanceof JSONObject) return positive((JSONObject) raw, "page", "nextPage");
        if (raw instanceof Number) return ((Number) raw).intValue();
        if (raw instanceof String) {
            try { return Integer.parseInt((String) raw); } catch (NumberFormatException ignored) {}
            Matcher match = Pattern.compile("[?&]page=(\\d+)").matcher((String) raw);
            if (match.find()) try { return Integer.parseInt(match.group(1)); } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
    private static int positive(JSONObject object, String... keys) {
        if (object != null) for (String key : keys) { int n = object.optInt(key, 0); if (n > 0) return n; }
        return 0;
    }
    private static String first(JSONObject object, String... keys) {
        if (object != null) for (String key : keys) {
            Object value = object.opt(key);
            if (value != null && value != JSONObject.NULL && !(value instanceof JSONObject) && !(value instanceof JSONArray)) {
                String text = String.valueOf(value).trim();
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }
}
