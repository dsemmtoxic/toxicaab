package com.toxic.search;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.*;

public final class FriendsPresenceTest {
    private static final String PRIVATE_FRIEND = "{\"uniqueId\":\"hhbr-private\","
            + "\"name\":\"PrivateFriend\",\"profileVisible\":false,\"online\":true}";

    @Test public void privateFriendRemainsOnlineWhenOnlyTheListReportsPresence() {
        PresenceRepository repository = new PresenceRepository(url -> url.endsWith("/friends")
                ? "[" + PRIVATE_FRIEND + "]"
                : "{\"uniqueId\":\"hhbr-private\",\"profileVisible\":false}");
        assertEquals(PresenceRepository.State.UNKNOWN,
                repository.fetch("br", "hhbr-private", "PrivateFriend").state);
        assertEquals(PresenceRepository.State.ONLINE,
                repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", "PrivateFriend"));
    }

    @Test public void freshOfflineReplacesPreviousOnlineForPrivateFriend() {
        AtomicInteger requests = new AtomicInteger();
        PresenceRepository repository = new PresenceRepository(url -> "["
                + (requests.getAndIncrement() == 0 ? PRIVATE_FRIEND : PRIVATE_FRIEND.replace("true", "false")) + "]");
        assertEquals(PresenceRepository.State.ONLINE,
                repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", ""));
        assertEquals(PresenceRepository.State.OFFLINE,
                repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", ""));
        assertEquals(2, requests.get());
    }

    @Test public void failureDoesNotReuseThePreviousOnlineList() {
        AtomicInteger requests = new AtomicInteger();
        PresenceRepository repository = new PresenceRepository(url -> {
            if (requests.getAndIncrement() == 0) return "[" + PRIVATE_FRIEND + "]";
            throw new IOException("Unavailable");
        });
        assertEquals(PresenceRepository.State.ONLINE,
                repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", ""));
        assertEquals(PresenceRepository.State.UNKNOWN,
                repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", ""));
    }

    @Test public void missingOnlineIsUnknownEvenForPrivateFriends() {
        PresenceRepository repository = new PresenceRepository(url ->
                "[{\"uniqueId\":\"hhbr-private\",\"profileVisible\":false}]");
        assertEquals(PresenceRepository.State.UNKNOWN,
                repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", ""));
    }

    @Test public void currentListIsScopedToTheRequestedHotelAndOwner() {
        PresenceRepository repository = new PresenceRepository(url -> {
            assertEquals("https://www.habbo.com.tr/api/public/users/hhtr-owner/friends", url);
            return "[]";
        });
        assertEquals(PresenceRepository.State.UNKNOWN,
                repository.fetchFriends("tr", " hhtr-owner ").stateFor("hhtr-friend", ""));
    }

    @Test public void missingOwnerDoesNotTriggerAnUnscopedRequest() {
        PresenceRepository repository = new PresenceRepository(url -> { fail("Unexpected request"); return "[]"; });
        assertEquals(PresenceRepository.State.UNKNOWN,
                repository.fetchFriends("br", " ").stateFor("hhbr-private", "PrivateFriend"));
    }

    @Test public void supportedListWrappersAndBooleanRepresentations() {
        for (String payload : new String[]{"[" + PRIVATE_FRIEND + "]",
                "{\"friends\":[" + PRIVATE_FRIEND + "]}",
                "{\"data\":{\"friends\":[" + PRIVATE_FRIEND + "]}}",
                "{\"data\":[" + PRIVATE_FRIEND.replace("\"online\":true", "\"isOnline\":\"1\"") + "]}"}) {
            PresenceRepository repository = new PresenceRepository(url -> payload);
            assertEquals(PresenceRepository.State.ONLINE,
                    repository.fetchFriends("br", "hhbr-owner").stateFor("HHBR-PRIVATE", ""));
        }
    }

    @Test public void renamedFriendMatchesIdentityAndReusedNickDoesNot() throws Exception {
        PresenceRepository.FriendsSnapshot list = new PresenceRepository.FriendsSnapshot(
                new JSONArray().put(new JSONObject(PRIVATE_FRIEND)));
        assertEquals(PresenceRepository.State.ONLINE, list.stateFor("hhbr-private", "OldName"));
        assertEquals(PresenceRepository.State.UNKNOWN, list.stateFor("hhbr-another", "PrivateFriend"));
        assertEquals(PresenceRepository.State.ONLINE, list.stateFor("", " privatefriend "));
    }

    @Test public void unavailableOrEmptyListCannotInventPresence() {
        for (String payload : new String[]{"[]", "{\"error\":\"unavailable\"}", "<html>maintenance</html>"}) {
            PresenceRepository repository = new PresenceRepository(url -> payload);
            assertEquals(PresenceRepository.State.UNKNOWN,
                    repository.fetchFriends("br", "hhbr-owner").stateFor("hhbr-private", "PrivateFriend"));
        }
    }

    @Test public void individualProfileLookupStillValidatesTheIdentity() {
        PresenceRepository repository = new PresenceRepository(url -> PRIVATE_FRIEND);
        assertEquals(PresenceRepository.State.ONLINE,
                repository.fetch("br", "hhbr-private", "PrivateFriend").state);
        assertEquals(PresenceRepository.State.UNKNOWN,
                repository.fetch("br", "hhbr-another", "PrivateFriend").state);
    }
}
