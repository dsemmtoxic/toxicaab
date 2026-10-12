package com.toxic.search;

import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ClothingDetailsTest {
    private JSONObject data() throws Exception {
        return new JSONObject().put("code","clothing_r20_inuit")
            .put("localeNames",new JSONObject().put("br","Visual Inuit").put("us","Inuit Outfit"))
            .put("localeDescriptions",new JSONObject().put("br","clothing_r20_inuit desc").put("us","A warm outfit"))
            .put("line",new JSONObject().put("localeNames",new JSONObject().put("br","Natal 2020")))
            .put("badgeInfo",new JSONObject().put("hotelInfo",new JSONObject()
                .put("br",new JSONObject().put("ownerCount",1194).put("trackedOwnerCount",745))
                .put("us",new JSONObject().put("ownerCount",1385))))
            .put("prices",new JSONArray().put(new JSONObject().put("credits",35).put("diamonds",35)));
    }
    @Test public void iconAndFullImageAreDifferentAndUseTheCatalogueCode() throws Exception {
        JSONObject item=new JSONObject().put("_habboDexDetails",data());
        assertEquals("https://habbodex.com/images/furni/clothing_r20_inuit/clothing_r20_inuit_icon.png",ClothingDetails.image(item,true));
        assertEquals("https://habbodex.com/images/furni/clothing_r20_inuit/clothing_r20_inuit.png",ClothingDetails.image(item,false));
        assertEquals("",ClothingDetails.image(new JSONObject().put("rarityIconCode","7alj14m"),true));
    }
    @Test public void namesAndCollectionsFollowTheSelectedHotel() throws Exception {
        JSONObject item=data();
        assertEquals("Visual Inuit",ClothingDetails.name(item,"br"));
        assertEquals("Inuit Outfit",ClothingDetails.name(item,"com"));
        assertEquals("Natal 2020",ClothingDetails.collection(item,"br"));
    }
    @Test public void countsUseTheSelectedHotelAndKeepTrackedCountsSeparate() throws Exception {
        JSONObject item=data();
        assertEquals(1194,ClothingDetails.ownerCount(item,"br",false));
        assertEquals(745,ClothingDetails.ownerCount(item,"br",true));
        assertEquals(1385,ClothingDetails.ownerCount(item,"com",false));
        assertEquals(-1,ClothingDetails.ownerCount(item,"fr",false));
        item.getJSONObject("badgeInfo").getJSONObject("hotelInfo").getJSONObject("br").put("ownerCount",0);
        assertEquals(0,ClothingDetails.ownerCount(item,"br",false));
    }
    @Test public void placeholderDescriptionsAreHiddenButRealDescriptionsAreKept() throws Exception {
        assertEquals("",ClothingDetails.description(data(),"br"));
        assertEquals("A warm outfit",ClothingDetails.description(data(),"us"));
    }
    @Test public void catalogueErrorsAndMismatchedItemsCannotBecomePrimary() throws Exception {
        JSONObject item=data();
        assertTrue(ClothingDetails.valid(item,"clothing_r20_inuit"));
        assertFalse(ClothingDetails.valid(item,"clothing_other"));
        item.put("ok",false); assertFalse(ClothingDetails.valid(item,"clothing_r20_inuit"));
        assertEquals("",ClothingDetails.code(new JSONObject().put("code","ch-6452")));
        assertEquals("",ClothingDetails.code(new JSONObject().put("code","../../photo")));
    }
    @Test public void eachCurrencyAndOfferIsRetained() throws Exception {
        JSONArray prices=ClothingDetails.prices(data(),"br");
        assertEquals(35,prices.getJSONObject(0).getInt("credits"));
        assertEquals(35,prices.getJSONObject(0).getInt("diamonds"));
    }
}
