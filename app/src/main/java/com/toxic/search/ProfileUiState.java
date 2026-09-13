package com.toxic.search;

import android.os.Bundle;

/** Saves only the reference and lightweight UI state, never stale online flags. */
final class ProfileUiState {
    String id = "", name = "", figure = "", hotel = "br";
    int friendsPage = 1, badgesPage = 1, scrollY, photosX, stylesX, mottoIndex, avatarDirection = 2;
    boolean removed, hideAchievements = true;

    boolean matches(ProfileResult profile) {
        if (profile == null || !hotel.equals(profile.hotelKey)) return false;
        return !id.isEmpty() ? id.equalsIgnoreCase(profile.uniqueId) : name.equalsIgnoreCase(profile.name);
    }

    void apply(ProfileResult profile) {
        profile.friendsTabPage = friendsPage;
        profile.friendsTabShowingRemoved = removed;
        profile.friendsTabSelectionTouched = true;
        profile.badgesTabPage = badgesPage;
        profile.hideAchievementBadges = hideAchievements;
        profile.previousMottosSlideIndex = mottoIndex;
    }

    void write(Bundle out) {
        Bundle state = new Bundle();
        state.putString("id", id); state.putString("name", name);
        state.putString("figure", figure); state.putString("hotel", hotel);
        state.putInt("friendsPage", friendsPage); state.putInt("badgesPage", badgesPage);
        state.putInt("scrollY", scrollY); state.putInt("photosX", photosX); state.putInt("stylesX", stylesX);
        state.putInt("mottoIndex", mottoIndex); state.putInt("avatarDirection", avatarDirection);
        state.putBoolean("removed", removed); state.putBoolean("hideAchievements", hideAchievements);
        out.putBundle("toxic.profile.ui.v1", state);
    }

    static ProfileUiState read(Bundle saved) {
        if (saved == null) return null;
        Bundle state = saved.getBundle("toxic.profile.ui.v1");
        if (state == null) return null;
        ProfileUiState result = new ProfileUiState();
        result.id = state.getString("id", ""); result.name = state.getString("name", "");
        if (result.id.isEmpty() && result.name.isEmpty()) return null;
        result.figure = state.getString("figure", ""); result.hotel = state.getString("hotel", "br");
        result.friendsPage = Math.max(1, state.getInt("friendsPage", 1));
        result.badgesPage = Math.max(1, state.getInt("badgesPage", 1));
        result.scrollY = Math.max(0, state.getInt("scrollY"));
        result.photosX = Math.max(0, state.getInt("photosX"));
        result.stylesX = Math.max(0, state.getInt("stylesX"));
        result.mottoIndex = Math.max(0, state.getInt("mottoIndex"));
        result.avatarDirection = Math.max(0, Math.min(7, state.getInt("avatarDirection", 2)));
        result.removed = state.getBoolean("removed");
        result.hideAchievements = state.getBoolean("hideAchievements", true);
        return result;
    }
}
