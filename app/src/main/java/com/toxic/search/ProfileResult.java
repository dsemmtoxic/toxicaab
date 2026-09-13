package com.toxic.search;

import org.json.JSONObject;
import java.util.*;

final class ProfileResult {
    boolean friendsLoadFailed, removedFriendsLoadFailed, badgesLoadFailed;
    int searchToken = 0;
    long snapshotVersion = 0L;
    String searchedNick = "", uniqueId = "", name = "", motto = "", figure = "", memberSince = "", lastAccess = "", level = "", starGems = "", totalBadges = "", hotelKey = "br";
    boolean online = false, privateProfile = false, banned = false;
    JSONObject habboPublic, dex, suggest, dexProfile, officialProfile;
    HashMap<String, JSONObject> officialBadgeLookup = new HashMap<>();
    ArrayList<JSONObject> previousNames = new ArrayList<>(), previousMottos = new ArrayList<>(), previousStyles = new ArrayList<>(), photos = new ArrayList<>(), friends = new ArrayList<>(), oldFriends = new ArrayList<>(), rooms = new ArrayList<>(), oldRooms = new ArrayList<>(), groups = new ArrayList<>(), selectedBadges = new ArrayList<>(), badges = new ArrayList<>(), badgesWithAchievements = new ArrayList<>();
    ArrayList<JSONObject> allPhotosSource = new ArrayList<>(), allStylesSource = new ArrayList<>();
    int photosNextPage = 0, stylesNextPage = 0, photosTotal = 0, stylesTotal = 0;
    int stylesRemoteNextPage = 0;
    volatile long photosAutoLoadRetryAfterMs = 0L, stylesAutoLoadRetryAfterMs = 0L;
    int removedFriendsNextPage = 0, removedFriendsTotal = 0, friendsNextPage = 0, friendsTotal = 0, friendsTabPage = 1;
    int previousMottosSlideIndex = 0;
    int badgesNextPage = 0, badgesTotal = 0, badgesTabPage = 1;
    boolean photosHasMore = false, stylesHasMore = false;
    volatile boolean photosLoading = false, stylesLoading = false;
    boolean removedFriendsHasMore = false, removedFriendsLoading = false, removedFriendsDataAvailable = false, friendsHasMore = false, friendsLoading = false, friendsPagedMode = false, friendsTabShowingRemoved = false, friendsTabSelectionTouched = false;
    boolean badgesHasMore = false, badgesLoading = false, badgesPagedMode = false, hideAchievementBadges = true;
    boolean officialProfileAttempted = false, officialPhotosAttempted = false, officialPhotosSucceeded = false, photosFromOfficial = false, stylesFromComplement = false, stylesRemotePaged = false, friendsDatesReady = false;
}
