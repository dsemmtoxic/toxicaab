package com.toxic.search;

/** Private profiles expose a section only after its data is actually available. */
final class ProfileSectionPolicy {
    static boolean visible(ProfileResult profile,String section) {
        if(profile==null)return false;
        if(!profile.privateProfile && !profile.banned)return true;
        switch(section) {
            case "styles": return !profile.previousStyles.isEmpty();
            case "photos": return !profile.photos.isEmpty();
            case "friends": return !profile.friends.isEmpty() || !profile.oldFriends.isEmpty();
            case "rooms": return !profile.rooms.isEmpty();
            case "groups": return !profile.groups.isEmpty();
            case "badges": return !profile.badges.isEmpty() || !profile.badgesWithAchievements.isEmpty();
            default: return true;
        }
    }
}
