package com.toxic.search;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import static org.junit.Assert.*;

public final class RoomPhotosPageTest {
    @Test public void emptyRoomResponsesAreSuccessfulEndStates() throws Exception {
        RoomPhotosPage page=RoomPhotosPage.decode(new JSONObject().put("result",JSONObject.NULL).put("total",0),1,8,"123");
        assertTrue(page.success); assertEquals(0,page.nextPage); assertTrue(page.items.isEmpty());
        assertTrue(RoomPhotosPage.decode(new JSONObject().put("status",404),1,8,"123").success);
        assertFalse(RoomPhotosPage.decode(new JSONObject().put("status",503),1,8,"123").success);
    }
    @Test public void onlyMissingOrEmptyRoomPhotoResourcesAreConvertedToEmpty() {
        String url=RoomPhotosPage.url("br","150478947",1);
        assertTrue(RoomPhotosPage.emptyHttpResult(url,404));
        assertTrue(RoomPhotosPage.emptyHttpResult(url,204));
        assertTrue(RoomPhotosPage.emptyHttpResult(url,410));
        assertFalse(RoomPhotosPage.emptyHttpResult(url,0));
        assertFalse(RoomPhotosPage.emptyHttpResult(url,403));
        assertFalse(RoomPhotosPage.emptyHttpResult(url,503));
        assertFalse(RoomPhotosPage.emptyHttpResult("https://habbodex.com/api/v1/furnidex/furni/clothing_r20_inuit",404));
    }
    @Test public void theRoomPhotoRequestUsesThePhotosResource() {
        assertEquals("https://habbodex.com/api/v1/roominfo/br/room/150478947/photos?page=1&limit=8",
                RoomPhotosPage.url("br", "150478947", 1));
        assertTrue(RoomPhotosPage.url("com", "150478947", 2).contains("/photos?page=2&limit=8"));
    }

    @Test public void theActualApiSchemaKeepsZeroRoomIdsAndCreatorDetails() throws Exception {
        JSONObject photo = new JSONObject().put("creationTime", 1782946029052L)
                .put("creator", new JSONObject().put("name", "Lcka").put("id", "hhbr-0a25f3f07fda370170978c5c257ebda4")
                        .put("figureString", "hr-828-57.hd-209-10.ch-220-92"))
                .put("hotel", "br").put("id", "bd37c7f5-137b-4182-a345-3ac20bc3fce7")
                .put("likerNames", new JSONArray().put("Lu,a"))
                .put("previewUrl", "//habbo-stories-content.s3.amazonaws.com/servercamera/purchased/hhbr/p-14899929-1782946028933.png")
                .put("roomId", 0);
        JSONObject response = new JSONObject().put("result", new JSONArray().put(photo)).put("total", 1);
        RoomPhotosPage page = RoomPhotosPage.decode(response, 1, 8, "150478947");
        assertTrue(page.success);
        assertEquals(1, page.items.size());
        assertEquals("150478947", page.items.get(0).getString("roomId"));
        assertEquals("Lcka", page.items.get(0).getJSONObject("creator").getString("name"));
        assertEquals("hhbr-0a25f3f07fda370170978c5c257ebda4", page.items.get(0).getJSONObject("creator").getString("id"));
        assertEquals(1782946029052L, page.items.get(0).getLong("creationTime"));
        assertEquals("Lu,a", page.items.get(0).getJSONArray("likerNames").getString(0));
        assertEquals(0, photo.getInt("roomId"));
        assertEquals(0, page.nextPage);
    }

    @Test public void aFullResultPageDoesNotStopAtTheReturnedCount() throws Exception {
        JSONObject response = new JSONObject().put("result", photos(8, "0")).put("total", 8);
        assertEquals(2, RoomPhotosPage.decode(response, 1, 8, "123").nextPage);
        assertEquals(3, RoomPhotosPage.decode(response, 2, 8, "123").nextPage);
        response.put("result", photos(3, "0")).put("total", 3);
        assertEquals(0, RoomPhotosPage.decode(response, 3, 8, "123").nextPage);
        response.put("result", new JSONArray()).put("total", 0);
        assertTrue(RoomPhotosPage.decode(response, 4, 8, "123").success);
        assertEquals(0, RoomPhotosPage.decode(response, 4, 8, "123").nextPage);
    }

    @Test public void explicitPaginationStillEndsAFullResultPage() throws Exception {
        JSONObject response = new JSONObject().put("result", photos(8, "0")).put("total", 8)
                .put("hasMore", false);
        assertEquals(0, RoomPhotosPage.decode(response, 1, 8, "123").nextPage);
    }
    private JSONArray photos(int count, String room) throws Exception {
        JSONArray list = new JSONArray();
        for (int i = 0; i < count; i++) list.put(new JSONObject().put("id", i)
                .put("roomId", room).put("url", "https://example.test/" + i + ".png"));
        return list;
    }

