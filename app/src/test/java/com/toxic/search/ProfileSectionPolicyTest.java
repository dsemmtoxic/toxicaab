package com.toxic.search;

import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProfileSectionPolicyTest {
    @Test public void officialOnlyPrivateProfilesHideAllUnavailableAreasEvenWithLoadingFlags() {
        ProfileResult p=new ProfileResult(); p.privateProfile=true;
        p.friendsDatesReady=true; p.friendsLoading=true; p.photosHasMore=true; p.badgesLoading=true;
        for(String section:new String[]{"friends","photos","rooms","groups","badges","styles"})assertFalse(ProfileSectionPolicy.visible(p,section));
    }
    @Test public void privateHistoricalDataRemainsVisibleWhenPresent() throws Exception {
        ProfileResult p=new ProfileResult(); p.privateProfile=true;
        p.oldFriends.add(new JSONObject().put("name","friend"));
        p.photos.add(new JSONObject().put("url","https://example.test/photo.png"));
        assertTrue(ProfileSectionPolicy.visible(p,"friends"));
        assertTrue(ProfileSectionPolicy.visible(p,"photos"));
        assertFalse(ProfileSectionPolicy.visible(p,"groups"));
    }
    @Test public void publicLoadingAndHeaderInformationAreUnaffected() {
        ProfileResult p=new ProfileResult();
        assertTrue(ProfileSectionPolicy.visible(p,"friends"));
        p.privateProfile=true;
        assertTrue(ProfileSectionPolicy.visible(p,"header"));
        assertTrue(ProfileSectionPolicy.visible(p,"stats"));
    }
    @Test public void snapshotsPrepareSignaturesWithoutSharingMutableRecords() throws Exception {
        ProfileResult source=new ProfileResult();
        source.photos.add(new JSONObject().put("id","photo").put("likes",4));
        ProfileResult snapshot=ProfileSnapshots.copy(source);
        long hash=1125899906842597L;
        for(JSONObject item:snapshot.photos)hash=31L*hash+item.toString().hashCode();
        assertEquals("1:"+Long.toHexString(hash),snapshot.recordSignatures.get(snapshot.photos));
        source.photos.get(0).put("likes",10);
        assertEquals(4,snapshot.photos.get(0).getInt("likes"));
        ProfileResult visible=new ProfileResult();
        ProfileStateMerger.update(visible,snapshot);
        assertEquals(snapshot.recordSignatures.get(snapshot.photos),visible.recordSignatures.get(visible.photos));
    }
}
