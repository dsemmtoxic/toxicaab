package com.toxic.search;

import org.json.JSONObject;
import java.util.*;

final class ProfileHistoryItem {
    final String nick;
    final String figure;
    final String hotelKey;
    final String uniqueId;
    ProfileHistoryItem(String nick, String figure, String hotelKey) {
        this(nick, figure, hotelKey, "");
    }
    ProfileHistoryItem(String nick, String figure, String hotelKey, String uniqueId) {
        this.nick = nick == null ? "" : nick;
        this.figure = figure == null ? "" : figure;
        this.hotelKey = hotelKey == null || hotelKey.trim().isEmpty() ? "br" : hotelKey;
        this.uniqueId = uniqueId == null ? "" : uniqueId.trim();
    }
}
