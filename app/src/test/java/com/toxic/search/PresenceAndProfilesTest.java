package com.toxic.search;

import org.json.JSONObject;
import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public final class PresenceAndProfilesTest {
    @Test public void currentPresenceOverridesBothDirectionsWithoutChangingDates() throws Exception {
        for (boolean historical : new boolean[]{true, false}) {
            JSONObject old = new JSONObject().put("uniqueId", "hhbr-test").put("online", historical)
                    .put("removedAt", "2026-09-01");
            JSONObject fresh = new JSONObject().put("uniqueId", "hhbr-test").put("online", !historical);
            ArrayList<JSONObject> records = new ArrayList<>(), current = new ArrayList<>();
            records.add(old); current.add(fresh);
            FriendRecords.applyCurrentPresence(records, current);
            assertEquals(!historical, old.getBoolean("online"));
            assertEquals("2026-09-01", old.getString("removedAt"));
        }
    }

    @Test public void missingPresenceIsUnknown() throws Exception {
        assertEquals(PresenceRepository.State.UNKNOWN, PresenceRepository.readState(new JSONObject()));
        assertEquals(PresenceRepository.State.UNKNOWN, PresenceRepository.readState(new JSONObject().put("error", "temporarily unavailable")));
        assertEquals(PresenceRepository.State.ONLINE, PresenceRepository.readState(new JSONObject().put("isOnline", true)));
        assertEquals(PresenceRepository.State.OFFLINE, PresenceRepository.readState(new JSONObject().put("online", false)));
    }

    @Test public void unrelatedRecordsAreNotOverwritten() throws Exception {
        JSONObject friend = new JSONObject().put("uniqueId", "hhbr-a").put("online", false);
        ArrayList<JSONObject> records = new ArrayList<>(), current = new ArrayList<>();
        records.add(friend);
        current.add(new JSONObject().put("uniqueId", "hhbr-b").put("online", true));
        FriendRecords.applyCurrentPresence(records, current);
        assertFalse(friend.getBoolean("online"));
    }

    @Test public void snapshotDoesNotShareMutableRecords() throws Exception {
        ProfileResult source = new ProfileResult();
        JSONObject record = new JSONObject().put("name", "Before").put("online", true)
                .put("membership", new JSONObject().put("admin", false));
        source.friends.add(record);
        source.dexProfile = new JSONObject().put("identity", record);
        ProfileResult snapshot = ProfileSnapshots.copy(source);
        record.put("name", "After");
        record.getJSONObject("membership").put("admin", true);
        assertEquals("Before", snapshot.friends.get(0).getString("name"));
        assertFalse(snapshot.friends.get(0).getJSONObject("membership").getBoolean("admin"));
        assertEquals("Before", snapshot.dexProfile.getJSONObject("identity").getString("name"));
    }

    @Test public void backgroundUpdatesKeepTheSelectedPage() {
        ProfileResult displayed = new ProfileResult(), incoming = new ProfileResult();
        displayed.friendsTabPage = 7; displayed.friendsTabShowingRemoved = true;
        displayed.friendsTabSelectionTouched = true;
        displayed.badgesTabPage = 3; displayed.hideAchievementBadges = false;
        incoming.name = "Updated"; incoming.friendsTotal = 140;
        ProfileStateMerger.update(displayed, incoming);
        assertEquals("Updated", displayed.name);
        assertEquals(140, displayed.friendsTotal);
        assertEquals(7, displayed.friendsTabPage);
        assertTrue(displayed.friendsTabShowingRemoved);
        assertEquals(3, displayed.badgesTabPage);
        assertFalse(displayed.hideAchievementBadges);
    }

    @Test public void groupIconsUseCodesAndConfirmedOwnership() throws Exception {
        assertEquals(GroupMetadata.OPEN, GroupMetadata.access(new JSONObject().put("type", "NORMAL")));
        assertEquals(GroupMetadata.REQUEST, GroupMetadata.access(new JSONObject().put("type", "EXCLUSIVE")));
        assertEquals(GroupMetadata.CLOSED, GroupMetadata.access(new JSONObject().put("type", "CLOSED")));
        assertEquals(GroupMetadata.UNKNOWN, GroupMetadata.access(new JSONObject()));
        assertEquals(GroupMetadata.MEMBER, GroupMetadata.role(new JSONObject().put("owner", true)));
        assertEquals(GroupMetadata.ADMIN, GroupMetadata.role(new JSONObject().put("membership", new JSONObject().put("isAdmin", true))));
        assertEquals(GroupMetadata.OWNER, GroupMetadata.role(new JSONObject().put(GroupMetadata.OWNER_MARKER, true).put("isAdmin", true)));
    }

    @Test public void delayedSnapshotsDoNotReplaceNewerData() {
        ProfileResult source = new ProfileResult();
        source.name = "First";
        ProfileResult earlier = ProfileSnapshots.copy(source);
        source.name = "Latest";
        ProfileResult later = ProfileSnapshots.copy(source);
        ProfileResult displayed = new ProfileResult();
        assertTrue(ProfileStateMerger.update(displayed, later));
        assertFalse(ProfileStateMerger.update(displayed, earlier));
        assertEquals("Latest", displayed.name);
    }

    @Test public void serverRetryDelaysAreBounded() {
        assertEquals(90_000L, JsonHttpClient.retryAfterMillis("90", 0));
        assertEquals(300_000L, JsonHttpClient.retryAfterMillis("999999", 0));
        assertEquals(300_000L, JsonHttpClient.retryAfterMillis(String.valueOf(Long.MAX_VALUE), 0));
        assertEquals(1_000L, JsonHttpClient.retryAfterMillis("-20", 0));
        assertEquals(30_000L, JsonHttpClient.retryAfterMillis(null, 0));
    }
}
