package com.toxic.search;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.Locale;

/** HabboDex catalogue metadata; all locales remain cached for hotel changes. */
final class ClothingDetails {
    static final String API = "https://habbodex.com/api/v1/furnidex/furni/";
    static String code(JSONObject item) {
        if (item == null) return "";
        for (String key : new String[]{"code", "classname", "className", "furniCode"}) {
            String value = item.optString(key, "").trim().toLowerCase(Locale.ROOT);
            if (value.matches("[a-z][a-z0-9_]{2,119}")) return value;
        }
        return "";
    }
    static boolean valid(JSONObject item, String expected) {
        return item != null && !expected.isEmpty() && expected.equals(code(item))
                && item.optBoolean("ok", true) && item.optInt("status", 200) < 400
                && (!item.has("error") || item.isNull("error") || Boolean.FALSE.equals(item.opt("error")))
                && item.optJSONObject("localeNames") != null;
    }
    static JSONObject primary(JSONObject item) { return item == null ? null : item.optJSONObject("_habboDexDetails"); }
    static String image(JSONObject item, boolean icon) {
        JSONObject data = primary(item);
        String code = code(data);
        return code.isEmpty() ? "" : "https://habbodex.com/images/furni/"+code+"/"+code+(icon ? "_icon" : "")+".png";
    }
    static String hotelKey(String hotel) {
        String key = hotel == null ? "" : hotel.toLowerCase(Locale.ROOT);
        return "com".equals(key) || "en".equals(key) ? "us" : key;
    }
    static String localized(JSONObject map, String hotel) {
        if (map == null) return "";
        for (String key : new String[]{hotelKey(hotel), "us", "en", "br"}) {
            String value = map.optString(key, "").trim();
            if (!value.isEmpty() && !"null".equalsIgnoreCase(value)) return value;
        }
        Iterator<String> keys=map.keys();
        while(keys.hasNext()) { String value=map.optString(keys.next(), "").trim(); if(!value.isEmpty() && !"null".equalsIgnoreCase(value)) return value; }
        return "";
    }
    static String name(JSONObject data, String hotel) { return data == null ? "" : localized(data.optJSONObject("localeNames"), hotel); }
    static String description(JSONObject data, String hotel) {
        if (data == null) return "";
        String value=localized(data.optJSONObject("localeDescriptions"),hotel), code=code(data);
        return value.equalsIgnoreCase(code) || value.equalsIgnoreCase(code+" desc") ? "" : value;
    }
    static String collection(JSONObject data,String hotel) {
        JSONObject line=data==null?null:data.optJSONObject("line");
        return line==null?"":localized(line.optJSONObject("localeNames"),hotel);
    }
    static JSONObject hotelInfo(JSONObject data, String hotel) {
        JSONObject badge=data==null?null:data.optJSONObject("badgeInfo");
        JSONObject hotels=badge==null?null:badge.optJSONObject("hotelInfo");
        return hotels==null?null:hotels.optJSONObject(hotelKey(hotel));
    }
    static long ownerCount(JSONObject data,String hotel,boolean tracked) {
        JSONObject info=hotelInfo(data,hotel); String key=tracked?"trackedOwnerCount":"ownerCount";
        return info==null || !info.has(key) || info.isNull(key) ? -1 : Math.max(-1,info.optLong(key,-1));
    }
    static JSONArray prices(JSONObject data,String hotel) {
        JSONArray prices=data==null?null:data.optJSONArray("prices");
        if(prices!=null && prices.length()>0)return prices;
        JSONObject info=hotelInfo(data,hotel);
        return info==null?null:info.optJSONArray("prices");
    }
}
