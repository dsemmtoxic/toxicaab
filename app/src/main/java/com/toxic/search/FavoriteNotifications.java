package com.toxic.search;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import static com.toxic.search.MainActivity.localizedStringStatic;

final class FavoriteNotifications {
    static final String EXTRA_NICK = "favorite_profile_nick";
    static final String EXTRA_HOTEL = "favorite_profile_hotel";
    static final String EXTRA_ID = "favorite_profile_id";
    static final String EXTRA_FIGURE = "favorite_profile_figure";
    private static final String PREFS = "toxic_search_settings";
    private static final String PREF_NOTIFY_FAVORITE_ONLINE = "notify_favorite_online";
    private static final String PREF_FAVORITES = "favorite_profiles";
    private static final String PREF_FAVORITE_ONLINE_STATES = "favorite_online_states";
    

    static void checkFavoritesInBackground(Context context) {
        if (context == null) return;
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (!sp.getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, true)) return;

        String rawFavorites = sp.getString(PREF_FAVORITES, "");
        if (rawFavorites == null || rawFavorites.trim().isEmpty()) return;

        JSONObject states;
        try {
            String rawStates = sp.getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            states = new JSONObject(rawStates == null || rawStates.trim().isEmpty() ? "{}" : rawStates);
        } catch(Exception e) {
            states = new JSONObject();
        }

        try {
            JSONArray arr = new JSONArray(rawFavorites);
            long deadline = android.os.SystemClock.elapsedRealtime() + 120_000L;
            for (int i=0; i<arr.length(); i++) {
                if (Thread.currentThread().isInterrupted() || FavoriteRefreshCoordinator.foreground
                        || android.os.SystemClock.elapsedRealtime() > deadline
                        || !sp.getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, true)) break;
                JSONObject fav = arr.optJSONObject(i);
                if (fav == null) continue;
                String nick = fav.optString("nick", "").trim();
                if (nick.isEmpty()) continue;
                String hotel = normalizeHotelKeyStatic(fav.optString("hotel", "br"));
                if (hotel.isEmpty()) hotel = "br";
                String uniqueId = fav.optString("uniqueId", fav.optString("id", "")).trim();
                String key = profileIdentityKeyStatic(hotel, uniqueId, nick);

                FavoriteStatus st = fetchFavoriteStatusStatic(nick, fav.optString("figure", ""), hotel, uniqueId);
                if (Thread.currentThread().isInterrupted() || FavoriteRefreshCoordinator.foreground
                        || !sp.getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, true)) break;
                if (st == null) continue;

                boolean hadPrevious = states.has(key);
                boolean wasOnline = states.optBoolean(key, false);
                states.put(key, st.online);

