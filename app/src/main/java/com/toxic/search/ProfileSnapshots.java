package com.toxic.search;

import org.json.*;
import java.util.*;

final class ProfileSnapshots {
    private static final java.util.concurrent.atomic.AtomicLong versions = new java.util.concurrent.atomic.AtomicLong();
    static ProfileResult copy(ProfileResult src) {
        ProfileResult c = new ProfileResult();
        if (src == null) return c;
        c.friendsLoadFailed = src.friendsLoadFailed;
        c.removedFriendsLoadFailed = src.removedFriendsLoadFailed;
        c.badgesLoadFailed = src.badgesLoadFailed;
        c.searchToken = src.searchToken;
        c.searchedNick = src.searchedNick;
        c.uniqueId = src.uniqueId;
        c.name = src.name;
        c.motto = src.motto;
        c.figure = src.figure;
        c.memberSince = src.memberSince;
        c.lastAccess = src.lastAccess;
        c.level = src.level;
        c.starGems = src.starGems;
        c.hotelKey = src.hotelKey;
        c.online = src.online;
        c.privateProfile = src.privateProfile;
        c.banned = src.banned;
        c.habboPublic = json(src.habboPublic);
        c.dex = json(src.dex);
        c.suggest = json(src.suggest);
        c.dexProfile = json(src.dexProfile);
        c.officialProfile = json(src.officialProfile);
        c.officialBadgeLookup = new HashMap<>(); 
        for (Map.Entry<String, JSONObject> e : src.officialBadgeLookup.entrySet()) {
            c.officialBadgeLookup.put(e.getKey(), json(e.getValue()));
        }
        c.previousNames = list(src.previousNames);
        c.previousMottos = list(src.previousMottos);
        c.previousStyles = list(src.previousStyles);
        c.photos = list(src.photos);
        c.friends = list(src.friends);
        c.oldFriends = list(src.oldFriends);
        c.rooms = list(src.rooms);
        c.oldRooms = list(src.oldRooms);
        c.groups = list(src.groups);
        c.badges = list(src.badges);
        c.badgesWithAchievements = list(src.badgesWithAchievements);
        c.totalBadges = src.totalBadges;
        c.selectedBadges = list(src.selectedBadges);
        c.allPhotosSource = list(src.allPhotosSource);
        c.allStylesSource = list(src.allStylesSource);
        c.photosNextPage = src.photosNextPage;
        c.stylesNextPage = src.stylesNextPage;
        c.photosTotal = src.photosTotal;
        c.stylesTotal = src.stylesTotal;
        c.stylesRemoteNextPage = src.stylesRemoteNextPage;
        c.photosAutoLoadRetryAfterMs = src.photosAutoLoadRetryAfterMs;
        c.stylesAutoLoadRetryAfterMs = src.stylesAutoLoadRetryAfterMs;
        c.removedFriendsNextPage = src.removedFriendsNextPage;
        c.removedFriendsTotal = src.removedFriendsTotal;
        c.friendsNextPage = src.friendsNextPage;
        c.friendsTotal = src.friendsTotal;
        c.friendsTabPage = src.friendsTabPage;
        c.previousMottosSlideIndex = src.previousMottosSlideIndex;
        c.badgesNextPage = src.badgesNextPage;
        c.badgesTotal = src.badgesTotal;
        c.badgesTabPage = src.badgesTabPage;
        c.photosHasMore = src.photosHasMore;
        c.stylesHasMore = src.stylesHasMore;
        c.photosLoading = src.photosLoading;
        c.stylesLoading = src.stylesLoading;
        c.removedFriendsHasMore = src.removedFriendsHasMore;
        c.removedFriendsLoading = src.removedFriendsLoading;
        c.removedFriendsDataAvailable = src.removedFriendsDataAvailable;
        c.friendsHasMore = src.friendsHasMore;
        c.friendsLoading = src.friendsLoading;
        c.friendsPagedMode = src.friendsPagedMode;
        c.friendsTabShowingRemoved = src.friendsTabShowingRemoved;
        c.friendsTabSelectionTouched = src.friendsTabSelectionTouched;
        c.badgesHasMore = src.badgesHasMore;
        c.badgesLoading = src.badgesLoading;
        c.badgesPagedMode = src.badgesPagedMode;
        c.hideAchievementBadges = src.hideAchievementBadges;
        c.officialProfileAttempted = src.officialProfileAttempted;
        c.officialPhotosAttempted = src.officialPhotosAttempted;
        c.officialPhotosSucceeded = src.officialPhotosSucceeded;
        c.photosFromOfficial = src.photosFromOfficial;
        c.stylesFromComplement = src.stylesFromComplement;
        c.stylesRemotePaged = src.stylesRemotePaged;
        c.friendsDatesReady = src.friendsDatesReady;
        c.snapshotVersion = versions.incrementAndGet();
        return c;
    }

    static JSONObject json(JSONObject value) {
        if (value == null) return null;
        try { return new JSONObject(value.toString()); }
        catch (JSONException error) { throw new IllegalStateException("Invalid profile snapshot", error); }
    }
    static ArrayList<JSONObject> list(ArrayList<JSONObject> values) {
        ArrayList<JSONObject> result = new ArrayList<>();
        if (values != null) for (JSONObject value : values) result.add(json(value));
        return result;
    }
}