    @Test public void nestedRoomCollectionKeepsItsOwnPagination() throws Exception {
        JSONObject response = new JSONObject().put("ok", true).put("data", new JSONObject()
                .put("room", new JSONObject().put("id", "150478947").put("name", "Sala"))
                .put("photos", new JSONObject().put("data", photos(20, "150478947"))
                        .put("pagination", new JSONObject().put("total", 280).put("nextPage", 2))));
        RoomPhotosPage page = RoomPhotosPage.decode(response, 1, 20, "150478947");
        assertTrue(page.success);
        assertEquals(20, page.items.size());
        assertEquals(280, page.total);
        assertEquals(2, page.nextPage);
        assertEquals("Sala", page.room.getString("name"));
    }

    @Test public void anUnpagedAlbumDoesNotHaveA200PhotoLimit() throws Exception {
        RoomPhotosPage page = RoomPhotosPage.decode(new JSONObject().put("photos", photos(280, "123"))
                .put("photoCount", 280), 1, 20, "123");
        assertEquals(280, page.items.size());
        assertEquals(280, page.total);
        assertEquals(0, page.nextPage);
    }

    @Test public void photosFromAnotherRoomAreExcludedWithoutMutatingTheResponse() throws Exception {
        JSONObject valid = new JSONObject().put("url", "https://example.test/a.png");
        JSONObject response = new JSONObject().put("room", new JSONObject().put("photos", new JSONArray()
                .put(valid).put(new JSONObject().put("roomId", "456").put("url", "https://example.test/b.png"))));
        RoomPhotosPage page = RoomPhotosPage.decode(response, 1, 20, "123");
        assertEquals(1, page.items.size());
        assertEquals("123", page.items.get(0).getString("roomId"));
        assertFalse(valid.has("roomId"));
    }

    @Test public void aProviderFailureIsNotReportedAsAnEmptyAlbum() throws Exception {
        assertFalse(RoomPhotosPage.decode(new JSONObject().put("status", 503).put("error", "unavailable"), 1, 8, "123").success);
        assertFalse(RoomPhotosPage.decode(new JSONObject().put("status", 500).put("result", new JSONArray()), 1, 8, "123").success);
        assertFalse(RoomPhotosPage.decode("<html>Challenge</html>", 1, 20, "123").success);
        assertTrue(RoomPhotosPage.decode(new JSONObject().put("photos", new JSONArray()), 1, 20, "123").success);
        assertTrue(RoomPhotosPage.decode(new JSONObject().put("photos", new JSONArray()).put("error", JSONObject.NULL), 1, 20, "123").success);
    }

    @Test public void nullResultsWithoutTotalsStillMeanAnEmptyAlbum() throws Exception {
        for (Object response : new Object[]{JSONObject.NULL, new JSONObject().put("result", JSONObject.NULL),
                new JSONObject().put("data", new JSONObject().put("photos", JSONObject.NULL)),
                new JSONObject().put("total", 0), new JSONObject()}) {
            RoomPhotosPage page = RoomPhotosPage.decode(response, 1, 8, "123");
            assertTrue(page.success);
            assertTrue(page.items.isEmpty());
            assertEquals(0, page.nextPage);
        }
    }

    @Test public void responsiveNoPhotoMessagesAreNotNetworkFailures() throws Exception {
        for (JSONObject response : new JSONObject[]{new JSONObject().put("error", "No photos found"),
                new JSONObject().put("ok", false).put("result", new JSONArray()),
                new JSONObject().put("status", 404).put("message", "Room not found")}) {
            RoomPhotosPage page = RoomPhotosPage.decode(response, 1, 8, "123");
            assertTrue(page.success);
            assertTrue(page.items.isEmpty());
            assertEquals(0, page.nextPage);
        }
    }

    @Test public void explicitLastPageWinsOverAnOutdatedTotal() throws Exception {
        JSONObject response = new JSONObject().put("photos", photos(20, "123")).put("total", 280)
                .put("pagination", new JSONObject().put("nextPage", JSONObject.NULL));
        assertEquals(0, RoomPhotosPage.decode(response, 14, 20, "123").nextPage);
        response.getJSONObject("pagination").put("nextPage", 0);
        assertEquals(0, RoomPhotosPage.decode(response, 2, 20, "123").nextPage);
    }

    @Test public void nextUrlIsReadAsAPageAndNeverUsedAsADownloadTarget() throws Exception {
        JSONObject response = new JSONObject().put("photos", photos(20, "123"))
                .put("next", "https://example.test/photos?page=3&limit=20");
        assertEquals(3, RoomPhotosPage.decode(response, 2, 20, "123").nextPage);
    }

    @Test public void shortServerPagesAreFollowedUsingTheRealPageSize() throws Exception {
        JSONObject response = new JSONObject().put("photos", photos(10, "123"))
                .put("pagination", new JSONObject().put("total", 35).put("perPage", 10));
        assertEquals(3, RoomPhotosPage.decode(response, 2, 20, "123").nextPage);
    }

    @Test public void aRawArrayAndAnEmptyLastPageAreValid() throws Exception {
        assertEquals(280, RoomPhotosPage.decode(photos(280, "123"), 1, 20, "123").items.size());
        RoomPhotosPage last = RoomPhotosPage.decode(new JSONArray(), 16, 20, "123");
        assertTrue(last.success);
        assertEquals(0, last.nextPage);
    }
}