                // The scheduler can run much later than login; the fresh state transition
                // is the signal, not a short last-login window.
                if (hadPrevious && !wasOnline && st.online
                        && !Thread.currentThread().isInterrupted()) {
                    showFavoriteOnlineSystemNotificationStatic(context, st);
                }
            }
            sp.edit().putString(PREF_FAVORITE_ONLINE_STATES, states.toString()).apply();
        } catch(Exception error) { AppDiagnostics.failure("favorite_background_check", error); }
    }

    private static FavoriteStatus fetchFavoriteStatusStatic(String nick, String fallbackFigure, String hotel, String uniqueId) {
        PresenceRepository.Result result = new PresenceRepository().fetch(hotel, uniqueId, nick);
        if (result.profile == null || result.state == PresenceRepository.State.UNKNOWN) return null;
        JSONObject obj = result.profile;
        FavoriteStatus status = new FavoriteStatus();
        status.nick = obj.optString("name", nick);
        status.uniqueId = obj.optString("uniqueId", uniqueId);
        status.figure = obj.optString("figureString", fallbackFigure);
        status.hotelKey = hotel;
        status.online = result.state == PresenceRepository.State.ONLINE;
        status.privateProfile = !obj.optBoolean("profileVisible", true);
        status.lastAccess = obj.optString("lastAccessTime", obj.optString("lastLoginTime", ""));
        return status;
    }


    private static Bitmap loadNotificationHeadBitmapStatic(Context context, FavoriteStatus st) {
        if (context == null || st == null) return null;
        String url = "";
        try {
            String base = "https://" + hotelDomainStatic(st.hotelKey) + "/habbo-imaging/avatarimage?";
            if (st.nick != null && !st.nick.trim().isEmpty()) url = base + "user=" + URLEncoder.encode(st.nick, "UTF-8");
            else if (st.figure != null && !st.figure.trim().isEmpty()) url = base + "figure=" + URLEncoder.encode(st.figure, "UTF-8");
            if (!url.isEmpty()) url += "&size=m&direction=2&head_direction=2&headonly=1";
        } catch (Exception ignored) {}
        String key = profileIdentityKeyStatic(st.hotelKey, st.uniqueId, st.nick);
        File legacy = new File(new File(context.getCacheDir(), "favorite_heads"), Math.abs(key.hashCode()) + ".png");
        return FavoriteAvatarLoader.notificationIcon(context, url, legacy);
    }

    static Intent profileIntent(Context context, FavoriteStatus st) {
        String hotel = normalizeHotelKeyStatic(st.hotelKey);
        if (hotel.isEmpty()) hotel = "br";
        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        open.putExtra(EXTRA_NICK, st.nick).putExtra(EXTRA_HOTEL, hotel)
                .putExtra(EXTRA_ID, st.uniqueId).putExtra(EXTRA_FIGURE, st.figure);
        open.setData(new android.net.Uri.Builder().scheme("toxic").authority("favorite")
                .appendPath(profileIdentityKeyStatic(hotel, st.uniqueId, st.nick)).build());
        return open;
    }

    static ProfileHistoryItem profileFromIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(EXTRA_HOTEL)) return null;
        String hotel = normalizeHotelKeyStatic(intent.getStringExtra(EXTRA_HOTEL));
        if (hotel.isEmpty()) return null;
        String nick = intent.getStringExtra(EXTRA_NICK);
        String id = intent.getStringExtra(EXTRA_ID);
        if ((nick == null || nick.trim().isEmpty()) && (id == null || id.trim().isEmpty())) return null;
        return new ProfileHistoryItem(nick == null ? "" : nick.trim(),
                intent.getStringExtra(EXTRA_FIGURE), hotel, id == null ? "" : id.trim());
    }

    static void showFavoriteOnlineSystemNotificationStatic(Context context, FavoriteStatus st) {
        try {
            NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm == null || st == null) return;
            String channelId = "favorite_online";
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(channelId, localizedStringStatic(context, st.hotelKey, R.string.favorites), NotificationManager.IMPORTANCE_HIGH);
                nm.createNotificationChannel(ch);
            }
            Intent open = profileIntent(context, st);
            PendingIntent pi = PendingIntent.getActivity(context,
                    profileIdentityKeyStatic(st.hotelKey, st.uniqueId, st.nick).hashCode(), open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            String msg = localizedStringStatic(context, st.hotelKey, R.string.favorite_online_banner, st.nick == null ? "" : st.nick);
            Bitmap largeIcon = loadNotificationHeadBitmapStatic(context, st);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, channelId) : new Notification.Builder(context);
            b.setSmallIcon(R.drawable.notification_image)
             .setContentTitle(localizedStringStatic(context, st.hotelKey, R.string.favorites))
             .setContentText(msg)
             .setWhen(System.currentTimeMillis())
             .setShowWhen(true)
             .setPriority(Notification.PRIORITY_HIGH)
             .setContentIntent(pi)
             .setAutoCancel(true)
             .setStyle(new Notification.BigTextStyle().bigText(msg));
            if (largeIcon != null) b.setLargeIcon(largeIcon);
            nm.notify(Math.abs(profileIdentityKeyStatic(st.hotelKey, st.uniqueId, st.nick).hashCode()), b.build());
        } catch(Exception ignored) {}
    }

    private static String readAllStatic(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
        return out.toString("UTF-8");
    }

    private static String normalizeHotelKeyStatic(String hotel) {
        String h = hotel == null ? "" : hotel.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if ("us".equals(h)) h = "com";
        String[] allowed = {"br","com","es","de","fr","fi","it","nl","tr"};
        for (String a : allowed) if (a.equals(h)) return h;
        return "";
    }

    private static String profileIdentityKeyStatic(String hotelKey, String uniqueId, String nick) {
        String hotel = normalizeHotelKeyStatic(hotelKey);
        String id = normalizeNickKeyStatic(uniqueId);
        if (!id.isEmpty()) return hotel + ":id:" + id;
        return hotel + ":nick:" + normalizeNickKeyStatic(nick);
    }

    private static String normalizeNickKeyStatic(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private static String hotelDomainStatic(String key) {
        String h = normalizeHotelKeyStatic(key);
        if ("com".equals(h)) return "www.habbo.com";
        if ("es".equals(h)) return "www.habbo.es";
        if ("de".equals(h)) return "www.habbo.de";
        if ("fr".equals(h)) return "www.habbo.fr";
        if ("fi".equals(h)) return "www.habbo.fi";
        if ("it".equals(h)) return "www.habbo.it";
        if ("nl".equals(h)) return "www.habbo.nl";
        if ("tr".equals(h)) return "www.habbo.com.tr";
        return "www.habbo.com.br";
    }
}
