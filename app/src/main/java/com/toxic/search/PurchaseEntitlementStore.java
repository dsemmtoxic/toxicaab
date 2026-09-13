package com.toxic.search;

import android.content.SharedPreferences;

/** Persists only entitlements already confirmed by the verification endpoint. */
final class PurchaseEntitlementStore {
    private static final String TOKEN = "supporter_verified_token";
    private static final String UNTIL = "supporter_verified_until_ms";
    private static final String EXPIRES = "supporter_verified_expires_at_ms";
    private final SharedPreferences preferences;
    PurchaseEntitlementStore(SharedPreferences preferences) { this.preferences = preferences; }

    static final class Entitlement {
        final String token;
        final long verifiedUntil, expiresAt;
        Entitlement(String token, long verifiedUntil, long expiresAt) {
            this.token = token; this.verifiedUntil = verifiedUntil; this.expiresAt = expiresAt;
        }
    }

    Entitlement read(long now) {
        String token = preferences.getString(TOKEN, "");
        long until = preferences.getLong(UNTIL, 0L), expires = preferences.getLong(EXPIRES, 0L);
        if (token == null || token.trim().isEmpty() || until <= now || (expires > 0 && expires <= now)) {
            clear();
            return null;
        }
        return new Entitlement(token.trim(), until, expires);
    }

    long save(String token, long now, long expires, long ttl) {
        long until = now + ttl;
        if (expires > now) until = Math.min(until, expires);
        preferences.edit().putString(TOKEN, token).putLong(UNTIL, until).putLong(EXPIRES, expires).apply();
        return until;
    }

    void clear() { preferences.edit().remove(TOKEN).remove(UNTIL).remove(EXPIRES).apply(); }
}
