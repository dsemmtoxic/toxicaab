package com.toxic.search;

import android.app.*;
import android.os.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import org.json.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryProductDetailsResult;
import com.android.billingclient.api.QueryPurchasesParams;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private static final String HABBODEX = "https://habbodex.com/api/v1";
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private FrameLayout screen;
    private LinearLayout root, resultWrap;
    private EditText searchInput;
    private Button searchBtn;
    private TextView statusText;
    private ProgressBar progress;
    private LinearLayout suggestionsBox;
    private ScrollView suggestionsScroll;
    private int suggestionRequestId = 0;
    private boolean suppressSuggestions = false;
    private boolean programmaticSearchTextChange = false;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private int avatarDirection = 2;
    private ImageView currentAvatarImage;
    private String currentProfileFigure = "";
    private boolean currentProfilePrivate = false;
    private volatile int activeSearchToken = 0;
    private volatile boolean searchInProgress = false;
    private volatile String activeSearchNick = "";
    private String currentLoadedNick = "";
    private int inlineProgressPct = 0;
    private String inlineProgressMessage = "";
    private final ConcurrentHashMap<String, ProfileResult> profileCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> profileCacheTimes = new ConcurrentHashMap<>();
    private static final long SESSION_CACHE_TTL_MS = 5L * 60L * 1000L;
    private ProfileResult activeRenderedProfile = null;
    private final ArrayDeque<ProfileResult> profileHistory = new ArrayDeque<>();
    private static final int PROFILE_HISTORY_LIMIT = 25;
    private int visiblePhotosCount = 20;
    private int visibleStylesCount = 20;
    private int photosScrollX = 0;
    private int stylesScrollX = 0;
    private static final int PAGE_CHUNK = 20;
    private static final String PREFS = "toxic_search_settings";
    private static final String PREF_MAX_PROFILES = "max_profiles";
    private static final String PREF_CACHE_DAYS = "cache_days";
    private static final String PREF_MAX_CACHE_MB = "max_cache_mb";
    private static final String PREF_HOTEL = "hotel";
    private static final String PREF_OPENED_HISTORY = "opened_profiles_history";
    private static final String PREF_FAVORITES = "favorite_profiles";
    private static final String PREF_NOTIFY_FAVORITE_ONLINE = "notify_favorite_online";
    private static final String PREF_FAVORITE_ONLINE_STATES = "favorite_online_states";
    private static final String PREF_VISUAL_EDITOR_FIGURE = "visual_editor_figure";
    private static final String PREF_VISUAL_EDITOR_GENDER = "visual_editor_gender";
    private static final String PREF_VISUAL_EDITOR_TYPE = "visual_editor_type";
    private static final String PREF_VISUAL_EDITOR_DIRECTION = "visual_editor_direction";
    private static final String PREF_SAVED_VISUALS = "saved_visual_editor_figures";
    private static final int MAX_SAVED_VISUALS = 6;
    private static final int MAX_FAVORITES = 12;
    private static final String PREF_TUTORIAL_SHOWN = "tutorial_shown";
    private static final long PROFILE_REFRESH_COOLDOWN_MS = 60L * 1000L;
    private static final long FAVORITES_REFRESH_COOLDOWN_MS = 15L * 1000L;
    private ScrollView mainScroll;
    private LinearLayout pullRefreshChip;
    private CircularPullProgressView pullRefreshSpinner;
    private TextView pullRefreshText;
    private long lastSameNickRefreshAt = 0L;
    private float pullStartY = 0f;
    private boolean pullStartedAtTop = false;
    private boolean pullReadyToRefresh = false;
    private boolean pullDragging = false;
    private final ArrayList<ProfileHistoryItem> openedProfilesHistory = new ArrayList<>();
    private final ArrayList<ProfileHistoryItem> favoriteProfiles = new ArrayList<>();
    private String currentHotelKey = "br";

    private InterstitialAd interstitialAd;
    private boolean interstitialLoading = false;
    private long lastInterstitialShownAt = 0L;
    private int profileOpenActionsSinceAd = 0;
    private int interstitialLoadFailureCount = 0;
    private long nextInterstitialLoadAllowedAt = 0L;
    private Runnable interstitialRetryRunnable = null;
    private static final String REAL_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-8079226281001828/5039255014";
    private static final String TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712";
    private static final String REAL_REWARDED_AD_UNIT_ID = "ca-app-pub-8079226281001828/1283312609";
    private static final String TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String REAL_TOP_SEARCH_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/8377352885";
    private static final String REAL_PREVIOUS_STYLES_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/1381533840";
    private static final String REAL_FRIENDS_REMOVED_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/5249048126";
    private static final String REAL_VISUAL_COLORS_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/6444755891";
    private static final String REAL_VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID = "ca-app-pub-8079226281001828/9823552100";
    private static final String TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111";
    private static final boolean USE_TEST_ADS = false;
    private static final String INTERSTITIAL_AD_UNIT_ID = USE_TEST_ADS ? TEST_INTERSTITIAL_AD_UNIT_ID : REAL_INTERSTITIAL_AD_UNIT_ID;
    private static final String REWARDED_AD_UNIT_ID = USE_TEST_ADS ? TEST_REWARDED_AD_UNIT_ID : REAL_REWARDED_AD_UNIT_ID;
    private static final String TOP_SEARCH_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_TOP_SEARCH_BANNER_AD_UNIT_ID;
    private static final String PREVIOUS_STYLES_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_PREVIOUS_STYLES_BANNER_AD_UNIT_ID;
    private static final String FRIENDS_REMOVED_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_FRIENDS_REMOVED_BANNER_AD_UNIT_ID;
    private static final String VISUAL_COLORS_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_VISUAL_COLORS_BANNER_AD_UNIT_ID;
    private static final String VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID = USE_TEST_ADS ? TEST_BANNER_AD_UNIT_ID : REAL_VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID;
    private static final String TAG_SEARCH_BOX_ANCHOR = "search_box_anchor";
    private AdView topSearchBannerAdView;
    private FrameLayout topSearchBannerAdContainer;
    private boolean topSearchBannerLoadStarted = false;
    private AdView previousStylesBannerAdView;
    private FrameLayout previousStylesBannerAdContainer;
    private boolean previousStylesBannerLoadStarted = false;
    private AdView friendsRemovedBannerAdView;
    private FrameLayout friendsRemovedBannerAdContainer;
    private boolean friendsRemovedBannerLoadStarted = false;
    private AdView visualColorsBannerAdView;
    private FrameLayout visualColorsBannerAdContainer;
    private boolean visualColorsBannerLoadStarted = false;
    private AdView visualNickSearchBannerAdView;
    private FrameLayout visualNickSearchBannerAdContainer;
    private boolean visualNickSearchBannerLoadStarted = false;
    private static final long INTERSTITIAL_COOLDOWN_MS = 120L * 1000L;
    private static final int ACTIONS_BETWEEN_INTERSTITIALS = 1;
    private static final long AD_RETRY_BASE_DELAY_MS = 2L * 60L * 1000L;
    private static final long AD_RETRY_MAX_DELAY_MS = 30L * 60L * 1000L;
    private static final int AD_RETRY_MAX_SHIFT = 4;
    private RewardedAd rewardedAd;
    private boolean rewardedLoading = false;
    private int rewardedLoadFailureCount = 0;
    private long nextRewardedLoadAllowedAt = 0L;
    private Runnable rewardedRetryRunnable = null;
    private TextView rewardAdBtn;
    private TextView rewardAdTimeLabel;
    private boolean openingSplashShownThisSession = false;
    private JSONObject visualFigureDataCache = null;
    private long visualFigureDataLoadedAt = 0L;
    private static final String VISUAL_FIGUREDATA_URL = "https://atoxic.com.br/tools/converter_figuredata.php?json=1";
    private static final String VISUAL_FIGUREDATA_CACHE_URL = "https://atoxic.com.br/cache/figuredata-ui.json";
    private static final String VISUAL_FIGUREDATA_DISK_CACHE_FILE = "visual_figuredata_cache.json";
    private static final long VISUAL_FIGUREDATA_CACHE_TTL_MS = 24L * 60L * 60L * 1000L;
    private static final String[] VISUAL_PRELOAD_TYPES = new String[] {
            "hd", "hr", "ha", "he", "ea", "fa",
            "ch", "ca", "cc", "cp",
            "lg", "sh", "wa",
            "pt", "mc"
    };
    private static final String DEFAULT_VISUAL_FIGURE_MALE = "hd-180-22-0";
    private static final String DEFAULT_VISUAL_FIGURE_FEMALE = "hd-600-1-0";
    private static final String DEFAULT_VISUAL_FIGURE = DEFAULT_VISUAL_FIGURE_MALE;
    private long adFreeUntilMs = 0L;
    private final Runnable adFreeTicker = new Runnable() {
        @Override public void run() {
            consumeAdFreeElapsed();
            updateRewardButtonText();
            if (!removeAdsPurchased && !hasAdFreeAccess() && topSearchBannerAdView == null) {
                preloadBannerAds();
                attachTopSearchBannerIfPossible();
                loadInterstitialAd();
            }
            uiHandler.postDelayed(this, 1000L);
        }
    };
    private static final String PREF_AD_FREE_UNTIL_MS = "ad_free_until_ms";
    private static final String PREF_REMOVE_ADS_PURCHASED = "remove_ads_purchased";
    private static final String REMOVE_ADS_PRODUCT_ID = "remove_ads";
    private boolean removeAdsPurchased = false;
    private BillingClient billingClient;
    private ProductDetails removeAdsProductDetails;
    private boolean billingConnecting = false;
    private boolean billingReady = false;
    private boolean pendingRemoveAdsPurchaseLaunch = false;
    private static final long REWARDED_AD_FREE_MS = 30L * 60L * 1000L;
    private static final long MAX_AD_FREE_MS = 4L * 60L * 60L * 1000L;
    private long lastFavoritesPullRefreshAt = 0L;
    private boolean appInForeground = true;
    private static final long FAVORITE_ONLINE_FOREGROUND_INTERVAL_MS = 15L * 1000L;
    private static final long FAVORITE_ONLINE_BACKGROUND_INTERVAL_MS = 60L * 1000L;

    private final int bg = Color.rgb(13, 13, 18);
    private final int purple = Color.rgb(139, 52, 217);
    private final int purple2 = Color.rgb(106, 51, 143);
    private final int pink = Color.rgb(255, 79, 131);
    private final int blue = Color.rgb(53, 167, 255);
    private final int green = Color.rgb(73, 230, 160);
    private final int red = Color.rgb(255, 92, 92);
    private final int cardFill = Color.argb(22, 255, 255, 255);
    private final int cardStroke = Color.argb(28, 255, 255, 255);
    private final int muted = Color.argb(178, 255, 255, 255);
    private Typeface habboFont;
    private boolean lightTheme = false;
    private boolean notifyFavoriteOnline = true;
    private final ConcurrentHashMap<String, Boolean> favoriteOnlineStates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, FavoriteStatus> favoriteStatusCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> favoriteOnlineLastToast = new ConcurrentHashMap<>();
    private final ArrayList<TextView> favoriteOnlineBadgeViews = new ArrayList<>();
    private Runnable favoriteOnlineWatcher = null;
    private String visualEditorCachedFigure = DEFAULT_VISUAL_FIGURE;
    private String visualEditorCachedGender = "M";
    private String visualEditorCachedType = "hd";
    private int visualEditorCachedDirection = 2;
    private String loadingProfileFigureHint = "";
    private String loadingProfileUniqueIdHint = "";
    private String loadingProfileHotelHint = "";
    private ImageView loadingProfileAvatarImage = null;
    private final Map<String, View> visualItemViewsSessionCache = new HashMap<>();
    private final Map<String, Integer> visualItemRenderLimits = new HashMap<>();
    private final Set<String> visualCategoryLoading = Collections.synchronizedSet(new HashSet<>());

    private interface IntChangeListener {
        void onChange(int value);
    }

    private interface PullTouchListener {
        void onPullTouch(MotionEvent event);
    }

    private class PullDispatchFrameLayout extends FrameLayout {
        private PullTouchListener pullTouchListener;

        PullDispatchFrameLayout(Context context) {
            super(context);
        }

        void setPullTouchListener(PullTouchListener listener) {
            this.pullTouchListener = listener;
        }

        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            if (pullTouchListener != null) pullTouchListener.onPullTouch(event);
            return super.dispatchTouchEvent(event);
        }
    }

    @Override public void onCreate(Bundle b) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(b);
        try {
            if (getActionBar() != null) getActionBar().hide();
        } catch (Exception ignored) {}
        lightTheme = getSharedPreferences(PREFS, MODE_PRIVATE).getString("theme", "dark").equals("light");
        currentHotelKey = normalizeHotelKey(getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_HOTEL, ""));
        if (currentHotelKey.isEmpty()) {
            currentHotelKey = defaultHotelForDeviceLocale();
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        }
        try {
            habboFont = Typeface.createFromAsset(getAssets(), "fonts/ubuntu_habbo.ttf");
        } catch (Exception e) {
            habboFont = Typeface.create("sans-serif-condensed", Typeface.BOLD);
        }
        getWindow().setStatusBarColor(lightTheme ? Color.WHITE : bg);
        getWindow().setNavigationBarColor(lightTheme ? Color.rgb(245, 245, 245) : bg);
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = lightTheme ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            if (Build.VERSION.SDK_INT >= 26 && lightTheme) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
        loadOpenedProfilesHistory();
        loadFavoriteProfiles();
        loadFavoriteOnlineStatesFromPrefs();
        notifyFavoriteOnline = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, true);
        loadVisualEditorState();
        adFreeUntilMs = getSharedPreferences(PREFS, MODE_PRIVATE).getLong(PREF_AD_FREE_UNTIL_MS, 0L);
        removeAdsPurchased = getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_REMOVE_ADS_PURCHASED, false);
        MobileAds.initialize(this, initializationStatus -> {});
        buildUi();
        preloadBannerAds();
        attachTopSearchBannerIfPossible();
        initBillingClient();
        loadInterstitialAd();
        loadRewardedAd();
        requestFavoriteNotificationPermissionIfNeeded();
        startFavoriteOnlineWatcher();
        updateFavoriteOnlineAlarm();
    }
    private void requestFavoriteNotificationPermissionIfNeeded() {
        try {
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 2606);
            }
        } catch(Exception ignored) {}
    }

    private void applySystemBarsForTheme() {
        getWindow().setStatusBarColor(lightTheme ? Color.WHITE : bg);
        getWindow().setNavigationBarColor(lightTheme ? Color.WHITE : bg);
        if (Build.VERSION.SDK_INT >= 23) {
            int flags = lightTheme ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
            if (Build.VERSION.SDK_INT >= 26 && lightTheme) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }
    }

    private long calculateAdRetryDelayMs(int failureCount) {
        int safeFailureCount = Math.max(1, failureCount);
        int shift = Math.min(AD_RETRY_MAX_SHIFT, safeFailureCount - 1);
        long delay = AD_RETRY_BASE_DELAY_MS << shift;
        return Math.min(delay, AD_RETRY_MAX_DELAY_MS);
    }

    private void cancelInterstitialAdRetry() {
        if (interstitialRetryRunnable != null) {
            uiHandler.removeCallbacks(interstitialRetryRunnable);
            interstitialRetryRunnable = null;
        }
    }

    private void cancelRewardedAdRetry() {
        if (rewardedRetryRunnable != null) {
            uiHandler.removeCallbacks(rewardedRetryRunnable);
            rewardedRetryRunnable = null;
        }
    }

    private void resetInterstitialBackoff() {
        interstitialLoadFailureCount = 0;
        nextInterstitialLoadAllowedAt = 0L;
        cancelInterstitialAdRetry();
    }

    private void resetRewardedBackoff() {
        rewardedLoadFailureCount = 0;
        nextRewardedLoadAllowedAt = 0L;
        cancelRewardedAdRetry();
    }

    private void detachViewFromParent(View view) {
        try {
            if (view == null) return;
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) ((ViewGroup) parent).removeView(view);
        } catch (Exception ignored) {}
    }

    private FrameLayout newBannerContainer() {
        FrameLayout container = new FrameLayout(this);
        container.setPadding(0, dp(6), 0, dp(6));
        // Start as INVISIBLE instead of GONE so the banner can be measured when attached.
        // It becomes VISIBLE only after AdMob confirms an ad, and GONE if there is no fill/error.
        container.setVisibility(View.INVISIBLE);
        return container;
    }

    private AdView newBannerAdView(String adUnitId, FrameLayout container) {
        AdView adView = new AdView(this);
        adView.setAdUnitId(adUnitId);
        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (container != null && !hasAdFreeAccess()) container.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                if (container != null) container.setVisibility(View.GONE);
            }
        });
        container.addView(adView, new FrameLayout.LayoutParams(-2, -2, Gravity.CENTER));
        return adView;
    }

    private void loadBannerAfterAttach(final AdView adView, final FrameLayout container) {
        if (adView == null || container == null || removeAdsPurchased || hasAdFreeAccess()) return;
        container.setVisibility(View.INVISIBLE);
        container.post(() -> {
            try {
                if (removeAdsPurchased || hasAdFreeAccess()) {
                    container.setVisibility(View.GONE);
                    return;
                }
                if (container.getParent() == null) return;
                int widthPx = container.getWidth();
                if (widthPx <= 0) widthPx = getResources().getDisplayMetrics().widthPixels - dp(36);
                int adWidthDp = Math.max(1, (int) (widthPx / getResources().getDisplayMetrics().density));
                adView.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidthDp));
                adView.loadAd(new AdRequest.Builder().build());
            } catch (Exception ignored) {
                container.setVisibility(View.GONE);
            }
        });
    }

    private void requestTopSearchBannerLoadIfNeeded() {
        if (topSearchBannerLoadStarted || topSearchBannerAdView == null || topSearchBannerAdContainer == null) return;
        topSearchBannerLoadStarted = true;
        loadBannerAfterAttach(topSearchBannerAdView, topSearchBannerAdContainer);
    }

    private void requestPreviousStylesBannerLoadIfNeeded() {
        if (previousStylesBannerLoadStarted || previousStylesBannerAdView == null || previousStylesBannerAdContainer == null) return;
        previousStylesBannerLoadStarted = true;
        loadBannerAfterAttach(previousStylesBannerAdView, previousStylesBannerAdContainer);
    }

    private void requestFriendsRemovedBannerLoadIfNeeded() {
        if (friendsRemovedBannerLoadStarted || friendsRemovedBannerAdView == null || friendsRemovedBannerAdContainer == null) return;
        friendsRemovedBannerLoadStarted = true;
        loadBannerAfterAttach(friendsRemovedBannerAdView, friendsRemovedBannerAdContainer);
    }

    private void requestVisualColorsBannerLoadIfNeeded() {
        if (visualColorsBannerLoadStarted || visualColorsBannerAdView == null || visualColorsBannerAdContainer == null) return;
        visualColorsBannerLoadStarted = true;
        loadBannerAfterAttach(visualColorsBannerAdView, visualColorsBannerAdContainer);
    }

    private void requestVisualNickSearchBannerLoadIfNeeded() {
        if (visualNickSearchBannerLoadStarted || visualNickSearchBannerAdView == null || visualNickSearchBannerAdContainer == null) return;
        visualNickSearchBannerLoadStarted = true;
        loadBannerAfterAttach(visualNickSearchBannerAdView, visualNickSearchBannerAdContainer);
    }

    private void requestBannerLoadForContainer(View banner) {
        if (banner == topSearchBannerAdContainer) requestTopSearchBannerLoadIfNeeded();
        else if (banner == previousStylesBannerAdContainer) requestPreviousStylesBannerLoadIfNeeded();
        else if (banner == friendsRemovedBannerAdContainer) requestFriendsRemovedBannerLoadIfNeeded();
        else if (banner == visualColorsBannerAdContainer) requestVisualColorsBannerLoadIfNeeded();
        else if (banner == visualNickSearchBannerAdContainer) requestVisualNickSearchBannerLoadIfNeeded();
    }

    private void ensureTopSearchBannerAd() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        if (topSearchBannerAdContainer == null || topSearchBannerAdView == null) {
            topSearchBannerAdContainer = newBannerContainer();
            topSearchBannerAdView = newBannerAdView(TOP_SEARCH_BANNER_AD_UNIT_ID, topSearchBannerAdContainer);
            topSearchBannerLoadStarted = false;
        }
    }

    private void ensurePreviousStylesBannerAd() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        if (previousStylesBannerAdContainer == null || previousStylesBannerAdView == null) {
            previousStylesBannerAdContainer = newBannerContainer();
            previousStylesBannerAdView = newBannerAdView(PREVIOUS_STYLES_BANNER_AD_UNIT_ID, previousStylesBannerAdContainer);
            previousStylesBannerLoadStarted = false;
        }
    }

    private void ensureFriendsRemovedBannerAd() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        if (friendsRemovedBannerAdContainer == null || friendsRemovedBannerAdView == null) {
            friendsRemovedBannerAdContainer = newBannerContainer();
            friendsRemovedBannerAdView = newBannerAdView(FRIENDS_REMOVED_BANNER_AD_UNIT_ID, friendsRemovedBannerAdContainer);
            friendsRemovedBannerLoadStarted = false;
        }
    }

    private void ensureVisualColorsBannerAd() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        if (visualColorsBannerAdContainer == null || visualColorsBannerAdView == null) {
            visualColorsBannerAdContainer = newBannerContainer();
            visualColorsBannerAdView = newBannerAdView(VISUAL_COLORS_BANNER_AD_UNIT_ID, visualColorsBannerAdContainer);
            visualColorsBannerLoadStarted = false;
        }
    }

    private void ensureVisualNickSearchBannerAd() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        if (visualNickSearchBannerAdContainer == null || visualNickSearchBannerAdView == null) {
            visualNickSearchBannerAdContainer = newBannerContainer();
            visualNickSearchBannerAdView = newBannerAdView(VISUAL_NICK_SEARCH_BANNER_AD_UNIT_ID, visualNickSearchBannerAdContainer);
            visualNickSearchBannerLoadStarted = false;
        }
    }

    private View buildTopSearchBannerAd() {
        ensureTopSearchBannerAd();
        if (topSearchBannerAdContainer == null) return null;
        detachViewFromParent(topSearchBannerAdContainer);
        return topSearchBannerAdContainer;
    }

    private View buildPreviousStylesBannerAd() {
        ensurePreviousStylesBannerAd();
        if (previousStylesBannerAdContainer == null) return null;
        detachViewFromParent(previousStylesBannerAdContainer);
        return previousStylesBannerAdContainer;
    }

    private View buildFriendsRemovedBannerAd() {
        ensureFriendsRemovedBannerAd();
        if (friendsRemovedBannerAdContainer == null) return null;
        detachViewFromParent(friendsRemovedBannerAdContainer);
        return friendsRemovedBannerAdContainer;
    }

    private View buildVisualColorsBannerAd() {
        ensureVisualColorsBannerAd();
        if (visualColorsBannerAdContainer == null) return null;
        detachViewFromParent(visualColorsBannerAdContainer);
        return visualColorsBannerAdContainer;
    }

    private View buildVisualNickSearchBannerAd() {
        ensureVisualNickSearchBannerAd();
        if (visualNickSearchBannerAdContainer == null) return null;
        detachViewFromParent(visualNickSearchBannerAdContainer);
        return visualNickSearchBannerAdContainer;
    }

    private void addBannerToResultWrap(View banner, int bottomMarginDp) {
        if (banner == null || resultWrap == null) return;
        detachViewFromParent(banner);
        resultWrap.addView(banner, lp(-1, dp(68), 0, 0, 0, bottomMarginDp));
        requestBannerLoadForContainer(banner);
    }

    private void attachTopSearchBannerIfPossible() {
        if (root == null || removeAdsPurchased || hasAdFreeAccess()) return;
        View banner = buildTopSearchBannerAd();
        if (banner == null) return;
        detachViewFromParent(banner);
        int insertAt = -1;
        for (int i = 0; i < root.getChildCount(); i++) {
            Object tag = root.getChildAt(i).getTag();
            if (TAG_SEARCH_BOX_ANCHOR.equals(tag)) {
                insertAt = i;
                break;
            }
        }
        LinearLayout.LayoutParams params = lp(-1, dp(68), 0, 0, 0, 16);
        if (insertAt >= 0) root.addView(banner, Math.min(insertAt + 1, root.getChildCount()), params);
        else root.addView(banner, params);
        requestTopSearchBannerLoadIfNeeded();
    }

    private void preloadBannerAds() {
        if (removeAdsPurchased || hasAdFreeAccess()) return;
        ensureTopSearchBannerAd();
        ensurePreviousStylesBannerAd();
        ensureFriendsRemovedBannerAd();
        ensureVisualColorsBannerAd();
        ensureVisualNickSearchBannerAd();
    }

    private void pauseBannerAds() {
        try { if (topSearchBannerAdView != null) topSearchBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (previousStylesBannerAdView != null) previousStylesBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (friendsRemovedBannerAdView != null) friendsRemovedBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (visualColorsBannerAdView != null) visualColorsBannerAdView.pause(); } catch(Exception ignored) {}
        try { if (visualNickSearchBannerAdView != null) visualNickSearchBannerAdView.pause(); } catch(Exception ignored) {}
    }

    private void resumeBannerAds() {
        try { if (topSearchBannerAdView != null) topSearchBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (previousStylesBannerAdView != null) previousStylesBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (friendsRemovedBannerAdView != null) friendsRemovedBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (visualColorsBannerAdView != null) visualColorsBannerAdView.resume(); } catch(Exception ignored) {}
        try { if (visualNickSearchBannerAdView != null) visualNickSearchBannerAdView.resume(); } catch(Exception ignored) {}
    }

    private void destroyBannerAd(AdView adView, FrameLayout container) {
        try { if (adView != null) adView.destroy(); } catch(Exception ignored) {}
        detachViewFromParent(container);
    }

    private void destroyAllBannerAds() {
        destroyBannerAd(topSearchBannerAdView, topSearchBannerAdContainer);
        destroyBannerAd(previousStylesBannerAdView, previousStylesBannerAdContainer);
        destroyBannerAd(friendsRemovedBannerAdView, friendsRemovedBannerAdContainer);
        destroyBannerAd(visualColorsBannerAdView, visualColorsBannerAdContainer);
        destroyBannerAd(visualNickSearchBannerAdView, visualNickSearchBannerAdContainer);
        topSearchBannerAdView = null;
        topSearchBannerAdContainer = null;
        topSearchBannerLoadStarted = false;
        previousStylesBannerAdView = null;
        previousStylesBannerAdContainer = null;
        previousStylesBannerLoadStarted = false;
        friendsRemovedBannerAdView = null;
        friendsRemovedBannerAdContainer = null;
        friendsRemovedBannerLoadStarted = false;
        visualColorsBannerAdView = null;
        visualColorsBannerAdContainer = null;
        visualColorsBannerLoadStarted = false;
        visualNickSearchBannerAdView = null;
        visualNickSearchBannerAdContainer = null;
        visualNickSearchBannerLoadStarted = false;
    }

    private void registerInterstitialLoadFailure() {
        interstitialLoadFailureCount++;
        nextInterstitialLoadAllowedAt = System.currentTimeMillis() + calculateAdRetryDelayMs(interstitialLoadFailureCount);
        scheduleInterstitialAdRetry();
    }

    private void registerRewardedLoadFailure() {
        rewardedLoadFailureCount++;
        nextRewardedLoadAllowedAt = System.currentTimeMillis() + calculateAdRetryDelayMs(rewardedLoadFailureCount);
        scheduleRewardedAdRetry();
    }

    private void scheduleInterstitialAdRetry() {
        if (removeAdsPurchased || hasAdFreeAccess() || !appInForeground || interstitialRetryRunnable != null) return;
        long delay = Math.max(0L, nextInterstitialLoadAllowedAt - System.currentTimeMillis());
        interstitialRetryRunnable = () -> {
            interstitialRetryRunnable = null;
            loadInterstitialAd();
        };
        uiHandler.postDelayed(interstitialRetryRunnable, delay);
    }

    private void scheduleRewardedAdRetry() {
        if (removeAdsPurchased || !appInForeground || rewardedRetryRunnable != null) return;
        long delay = Math.max(0L, nextRewardedLoadAllowedAt - System.currentTimeMillis());
        rewardedRetryRunnable = () -> {
            rewardedRetryRunnable = null;
            loadRewardedAd();
        };
        uiHandler.postDelayed(rewardedRetryRunnable, delay);
    }

    private boolean canLoadInterstitialAdNow() {
        return !removeAdsPurchased && !hasAdFreeAccess() && System.currentTimeMillis() >= nextInterstitialLoadAllowedAt;
    }

    private boolean canLoadRewardedAdNow() {
        return !removeAdsPurchased && System.currentTimeMillis() >= nextRewardedLoadAllowedAt;
    }

    private void loadInterstitialAd() {
        if (removeAdsPurchased || hasAdFreeAccess()) {
            cancelInterstitialAdRetry();
            return;
        }
        if (interstitialLoading || interstitialAd != null) return;
        if (!canLoadInterstitialAdNow()) {
            scheduleInterstitialAdRetry();
            return;
        }

        interstitialLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(
                this,
                INTERSTITIAL_AD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(InterstitialAd ad) {
                        interstitialLoading = false;
                        interstitialAd = ad;
                        resetInterstitialBackoff();
                        interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                interstitialAd = null;
                                loadInterstitialAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                interstitialAd = null;
                                registerInterstitialLoadFailure();
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                interstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        interstitialLoading = false;
                        interstitialAd = null;
                        registerInterstitialLoadFailure();
                    }
                }
        );
    }

    private void maybeShowProfileInterstitial() {
        profileOpenActionsSinceAd++;

        long now = System.currentTimeMillis();
        boolean cooldownOk = now - lastInterstitialShownAt >= INTERSTITIAL_COOLDOWN_MS;
        boolean actionCountOk = profileOpenActionsSinceAd >= ACTIONS_BETWEEN_INTERSTITIALS;

        if (hasAdFreeAccess()) {
            cancelInterstitialAdRetry();
            return;
        }

        if (interstitialAd != null && cooldownOk && actionCountOk && !isFinishing()) {
            profileOpenActionsSinceAd = 0;
            lastInterstitialShownAt = now;
            interstitialAd.show(this);
        } else if (interstitialAd == null) {
            loadInterstitialAd();
        }
    }


    private void loadRewardedAd() {
        if (removeAdsPurchased) {
            cancelRewardedAdRetry();
            return;
        }
        if (rewardedLoading || rewardedAd != null) return;
        if (!canLoadRewardedAdNow()) {
            scheduleRewardedAdRetry();
            return;
        }

        rewardedLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(
                this,
                REWARDED_AD_UNIT_ID,
                adRequest,
                new RewardedAdLoadCallback() {
                    @Override
                    public void onAdLoaded(RewardedAd ad) {
                        rewardedLoading = false;
                        rewardedAd = ad;
                        resetRewardedBackoff();
                        rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                rewardedAd = null;
                                loadRewardedAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                rewardedAd = null;
                                registerRewardedLoadFailure();
                                toast(t("cannot_show_video"));
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                rewardedAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        rewardedLoading = false;
                        rewardedAd = null;
                        registerRewardedLoadFailure();
                    }
                }
        );
    }


    private void initBillingClient() {
        try {
            billingClient = BillingClient.newBuilder(this)
                    .setListener(new PurchasesUpdatedListener() {
                        @Override public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                            int code = billingResult == null ? BillingClient.BillingResponseCode.ERROR : billingResult.getResponseCode();
                            if (code == BillingClient.BillingResponseCode.OK && purchases != null) {
                                handleRemoveAdsPurchases(purchases, true);
                            } else if (code != BillingClient.BillingResponseCode.USER_CANCELED) {
                                runOnUiThread(() -> toast(t("purchase_error")));
                            }
                        }
                    })
                    .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
                    .enableAutoServiceReconnection()
                    .build();
            ensureBillingReady();
        } catch(Exception ignored) {}
    }

    private void ensureBillingReady() {
        try {
            if (billingClient == null) initBillingClient();
            if (billingClient == null || billingReady || billingConnecting || billingClient.isReady()) {
                billingReady = billingClient != null && billingClient.isReady();
                return;
            }
            billingConnecting = true;
            billingClient.startConnection(new BillingClientStateListener() {
                @Override public void onBillingSetupFinished(BillingResult billingResult) {
                    billingConnecting = false;
                    billingReady = billingResult != null && billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK;
                    if (billingReady) {
                        queryRemoveAdsProductDetails();
                        queryRemoveAdsPurchases();
                        if (pendingRemoveAdsPurchaseLaunch && removeAdsProductDetails != null) runOnUiThread(() -> launchRemoveAdsPurchase());
                    }
                }
                @Override public void onBillingServiceDisconnected() {
                    billingConnecting = false;
                    billingReady = false;
                }
            });
        } catch(Exception ignored) {
            billingConnecting = false;
            billingReady = false;
        }
    }

    private void queryRemoveAdsProductDetails() {
        try {
            if (billingClient == null || !billingClient.isReady()) return;
            ArrayList<QueryProductDetailsParams.Product> products = new ArrayList<>();
            products.add(QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(REMOVE_ADS_PRODUCT_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build());
            QueryProductDetailsParams params = QueryProductDetailsParams.newBuilder()
                    .setProductList(products)
                    .build();
            billingClient.queryProductDetailsAsync(params, new ProductDetailsResponseListener() {
                @Override public void onProductDetailsResponse(BillingResult billingResult, QueryProductDetailsResult result) {
                    if (billingResult == null || billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK || result == null) return;
                    List<ProductDetails> list = result.getProductDetailsList();
                    if (list != null && !list.isEmpty()) {
                        removeAdsProductDetails = list.get(0);
                        if (pendingRemoveAdsPurchaseLaunch) runOnUiThread(() -> launchRemoveAdsPurchase());
                    }
                }
            });
        } catch(Exception ignored) {}
    }

    private void queryRemoveAdsPurchases() {
        try {
            if (billingClient == null || !billingClient.isReady()) return;
            QueryPurchasesParams params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build();
            billingClient.queryPurchasesAsync(params, (billingResult, purchases) -> {
                if (billingResult == null || billingResult.getResponseCode() != BillingClient.BillingResponseCode.OK) return;
                boolean owned = false;
                if (purchases != null) {
                    for (Purchase purchase : purchases) {
                        if (isRemoveAdsPurchase(purchase) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                            owned = true;
                        }
                    }
                    handleRemoveAdsPurchases(purchases, false);
                }
                setRemoveAdsPurchased(owned);
            });
        } catch(Exception ignored) {}
    }

    private boolean isRemoveAdsPurchase(Purchase purchase) {
        if (purchase == null) return false;
        try { return purchase.getProducts() != null && purchase.getProducts().contains(REMOVE_ADS_PRODUCT_ID); } catch(Exception ignored) { return false; }
    }

    private void handleRemoveAdsPurchases(List<Purchase> purchases, boolean showToast) {
        if (purchases == null) return;
        for (Purchase purchase : purchases) {
            if (!isRemoveAdsPurchase(purchase)) continue;
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                setRemoveAdsPurchased(true);
                if (!purchase.isAcknowledged()) {
                    try {
                        AcknowledgePurchaseParams params = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.getPurchaseToken())
                                .build();
                        billingClient.acknowledgePurchase(params, billingResult -> {});
                    } catch(Exception ignored) {}
                }
                if (showToast) runOnUiThread(() -> toast(t("remove_ads_purchased")));
            } else if (purchase.getPurchaseState() == Purchase.PurchaseState.PENDING && showToast) {
                runOnUiThread(() -> toast(t("purchase_pending")));
            }
        }
    }

    private void setRemoveAdsPurchased(boolean purchased) {
        if (removeAdsPurchased == purchased) {
            runOnUiThread(this::updateRewardButtonText);
            return;
        }
        removeAdsPurchased = purchased;
        if (purchased) {
            cancelInterstitialAdRetry();
            cancelRewardedAdRetry();
            destroyAllBannerAds();
            interstitialAd = null;
            rewardedAd = null;
            interstitialLoading = false;
            rewardedLoading = false;
        }
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_REMOVE_ADS_PURCHASED, purchased).apply();
        runOnUiThread(this::updateRewardButtonText);
    }

    private void launchRemoveAdsPurchase() {
        try {
            if (removeAdsPurchased) {
                toast(t("remove_ads_purchased"));
                updateRewardButtonText();
                return;
            }
            if (billingClient == null || !billingClient.isReady()) {
                pendingRemoveAdsPurchaseLaunch = true;
                ensureBillingReady();
                uiHandler.postDelayed(() -> { if (pendingRemoveAdsPurchaseLaunch && (billingClient == null || !billingClient.isReady())) toast(t("purchase_loading")); }, 1800L);
                return;
            }
            if (removeAdsProductDetails == null) {
                pendingRemoveAdsPurchaseLaunch = true;
                queryRemoveAdsProductDetails();
                uiHandler.postDelayed(() -> { if (pendingRemoveAdsPurchaseLaunch && removeAdsProductDetails != null) launchRemoveAdsPurchase(); }, 900L);
                uiHandler.postDelayed(() -> { if (pendingRemoveAdsPurchaseLaunch && removeAdsProductDetails == null) toast(t("purchase_loading")); }, 2200L);
                return;
            }
            pendingRemoveAdsPurchaseLaunch = false;
            BillingFlowParams.ProductDetailsParams.Builder productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(removeAdsProductDetails);
            try {
                List<ProductDetails.OneTimePurchaseOfferDetails> offers = removeAdsProductDetails.getOneTimePurchaseOfferDetailsList();
                if (offers != null && !offers.isEmpty()) {
                    String offerToken = offers.get(0).getOfferToken();
                    if (offerToken != null && !offerToken.trim().isEmpty()) productParamsBuilder.setOfferToken(offerToken);
                }
            } catch(Exception ignored) {}
            ArrayList<BillingFlowParams.ProductDetailsParams> productParams = new ArrayList<>();
            productParams.add(productParamsBuilder.build());
            BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productParams)
                    .build();
            BillingResult result = billingClient.launchBillingFlow(this, flowParams);
            if (result == null || result.getResponseCode() != BillingClient.BillingResponseCode.OK) {
                if (result != null && result.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
                    queryRemoveAdsPurchases();
                } else {
                    pendingRemoveAdsPurchaseLaunch = false;
                    toast(t("purchase_error"));
                }
            }
        } catch(Exception e) {
            toast(t("purchase_error"));
        }
    }

    private void showRewardedAdDialog() {
        loadRewardedAd();
        consumeAdFreeElapsed();
        String remaining = formatAdFreeRemaining();
        String message = hasAdFreeAccess()
                ? tr("adfree_msg_add", remaining)
                : t("adfree_msg_new");

        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(18), dp(18), dp(18));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);

        LinearLayout iconLine = new LinearLayout(this);
        iconLine.setGravity(Gravity.CENTER);
        ImageView icon = new ImageView(this);
        icon.setImageDrawable(new RewardVideoDrawable());
        iconLine.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));
        wrap.addView(iconLine, lp(-1, dp(58), 0, 0, 0, 10));

        TextView title = toxicLogoText(t("adfree_title"), 21);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 10));

        TextView msg = text(message, 14, lightTheme ? Color.rgb(55,55,55) : Color.argb(226,255,255,255), false);
        msg.setGravity(Gravity.CENTER);
        msg.setLineSpacing(dp(3), 1f);
        msg.setPadding(dp(8), dp(8), dp(8), dp(8));
        msg.setBackground(round(lightTheme ? Color.rgb(246,246,248) : Color.argb(18,255,255,255), dp(16), lightTheme ? Color.rgb(222,222,226) : Color.argb(28,255,255,255), 1));
        wrap.addView(msg, lp(-1, -2, 0, 0, 0, 14));

        if (hasAdFreeAccess()) {
            TextView timer = text(t("time_left") + ": " + formatAdFreeRemainingShort(), 13, lightTheme ? Color.rgb(50,50,50) : Color.WHITE, true);
            timer.setGravity(Gravity.CENTER);
            timer.setPadding(dp(10), dp(8), dp(10), dp(8));
            timer.setBackground(round(lightTheme ? Color.rgb(238,238,242) : Color.argb(24,255,255,255), dp(999), lightTheme ? Color.rgb(216,216,222) : Color.argb(30,255,255,255), 1));
            wrap.addView(timer, lp(-1, -2, 0, 0, 0, 14));
        }

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setGravity(Gravity.CENTER);
        wrap.addView(buttons, lp(-1, dp(48), 0, 0, 0, 0));

        TextView cancel = dialogButton(t("cancel"));
        cancel.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
        cancel.setBackground(round(lightTheme ? Color.rgb(242,242,244) : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(216,216,220) : Color.argb(30,255,255,255), 1));
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(48), 1);
        cp.rightMargin = dp(6);
        buttons.addView(cancel, cp);
        cancel.setOnClickListener(v -> dialog.dismiss());

        TextView watch = dialogButton(t("watch_video"));
        watch.setTextColor(Color.WHITE);
        watch.setBackground(grad(dp(14), purple2, purple));
        LinearLayout.LayoutParams wp = new LinearLayout.LayoutParams(0, dp(48), 1);
        wp.leftMargin = dp(6);
        buttons.addView(watch, wp);
        watch.setOnClickListener(v -> {
            dialog.dismiss();
            showRewardedAdForAdFreeTime();
        });

        View buyNoAds = buildNoAdsPurchaseBanner();
        LinearLayout.LayoutParams buyLp = new LinearLayout.LayoutParams(-1, dp(94));
        buyLp.topMargin = dp(12);
        wrap.addView(buyNoAds, buyLp);
        buyNoAds.setOnClickListener(v -> {
            dialog.dismiss();
            launchRemoveAdsPurchase();
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }
    }

    private View buildNoAdsPurchaseBanner() {
        FrameLayout banner = new FrameLayout(this);
        banner.setBackground(new NoAdsBannerDrawable());
        banner.setClickable(true);
        banner.setFocusable(true);
        banner.setPadding(dp(10), dp(8), dp(10), dp(8));

        ImageView crown = new ImageView(this);
        crown.setImageDrawable(new PremiumCrownDrawable());
        FrameLayout.LayoutParams crownLp = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.LEFT | Gravity.CENTER_VERTICAL);
        crownLp.leftMargin = dp(11);
        banner.addView(crown, crownLp);

        ImageView arrow = new ImageView(this);
        arrow.setImageDrawable(new PremiumArrowDrawable());
        FrameLayout.LayoutParams arrowLp = new FrameLayout.LayoutParams(dp(40), dp(40), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        arrowLp.rightMargin = dp(11);
        banner.addView(arrow, arrowLp);

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setGravity(Gravity.CENTER_VERTICAL);
        FrameLayout.LayoutParams textLp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER_VERTICAL);
        textLp.leftMargin = dp(60);
        textLp.rightMargin = dp(54);
        banner.addView(texts, textLp);

        TextView title = text(t("premium_title"), 16, Color.WHITE, true);
        title.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
        title.setSingleLine(false);
        title.setMaxLines(2);
        title.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= 26) title.setAutoSizeTextTypeUniformWithConfiguration(9, 16, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        texts.addView(title, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout subtitle = new LinearLayout(this);
        subtitle.setGravity(Gravity.CENTER_VERTICAL);
        subtitle.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(4);
        texts.addView(subtitle, subLp);

        ImageView adIcon = new ImageView(this);
        adIcon.setImageDrawable(new TinyNoAdDrawable());
        LinearLayout.LayoutParams adLp = new LinearLayout.LayoutParams(dp(17), dp(17));
        adLp.rightMargin = dp(6);
        subtitle.addView(adIcon, adLp);

        TextView sub = text(t("premium_remove_ads"), 12, Color.argb(232,255,255,255), false);
        sub.setSingleLine(false);
        sub.setMaxLines(2);
        sub.setIncludeFontPadding(false);
        if (Build.VERSION.SDK_INT >= 26) sub.setAutoSizeTextTypeUniformWithConfiguration(7, 12, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        subtitle.addView(sub, new LinearLayout.LayoutParams(0, -2, 1));

        TextView chip = text("✦  " + t("premium_pay_once"), 8, Color.rgb(50, 38, 8), true);
        chip.setGravity(Gravity.CENTER);
        chip.setSingleLine(true);
        chip.setMaxLines(1);
        chip.setIncludeFontPadding(false);
        chip.setPadding(dp(8), 0, dp(8), 0);
        chip.setBackground(round(Color.rgb(255, 193, 24), dp(999), Color.argb(80,255,255,255), 1));
        if (Build.VERSION.SDK_INT >= 26) chip.setAutoSizeTextTypeUniformWithConfiguration(6, 8, 1, android.util.TypedValue.COMPLEX_UNIT_SP);
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(-2, dp(18));
        chipLp.topMargin = dp(5);
        texts.addView(chip, chipLp);
        return banner;
    }

    private void showRewardedAdForAdFreeTime() {
        if (getAdFreeRemainingMs() >= MAX_AD_FREE_MS) {
            toast(t("limit_24h"));
            updateRewardButtonText();
            return;
        }

        if (rewardedAd == null) {
            toast(t("video_loading"));
            loadRewardedAd();
            return;
        }

        rewardedAd.show(this, (RewardItem rewardItem) -> grantAdFreeTime(REWARDED_AD_FREE_MS));
    }

    private void grantAdFreeTime(long millis) {
        long now = System.currentTimeMillis();
        long remaining = getAdFreeRemainingMs();
        long updatedRemaining = Math.min(MAX_AD_FREE_MS, Math.max(0L, remaining) + millis);
        adFreeUntilMs = now + updatedRemaining;
        saveAdFreeUntil();
        cancelInterstitialAdRetry();
        destroyAllBannerAds();
        updateRewardButtonText();
        toast(t("adfree_granted"));
    }

    private boolean hasAdFreeAccess() {
        return removeAdsPurchased || getAdFreeRemainingMs() > 0L;
    }

    private long getAdFreeRemainingMs() {
        long now = System.currentTimeMillis();
        long remaining = Math.max(0L, adFreeUntilMs - now);
        if (remaining <= 0L && adFreeUntilMs != 0L) {
            adFreeUntilMs = 0L;
            saveAdFreeUntil();
        }
        return remaining;
    }

    private void consumeAdFreeElapsed() {
        getAdFreeRemainingMs();
    }

    private void saveAdFreeUntil() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putLong(PREF_AD_FREE_UNTIL_MS, Math.max(0L, adFreeUntilMs)).apply();
    }

    private void saveAdFreeRemaining() {
        saveAdFreeUntil();
    }

    private void updateRewardButtonText() {
        if (rewardAdBtn == null) return;
        if (removeAdsPurchased) {
            rewardAdBtn.setVisibility(View.GONE);
            if (rewardAdTimeLabel != null) rewardAdTimeLabel.setVisibility(View.GONE);
            return;
        }
        rewardAdBtn.setVisibility(View.VISIBLE);
        long remainingMs = getAdFreeRemainingMs();

        rewardAdBtn.setText("");
        rewardAdBtn.setTextColor(Color.WHITE);

        if (rewardAdTimeLabel != null) {
            if (remainingMs > 0L) {
                rewardAdTimeLabel.setText(formatAdFreeRemainingShort());
                rewardAdTimeLabel.setTextColor(lightTheme ? Color.rgb(45,45,45) : Color.WHITE);
                rewardAdTimeLabel.setVisibility(View.VISIBLE);
            } else {
                rewardAdTimeLabel.setText("");
                rewardAdTimeLabel.setVisibility(View.GONE);
            }
        }
    }

    private String formatAdFreeRemainingShort() {
        long totalSeconds = Math.max(0L, getAdFreeRemainingMs()) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) return String.format(Locale.ROOT, "%02d:%02dh", hours, minutes);
        return String.format(Locale.ROOT, "%02d:%02dm", minutes, seconds);
    }

    private String formatAdFreeRemaining() {
        long totalSeconds = Math.max(0L, getAdFreeRemainingMs()) / 1000L;
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        if (hours > 0L) return hours + "h " + minutes + "min";
        if (minutes > 0L) return minutes + "min " + seconds + "s";
        return seconds + "s";
    }

    @Override protected void onResume() {
        super.onResume();
        appInForeground = true;
        resumeBannerAds();
        if (removeAdsPurchased || hasAdFreeAccess()) destroyAllBannerAds();
        else { preloadBannerAds(); attachTopSearchBannerIfPossible(); }
        loadFavoriteOnlineStatesFromPrefs();
        updateFavoriteOnlineBadgeText();
        uiHandler.removeCallbacks(adFreeTicker);
        uiHandler.post(adFreeTicker);
        startFavoriteOnlineWatcher();
        ensureBillingReady();
        queryRemoveAdsPurchases();
        if (!removeAdsPurchased) {
            if (!hasAdFreeAccess()) loadInterstitialAd();
            loadRewardedAd();
        }
        checkFavoriteOnlineNotifications();
    }

    @Override protected void onPause() {
        appInForeground = false;
        pauseBannerAds();
        saveAdFreeUntil();
        uiHandler.removeCallbacks(adFreeTicker);
        cancelInterstitialAdRetry();
        cancelRewardedAdRetry();
        startFavoriteOnlineWatcher();
        super.onPause();
    }

    @Override protected void onDestroy() {
        saveAdFreeUntil();
        uiHandler.removeCallbacks(adFreeTicker);
        cancelInterstitialAdRetry();
        cancelRewardedAdRetry();
        destroyAllBannerAds();
        if (favoriteOnlineWatcher != null) uiHandler.removeCallbacks(favoriteOnlineWatcher);
        try { if (billingClient != null && billingClient.isReady()) billingClient.endConnection(); } catch(Exception ignored) {}
        executor.shutdownNow();
        super.onDestroy();
    }

    private void buildUi() {
        screen = new PullDispatchFrameLayout(this);
        ((PullDispatchFrameLayout) screen).setPullTouchListener(this::handleMainPullToRefreshDispatch);
        screen.setBackground(makeBg());
        ScrollView scroll = new ScrollView(this);
        mainScroll = scroll;
        scroll.setFillViewport(true);
        scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        scroll.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && searchInput != null && searchInput.hasFocus() && !isTouchInsideView(searchInput, event)) {
                clearSearchFocus();
            }
            return false;
        });
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(26), dp(18), dp(86));
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));
        screen.addView(scroll, new FrameLayout.LayoutParams(-1, -1));

        pullRefreshChip = new LinearLayout(this);
        pullRefreshChip.setOrientation(LinearLayout.HORIZONTAL);
        pullRefreshChip.setGravity(Gravity.CENTER_VERTICAL);
        pullRefreshChip.setPadding(dp(14), dp(10), dp(14), dp(10));
        pullRefreshChip.setBackground(round(lightTheme ? Color.WHITE : Color.rgb(36, 24, 54), dp(999), lightTheme ? Color.rgb(216,216,216) : Color.argb(36,255,255,255), 1));
        pullRefreshChip.setAlpha(0f);
        pullRefreshChip.setTranslationY(-dp(40));
        pullRefreshChip.setVisibility(View.GONE);
        pullRefreshSpinner = new CircularPullProgressView(this);
        pullRefreshSpinner.setProgressPct(0);
        pullRefreshChip.addView(pullRefreshSpinner, new LinearLayout.LayoutParams(dp(32), dp(32)));
        pullRefreshText = text(t("updating_profile"), 13, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        LinearLayout.LayoutParams pullTxtLp = new LinearLayout.LayoutParams(-2, -2);
        pullTxtLp.leftMargin = dp(8);
        pullRefreshChip.addView(pullRefreshText, pullTxtLp);
        FrameLayout.LayoutParams pullLp = new FrameLayout.LayoutParams(-2, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        pullLp.topMargin = dp(12);
        screen.addView(pullRefreshChip, pullLp);
        screen.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && searchInput != null && searchInput.hasFocus() && !isTouchInsideView(searchInput, event)) {
                clearSearchFocus();
            }
            return false;
        });
        root.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && searchInput != null && searchInput.hasFocus() && !isTouchInsideView(searchInput, event)) {
                clearSearchFocus();
            }
            return false;
        });

        TextView historyBtn = text("", 22, lightTheme ? Color.rgb(33,33,33) : Color.argb(230,255,255,255), true);
        historyBtn.setGravity(Gravity.CENTER);
        historyBtn.setPadding(0, 0, 0, 0);
        historyBtn.setBackground(new HistoryClockDrawable());
        historyBtn.setOnClickListener(v -> showOpenedProfilesHistoryDialog());
        FrameLayout.LayoutParams historyLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.LEFT);
        historyLp.topMargin = dp(14);
        historyLp.leftMargin = dp(10);
        screen.addView(historyBtn, historyLp);

        rewardAdBtn = text("", 22, Color.WHITE, true);
        rewardAdBtn.setGravity(Gravity.CENTER);
        rewardAdBtn.setPadding(0, 0, 0, 0);
        rewardAdBtn.setIncludeFontPadding(false);
        rewardAdBtn.setBackground(new RewardVideoDrawable());
        rewardAdBtn.setOnClickListener(v -> showRewardedAdDialog());
        FrameLayout.LayoutParams rewardLp = new FrameLayout.LayoutParams(dp(38), dp(38), Gravity.TOP | Gravity.RIGHT);
        rewardLp.topMargin = dp(14);
        rewardLp.rightMargin = dp(10);
        screen.addView(rewardAdBtn, rewardLp);

        rewardAdTimeLabel = text("", 9, lightTheme ? Color.rgb(45,45,45) : Color.WHITE, true);
        rewardAdTimeLabel.setGravity(Gravity.CENTER);
        rewardAdTimeLabel.setIncludeFontPadding(false);
        rewardAdTimeLabel.setSingleLine(true);
        rewardAdTimeLabel.setVisibility(View.GONE);
        FrameLayout.LayoutParams rewardTimeLp = new FrameLayout.LayoutParams(dp(58), dp(16), Gravity.TOP | Gravity.RIGHT);
        rewardTimeLp.topMargin = dp(54);
        rewardTimeLp.rightMargin = dp(0);
        screen.addView(rewardAdTimeLabel, rewardTimeLp);

        updateRewardButtonText();
        


        ImageView topLogo = new ImageView(this);
        topLogo.setAdjustViewBounds(true);
        topLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        topLogo.setImageResource(R.drawable.toxic_top_logo);
        root.addView(topLogo, lp(-1, dp(75), 64, 0, 64, 4));

        LinearLayout subtitleRow = new LinearLayout(this);
        subtitleRow.setOrientation(LinearLayout.HORIZONTAL);
        subtitleRow.setGravity(Gravity.CENTER);
        root.addView(subtitleRow, lp(-1, dp(24), 0, 0, 0, 10));

        TextView subtitle = text(t("searching"), 14, muted, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTypeface(Typeface.DEFAULT_BOLD);
        subtitleRow.addView(subtitle, new LinearLayout.LayoutParams(-2, -2));

        ImageView selectedHotelFlag = new ImageView(this);
        selectedHotelFlag.setImageDrawable(new HotelFlagDrawable(currentHotelKey));
        LinearLayout.LayoutParams selectedFlagLp = new LinearLayout.LayoutParams(dp(24), dp(16));
        selectedFlagLp.leftMargin = dp(7);
        subtitleRow.addView(selectedHotelFlag, selectedFlagLp);

        LinearLayout searchOuter = neutralCard(dp(24));
        searchOuter.setTag(TAG_SEARCH_BOX_ANCHOR);
        searchOuter.setPadding(dp(16), dp(16), dp(16), dp(16));
        root.addView(searchOuter, lp(-1, -2, 0, 0, 0, 10));

        View topSearchBanner = buildTopSearchBannerAd();
        if (topSearchBanner != null) {
            root.addView(topSearchBanner, lp(-1, dp(68), 0, 0, 0, 16));
            requestTopSearchBannerLoadIfNeeded();
        }

        LinearLayout searchCard = neutralCard(dp(18));
        searchCard.setPadding(dp(14), dp(14), dp(14), dp(14));
        searchOuter.addView(searchCard, lp(-1, -2, 0, 0, 0, 0));

        searchInput = new EditText(this);
        searchInput.setSingleLine(true);
        searchInput.setHint(t("search_hint"));
        searchInput.setHintTextColor(lightTheme ? Color.rgb(117, 117, 117) : Color.argb(135,255,255,255));
        searchInput.setTextColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);
        searchInput.setTextSize(16);
        searchInput.setTypeface(habboFont);
        searchInput.setGravity(Gravity.CENTER_VERTICAL);
        searchInput.setPadding(dp(16), 0, dp(16), 0);
        searchInput.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(28,255,255,255), dp(14), lightTheme ? Color.rgb(210,210,210) : Color.argb(35,255,255,255), 1));
        searchInput.setCursorVisible(false);
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            searchInput.setCursorVisible(hasFocus);
            if (!hasFocus) setSuggestionsVisible(false);
        });
        searchCard.addView(searchInput, lp(-1, dp(42), 0, 0, 0, 8));

        suggestionsScroll = new ScrollView(this);
        suggestionsScroll.setVisibility(View.GONE);
        suggestionsScroll.setFillViewport(false);
        suggestionsScroll.setVerticalScrollBarEnabled(true);
        suggestionsScroll.setScrollbarFadingEnabled(false);
        suggestionsScroll.setNestedScrollingEnabled(true);
        suggestionsScroll.setOnTouchListener((v, event) -> {
            requestDisallowParents(v, true);
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) requestDisallowParents(v, false);
            return false;
        });
        tintScrollBar(suggestionsScroll);

        suggestionsBox = new LinearLayout(this);
        suggestionsBox.setOrientation(LinearLayout.VERTICAL);
        suggestionsScroll.addView(suggestionsBox, new ScrollView.LayoutParams(-1, -2));

        searchCard.addView(suggestionsScroll, lp(-1, dp(230), 0, 0, 0, 10));

        searchBtn = new Button(this);
        searchBtn.setText(t("search_button"));
        searchBtn.setTextColor(Color.WHITE);
        searchBtn.setTextSize(16);
        searchBtn.setAllCaps(false);
        searchBtn.setTypeface(Typeface.DEFAULT_BOLD);
        searchBtn.setBackground(grad(dp(16), purple2, Color.rgb(166, 42, 235)));
        searchCard.addView(searchBtn, lp(-1, dp(58), 0, 0, 0, 0));

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        progress.setVisibility(View.GONE);
        root.addView(progress, lp(-1, dp(34), 0, 0, 0, 2));
        statusText = text("", 14, Color.argb(210,255,255,255), false);
        statusText.setGravity(Gravity.CENTER);
        root.addView(statusText, lp(-1, -2, 0, 0, 0, 10));

        resultWrap = new LinearLayout(this);
        resultWrap.setOrientation(LinearLayout.VERTICAL);
        root.addView(resultWrap, lp(-1, -2, 0, 0, 0, 0));
        setContentView(screen);
        searchBtn.setOnClickListener(v -> {
            setSuggestionsVisible(false);
            clearSearchFocus();
            search();
        });
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            setSuggestionsVisible(false);
            clearSearchFocus();
            search();
            return true;
        });
        bindNickSuggestions();
        showStartState();
        if (!openingSplashShownThisSession) {
            showOpeningSplashOverlay();
        } else {
            addBottomNavigation(screen, 0, null);
        }
        maybeShowFirstRunTutorial();
    }

    private void showOpeningSplashOverlay() {
        if (screen == null) return;
        openingSplashShownThisSession = true;

        final FrameLayout splash = new FrameLayout(this);
        splash.setBackgroundColor(Color.BLACK);
        splash.setClickable(true);
        splash.setFocusable(true);

        LinearLayout splashCenter = new LinearLayout(this);
        splashCenter.setOrientation(LinearLayout.VERTICAL);
        splashCenter.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams centerLp = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        splash.addView(splashCenter, centerLp);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.toxic_logo_opening);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setPadding(dp(8), dp(8), dp(8), dp(8));
        splashCenter.addView(logo, new LinearLayout.LayoutParams(-1, dp(320)));

        LinearLayout disclaimerWrap = new LinearLayout(this);
        disclaimerWrap.setOrientation(LinearLayout.VERTICAL);
        disclaimerWrap.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams disclaimerLp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        disclaimerLp.bottomMargin = dp(10);
        splash.addView(disclaimerWrap, disclaimerLp);

        TextView disclaimer1 = text(t("disclaimer1"), 12, Color.argb(210,255,255,255), false);
        disclaimer1.setGravity(Gravity.CENTER);
        disclaimer1.setLineSpacing(dp(2), 1f);
        disclaimer1.setPadding(dp(26), dp(4), dp(26), 0);
        disclaimerWrap.addView(disclaimer1, new LinearLayout.LayoutParams(-1, -2));


        screen.addView(splash, new FrameLayout.LayoutParams(-1, -1));
        splash.bringToFront();

        uiHandler.postDelayed(() -> {
            splash.animate()
                    .alpha(0f)
                    .setDuration(260)
                    .withEndAction(() -> {
                        try { screen.removeView(splash); } catch (Exception ignored) {}
                        addBottomNavigation(screen, 0, null);
                    })
                    .start();
        }, 2000L);
    }


    private void maybeShowFirstRunTutorial() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        if (sp.getBoolean(PREF_TUTORIAL_SHOWN, false)) return;
        sp.edit().putBoolean(PREF_TUTORIAL_SHOWN, true).apply();
        uiHandler.postDelayed(() -> showTutorialOverlay(0), 2300L);
    }

    private void showTutorialOverlay(final int step) {
        if (screen == null) return;
        final int safeStep = Math.max(0, Math.min(2, step));

        final FrameLayout overlay = new FrameLayout(this);
        if (Build.VERSION.SDK_INT >= 21) overlay.setElevation(dp(80));
        overlay.setClickable(true);
        overlay.setFocusable(true);
        overlay.setBackground(new TutorialOverlayDrawable(safeStep));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(20), dp(18), dp(20), dp(18));
        card.setBackground(new TutorialCardDrawable());

        TextView stepChip = text((safeStep + 1) + "/3", 12, Color.WHITE, true);
        stepChip.setGravity(Gravity.CENTER);
        stepChip.setPadding(dp(10), dp(4), dp(10), dp(4));
        stepChip.setBackground(grad(dp(999), purple2, purple));
        LinearLayout.LayoutParams scp = new LinearLayout.LayoutParams(-2, dp(28));
        scp.gravity = Gravity.CENTER_HORIZONTAL;
        card.addView(stepChip, scp);

        TextView title = habboText(safeStep == 0 ? t("tutorial_settings_title") : (safeStep == 1 ? t("tutorial_search_title") : t("tutorial_history_title")), 22, true);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-1, -2);
        tp.setMargins(0, dp(10), 0, 0);
        card.addView(title, tp);

        TextView body = text(safeStep == 0 ? t("tutorial_settings_body") : (safeStep == 1 ? t("tutorial_search_body") : t("tutorial_history_body")), 14, Color.argb(230,255,255,255), false);
        body.setGravity(Gravity.CENTER);
        body.setLineSpacing(dp(4), 1f);
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-1, -2);
        bp.setMargins(0, dp(10), 0, dp(12));
        card.addView(body, bp);

        TextView hint = text(safeStep >= 2 ? t("tutorial_finish") : t("tap_to_continue"), 12, Color.argb(190,255,255,255), true);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(0, dp(4), 0, 0);
        card.addView(hint, new LinearLayout.LayoutParams(-1, -2));

        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cp.setMargins(dp(18), 0, dp(18), dp(76));
        overlay.addView(card, cp);

        overlay.setOnClickListener(v -> {
            try { screen.removeView(overlay); } catch (Exception ignored) {}
            if (safeStep < 2) showTutorialOverlay(safeStep + 1);
        });

        screen.addView(overlay, new FrameLayout.LayoutParams(-1, -1));
        overlay.bringToFront();
    }

    private void showStartState() {
        resultWrap.removeAllViews();
        LinearLayout c = sectionCard(t("ready_search"), 0, false);
        c.addView(centerNote(t("start_note")));
    }

    private void setSearchTextProgrammatically(String value) {
        suppressSuggestions = true;
        suggestionRequestId++;
        setSuggestionsVisible(false);
        if (searchInput != null) {
            programmaticSearchTextChange = true;
            searchInput.setText(value == null ? "" : value);
            searchInput.setSelection(searchInput.getText().length());
            programmaticSearchTextChange = false;
        }
    }

    private void search() {
        suppressSuggestions = true;
        suggestionRequestId++;
        setSuggestionsVisible(false);
        final String nick = searchInput.getText().toString().trim();
        final String nickKey = normalizeNickKey(nick);
        loadingProfileUniqueIdHint = "";
        loadingProfileFigureHint = "";
        loadingProfileHotelHint = normalizeHotelKey(currentHotelKey);
        if (nickKey.isEmpty()) { hidePullRefreshIndicator(); toast(t("type_nick_toast")); return; }

        if (searchInProgress && nickKey.equals(activeSearchNick)) {
            hidePullRefreshIndicator();
            toast(t("same_profile_loading"));
            return;
        }

        if (!searchInProgress && activeRenderedProfile != null && nickKey.equals(currentLoadedNick) && normalizeHotelKey(activeRenderedProfile.hotelKey).equals(currentHotelKey)) {
            long now = System.currentTimeMillis();
            long wait = PROFILE_REFRESH_COOLDOWN_MS - (now - lastSameNickRefreshAt);
            if (wait > 0) {
                hidePullRefreshIndicator();
                toast(tr("wait_refresh", Math.max(1, (int)Math.ceil(wait / 1000.0))));
                return;
            }
        }

        clearSearchFocus();
        setSuggestionsVisible(false);

        final int token = ++activeSearchToken;
        activeSearchNick = nickKey;
        searchInProgress = true;
        currentLoadedNick = "";
        currentProfilePrivate = false;
        inlineProgressPct = 0;
        inlineProgressMessage = "";
        visiblePhotosCount = PAGE_CHUNK;
        visibleStylesCount = PAGE_CHUNK;
        photosScrollX = 0;
        stylesScrollX = 0;
        pushCurrentProfileToHistory(nickKey);

        resultWrap.removeAllViews();
        setLoading(true, t("searching_profile") + " " + nick + "...");
        maybeShowProfileInterstitial();

        executor.execute(() -> {
            try {
                ProfileResult fresh = loadProfile(nick, false, token);
                if (!isActiveToken(token)) return;

                // Nunca reutiliza cache por nick em pesquisa manual: nicks podem ser reutilizados
                // por contas diferentes (ativa/banida). O cache seguro é por uniqueId.
                String resolvedCacheKey = !normalizeNickKey(fresh.uniqueId).isEmpty() ? normalizeNickKey(fresh.uniqueId) : nickKey;
                ProfileResult cached = getCachedProfile(resolvedCacheKey);
                final ProfileResult r = mergeFreshIntoCachedSafely(cached, fresh);
                putProfileCache(r, resolvedCacheKey);
                saveProfileCache(r, resolvedCacheKey);

                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    showInlineLoading(t("loading_details"));
                    renderProfile(r);
                });

                completeProfileSections(r, token);

                if (!isActiveToken(token)) return;
                putProfileCache(r, resolvedCacheKey);
                saveProfileCache(r, resolvedCacheKey);

                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    inlineProgressPct = 0;
                    inlineProgressMessage = "";
                    renderProfile(r);
                    statusText.setText("");
                    searchInProgress = false;
                    activeSearchNick = "";
                    currentLoadedNick = normalizeNickKey(r.name);
                    lastSameNickRefreshAt = System.currentTimeMillis();
                    searchBtn.setEnabled(true);
                    searchBtn.setText(t("search_button"));
                    hidePullRefreshIndicator();
                });
            } catch (ProfileNotFoundException e) {
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    activeSearchNick = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    hidePullRefreshIndicator();
                    showNotFoundState(e.nick, e.suggestions);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    activeSearchNick = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    hidePullRefreshIndicator();
                    showError(e.getMessage() == null ? t("error_search_profile") : e.getMessage());
                });
            }
        });
    }

    private String normalizeNickKey(String raw) {
        return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isActiveToken(int token) {
        return token == activeSearchToken;
    }

    private void openProfileReference(String name, String uniqueId, String figure, String hotelKey) {
        String hotel = normalizeHotelKey(hotelKey);
        if (!hotel.isEmpty()) {
            currentHotelKey = hotel;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        }
        String id = uniqueId == null ? "" : uniqueId.trim();
        loadingProfileUniqueIdHint = id;
        loadingProfileFigureHint = figure == null ? "" : figure.trim();
        loadingProfileHotelHint = normalizeHotelKey(currentHotelKey);
        String display = name == null ? "" : name.trim();
        if (display.isEmpty()) display = id;
        setSearchTextProgrammatically(display);
        clearSearchFocus();
        if (!id.isEmpty()) searchByUniqueId(id, display);
        else search();
    }

    private void searchByUniqueId(final String uniqueId, final String displayNick) {
        suppressSuggestions = true;
        suggestionRequestId++;
        setSuggestionsVisible(false);
        final String id = uniqueId == null ? "" : uniqueId.trim();
        final String idKey = normalizeNickKey(id);
        String previousLoadingIdHint = loadingProfileUniqueIdHint == null ? "" : loadingProfileUniqueIdHint.trim();
        if (!previousLoadingIdHint.equals(id)) loadingProfileFigureHint = "";
        loadingProfileUniqueIdHint = id;
        loadingProfileHotelHint = normalizeHotelKey(currentHotelKey);
        final String shownNick = displayNick == null || displayNick.trim().isEmpty() ? id : displayNick.trim();
        if (idKey.isEmpty()) { search(); return; }

        if (searchInProgress && idKey.equals(activeSearchNick)) {
            hidePullRefreshIndicator();
            toast(t("same_profile_loading"));
            return;
        }

        clearSearchFocus();
        setSuggestionsVisible(false);

        final int token = ++activeSearchToken;
        activeSearchNick = idKey;
        searchInProgress = true;
        currentLoadedNick = "";
        currentProfilePrivate = false;
        inlineProgressPct = 0;
        inlineProgressMessage = "";
        visiblePhotosCount = PAGE_CHUNK;
        visibleStylesCount = PAGE_CHUNK;
        photosScrollX = 0;
        stylesScrollX = 0;
        pushCurrentProfileToHistory(idKey);

        resultWrap.removeAllViews();
        setLoading(true, t("searching_profile") + " " + shownNick + "...");
        maybeShowProfileInterstitial();

        executor.execute(() -> {
            try {
                ProfileResult fresh = loadProfileByUniqueId(id, shownNick, false, token);
                if (!isActiveToken(token)) return;

                ProfileResult cached = getCachedProfile(idKey);
                final ProfileResult r = mergeFreshIntoCachedSafely(cached, fresh);
                putProfileCache(r, idKey);
                saveProfileCache(r, idKey);

                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    showInlineLoading(t("loading_details"));
                    renderProfile(r);
                });

                completeProfileSections(r, token);

                if (!isActiveToken(token)) return;
                putProfileCache(r, idKey);
                saveProfileCache(r, idKey);

                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    inlineProgressPct = 0;
                    inlineProgressMessage = "";
                    renderProfile(r);
                    statusText.setText("");
                    searchInProgress = false;
                    activeSearchNick = "";
                    currentLoadedNick = normalizeNickKey(r.uniqueId.isEmpty() ? r.name : r.uniqueId);
                    lastSameNickRefreshAt = System.currentTimeMillis();
                    searchBtn.setEnabled(true);
                    searchBtn.setText(t("search_button"));
                    hidePullRefreshIndicator();
                });
            } catch (ProfileNotFoundException e) {
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    activeSearchNick = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    showNotFoundState(shownNick, e.suggestions);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    searchInProgress = false;
                    activeSearchNick = "";
                    setLoading(false, "");
                    hidePullRefreshIndicator();
                    showError(e.getMessage() == null ? t("error_search_profile") : e.getMessage());
                });
            }
        });
    }

    private ProfileResult loadProfileByUniqueId(String uniqueId, String fallbackName, boolean includeSections, int token) throws Exception {
        ProfileResult r = new ProfileResult();
        r.searchedNick = fallbackName == null || fallbackName.trim().isEmpty() ? uniqueId : fallbackName.trim();
        r.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        r.hotelKey = currentHotelKey;

        JSONObject dexProfile = r.uniqueId.isEmpty() ? null : validProfileObject(unwrap(tryJson(habbodexProfileByUniqueUrl(r.uniqueId))));
        if (dexProfile != null && !isSameProfileId(r.uniqueId, dexProfile)) dexProfile = null;
        JSONObject officialProfile = r.uniqueId.isEmpty() ? null : tryJson(habboApiUrl("/api/public/users/" + enc(r.uniqueId) + "/profile"));
        JSONObject officialUser = officialProfile == null ? null : officialProfile.optJSONObject("user");
        JSONObject base = firstObject(validProfileObject(dexProfile), validProfileObject(officialUser), validProfileObject(officialProfile));
        if (base == null) throw new ProfileNotFoundException(r.searchedNick, new ArrayList<>());

        if (r.uniqueId.isEmpty()) r.uniqueId = firstText(base, "uniqueId", "id", "habboId");
        r.name = firstText(base, "name", "username", "habboName");
        if (r.name.isEmpty()) r.name = r.searchedNick;
        r.figure = firstText(base, "figureString", "figure", "figure_string");
        if (r.figure.isEmpty() && officialUser != null) r.figure = firstText(officialUser, "figureString", "figure", "figure_string");
        if (!r.figure.isEmpty()) updateLoadingProfileFigureHint(r.figure, token);
        if (r.figure.isEmpty()) r.figure = "hd-180-1";
        r.motto = firstText(base, "motto", "mission");
        if (r.motto.isEmpty() && officialUser != null) r.motto = firstText(officialUser, "motto", "mission");
        r.online = optBoolAny(base, false, "online", "isOnline");
        if (officialUser != null && officialUser.has("online")) r.online = officialUser.optBoolean("online", r.online);
        r.privateProfile = !optBoolAny(base, true, "profileVisible", "isProfileVisible", "visible");
        if (officialUser != null && officialUser.has("profileVisible")) r.privateProfile = !officialUser.optBoolean("profileVisible", true);
        if (officialProfile == null && !r.uniqueId.isEmpty()) r.privateProfile = true;
        if (isPrivateProfileFromSources(base, dexProfile, officialProfile, officialUser)) r.privateProfile = true;
        r.banned = optBoolTrue(base, "isBanned", "banned", "ban", "is_banned") || isBannedProfileFromSources(base, dexProfile, officialProfile, officialUser);
        r.memberSince = firstText(base, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
        if (r.memberSince.isEmpty() && officialUser != null) r.memberSince = firstText(officialUser, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
        r.lastAccess = firstText(base, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        if (r.lastAccess.isEmpty() && officialUser != null) r.lastAccess = firstText(officialUser, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        r.level = firstText(base, "currentLevel", "level");
        r.starGems = firstText(base, "starGemCount", "starGems");
        r.totalBadges = firstText(base, "totalBadges", "badgeCount", "badgesCount", "badgesTotal");
        r.previousNames = extractList(dexProfile, "previousNames");
        r.selectedBadges = extractListFromKeys(dexProfile, "selectedBadges", "badges");
        r.dexProfile = dexProfile;
        r.officialProfile = officialProfile;
        if (officialProfile != null) {
            r.friends = mergeLists(r.friends, extractList(officialProfile, "friends"));
            r.rooms = mergeLists(r.rooms, extractList(officialProfile, "rooms"));
            r.groups = mergeLists(r.groups, extractList(officialProfile, "groups"));
        }
        if (includeSections) completeProfileSections(r, activeSearchToken);
        return r;
    }

    private ProfileResult loadProfile(String nick, boolean includeSections, int token) throws Exception {
        ProfileResult r = new ProfileResult();
        r.searchedNick = nick;
        r.hotelKey = currentHotelKey;
        JSONObject habboPublic = validProfileObject(tryJson(habboApiUrl("/api/public/users?name=" + enc(nick))));
        if (habboPublic != null) {
            updateLoadingProfileFigureHint(firstText(habboPublic, "figureString", "figure", "figure_string"), token);
        }
        JSONObject dexByNameRaw = unwrap(tryJson(habbodexProfileByNameUrl(nick)));
        JSONObject dexByName = validProfileObject(dexByNameRaw);
        JSONObject suggest = unwrap(tryJson(habbodexSuggestUrl(nick)));

        // Se a API oficial achou uma conta, ela define a identidade correta da busca por nick.
        // Habbodex por nome pode devolver outra conta com o mesmo nick/case antigo (ex.: ativa + banida).
        // Só use dados do Habbodex por nome quando o uniqueId for o mesmo.
        if (habboPublic != null && dexByName != null && !isSameProfileObject(habboPublic, dexByName)) {
            dexByName = null;
        }

        r.habboPublic = habboPublic; r.dex = dexByName; r.suggest = suggest;
        JSONObject base = firstObject(validProfileObject(habboPublic), validProfileObject(dexByName));
        if (base == null) throw new ProfileNotFoundException(nick, filterExactPreviousNickSuggestions(suggest, nick));

        r.uniqueId = firstText(base, "uniqueId", "id", "habboId");
        if (r.uniqueId.isEmpty() && habboPublic != null) r.uniqueId = habboPublic.optString("uniqueId", "");
        r.name = firstText(base, "name", "username", "habboName");
        if (r.name.isEmpty()) r.name = nick;
        r.figure = firstText(base, "figureString", "figure", "figure_string");
        if (r.figure.isEmpty() && habboPublic != null) r.figure = habboPublic.optString("figureString", "");
        if (!r.figure.isEmpty()) updateLoadingProfileFigureHint(r.figure, token);
        if (r.figure.isEmpty()) r.figure = "hd-180-1";
        r.motto = firstText(base, "motto", "mission");
        if (r.motto.isEmpty() && habboPublic != null) r.motto = habboPublic.optString("motto", "");
        r.online = optBoolAny(base, false, "online", "isOnline");
        if (habboPublic != null && habboPublic.has("online")) r.online = habboPublic.optBoolean("online", r.online);
        r.privateProfile = !optBoolAny(base, true, "profileVisible", "isProfileVisible", "visible");
        if (habboPublic != null && habboPublic.has("profileVisible")) r.privateProfile = !habboPublic.optBoolean("profileVisible", true);
        if (isPrivateProfileFromSources(base, habboPublic, dexByName)) r.privateProfile = true;
        r.banned = isSameProfileObject(base, habboPublic) ? false : (optBoolTrue(base, "isBanned", "banned", "ban", "is_banned") || isBannedProfileFromSources(base, dexByName));
        r.memberSince = firstText(base, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
        if (r.memberSince.isEmpty() && habboPublic != null) r.memberSince = habboPublic.optString("memberSince", "");
        r.lastAccess = firstText(base, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        r.level = firstText(base, "currentLevel", "level");
        r.starGems = firstText(base, "starGemCount", "starGems");
        r.totalBadges = firstText(base, "totalBadges", "badgeCount", "badgesCount", "badgesTotal");
        r.previousNames = mergeLists(extractList(dexByName, "previousNames"), extractPreviousNamesFromSuggest(suggest, r.name));
        r.selectedBadges = extractListFromKeys(dexByName, "selectedBadges", "badges");

        if (!r.uniqueId.isEmpty()) {
            JSONObject dexProfile = validProfileObject(unwrap(tryJson(habbodexProfileByUniqueUrl(r.uniqueId))));
            if (dexProfile != null && !isSameProfileId(r.uniqueId, dexProfile)) dexProfile = null;
            if (dexProfile != null) {
                r.dexProfile = dexProfile;
                if (isPrivateProfileFromSources(dexProfile)) r.privateProfile = true;
                if (isBannedProfileFromSources(dexProfile)) r.banned = true;
                if (r.motto.isEmpty()) r.motto = firstText(dexProfile, "motto", "mission");
                if (r.memberSince.isEmpty()) r.memberSince = firstText(dexProfile, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
                if (r.lastAccess.isEmpty()) r.lastAccess = firstText(dexProfile, "lastAccessTime", "lastLoginTime", "lastOnline");

                if (r.level.isEmpty()) r.level = firstText(dexProfile, "currentLevel", "level");
                if (r.starGems.isEmpty()) r.starGems = firstText(dexProfile, "starGemCount", "starGems");
                if (r.totalBadges.isEmpty()) r.totalBadges = firstText(dexProfile, "totalBadges", "badgeCount", "badgesCount", "badgesTotal");
                r.previousNames = mergeLists(r.previousNames, extractList(dexProfile, "previousNames"));
                r.selectedBadges = mergeLists(r.selectedBadges, extractListFromKeys(dexProfile, "selectedBadges", "badges"));
            }

            JSONObject officialProfile = tryJson(habboApiUrl("/api/public/users/" + enc(r.uniqueId) + "/profile"));
            r.officialProfile = officialProfile;
            if (officialProfile == null && !r.uniqueId.isEmpty()) r.privateProfile = true;
            if (officialProfile != null && isPrivateProfileFromSources(officialProfile)) r.privateProfile = true;
            if (officialProfile != null) {
                JSONObject user = officialProfile.optJSONObject("user");
                if (user != null) {
                    if (isPrivateProfileFromSources(user)) r.privateProfile = true;
                    if (isBannedProfileFromSources(user)) r.banned = true;
                    if (r.level.isEmpty()) r.level = firstText(user, "currentLevel", "level");
                    if (r.starGems.isEmpty()) r.starGems = firstText(user, "starGemCount", "starGems");
                    if (r.totalBadges.isEmpty()) r.totalBadges = firstText(user, "totalBadges", "badgeCount", "badgesCount", "badgesTotal");
                    if (r.memberSince.isEmpty()) r.memberSince = firstText(user, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
                    if (r.lastAccess.isEmpty()) r.lastAccess = firstText(user, "lastAccessTime", "lastLoginTime", "lastOnline");
                    r.online = optBoolAny(user, r.online, "online", "isOnline");
                    r.selectedBadges = mergeLists(r.selectedBadges, extractListFromKeys(user, "selectedBadges", "badges"));
                }
                r.friends = mergeLists(r.friends, extractList(officialProfile, "friends"));
                r.rooms = mergeLists(r.rooms, extractList(officialProfile, "rooms"));
                r.groups = mergeLists(r.groups, extractList(officialProfile, "groups"));
            }
            if (includeSections) {
                completeProfileSections(r, activeSearchToken);
            }
        }
        return r;
    }

    private void completeProfileSections(ProfileResult r, int token) {
        if (r == null || r.uniqueId == null || r.uniqueId.isEmpty() || !isActiveToken(token)) return;

        PageResult photosPage = null;
        try { photosPage = fetchPageChunk(r.uniqueId, "photos", "photos", 1, PAGE_CHUNK, PAGE_CHUNK); } catch(Exception ignored) {}
        if (photosPage != null) applyPhotosPage(r, photosPage, true);
        try { enrichPhotoRoomInfo(r); } catch(Exception ignored) {}
        if (!isActiveToken(token)) return;
        putProfileCache(r, activeSearchNick);
        saveProfileCache(r, activeSearchNick);
        runOnUiThread(() -> {
            if (!isActiveToken(token)) return;
            showInlineLoading(t("loading_history"));
            renderProfile(r);
        });

        ArrayList<JSONObject> mottos = null;
        try { mottos = fetchAll(r.uniqueId, "previous-mottos", null, 100, 3); } catch(Exception ignored) {}
        if (mottos != null) r.previousMottos = mottos;
        if (!isActiveToken(token)) return;
        putProfileCache(r, activeSearchNick);
        saveProfileCache(r, activeSearchNick);
        runOnUiThread(() -> {
            if (!isActiveToken(token)) return;
            showInlineLoading(t("loading_styles_friends"));
            renderProfile(r);
        });

        PageResult badgesPage = null;
        try { badgesPage = fetchPage(r.uniqueId, "selected-badges", null, 1, 20); } catch(Exception ignored) {}
        if (badgesPage != null && badgesPage.items != null && !badgesPage.items.isEmpty()) r.selectedBadges = mergeLists(badgesPage.items, r.selectedBadges);

        PageResult allBadgesPage = null;
        try { allBadgesPage = fetchAllBadges(r.uniqueId, true, 100, 25); } catch(Exception ignored) {}
        if (allBadgesPage != null) {
            r.badges = allBadgesPage.items == null ? new ArrayList<>() : allBadgesPage.items;
            if (allBadgesPage.total > 0) r.totalBadges = String.valueOf(allBadgesPage.total);
        }
        PageResult allBadgesWithAchievementsPage = null;
        try { allBadgesWithAchievementsPage = fetchAllBadges(r.uniqueId, false, 100, 25); } catch(Exception ignored) {}
        if (allBadgesWithAchievementsPage != null) {
            r.badgesWithAchievements = allBadgesWithAchievementsPage.items == null ? new ArrayList<>() : allBadgesWithAchievementsPage.items;
            if (allBadgesWithAchievementsPage.total > 0) r.totalBadges = String.valueOf(allBadgesWithAchievementsPage.total);
        }
        enrichSelectedBadgesWithOwnership(r);
        if (!isActiveToken(token)) return;

        PageResult stylesPage = null;
        try { stylesPage = fetchPageChunk(r.uniqueId, "previous-styles", null, 1, PAGE_CHUNK, PAGE_CHUNK); } catch(Exception ignored) {}
        if (stylesPage != null) applyStylesPage(r, stylesPage, true);
        if (!isActiveToken(token)) return;

        ArrayList<JSONObject> friendsNow = null;
        try { friendsNow = fetchAll(r.uniqueId, "friends", "friends", 100, 50); } catch(Exception ignored) {}
        if (friendsNow != null) r.friends = mergeLists(friendsNow, r.friends);
        if (!isActiveToken(token)) return;

        ArrayList<JSONObject> removedFriends = null;
        try { removedFriends = fetchAll(r.uniqueId, "previous-friends", null, 100, 50); } catch(Exception ignored) {}
        if (removedFriends != null) r.oldFriends = removedFriends;
        if (!isActiveToken(token)) return;
        putProfileCache(r, activeSearchNick);
        saveProfileCache(r, activeSearchNick);
        runOnUiThread(() -> {
            if (!isActiveToken(token)) return;
            showInlineLoading(t("loading_rooms_groups"));
            renderProfile(r);
        });

        ArrayList<JSONObject> roomsNow = null;
        try { roomsNow = fetchAll(r.uniqueId, "rooms", "rooms", 100, 3); } catch(Exception ignored) {}
        if (roomsNow != null) r.rooms = mergeLists(roomsNow, r.rooms);
        if (!isActiveToken(token)) return;

        ArrayList<JSONObject> oldRoomsNow = null;
        try { oldRoomsNow = fetchAll(r.uniqueId, "previous-rooms", "rooms", 100, 3); } catch(Exception ignored) {}
        if (oldRoomsNow != null) r.oldRooms = oldRoomsNow;
        if (!isActiveToken(token)) return;

        ArrayList<JSONObject> groupsNow = null;
        try { groupsNow = fetchAll(r.uniqueId, "groups", "groups", 100, 3); } catch(Exception ignored) {}
        if (groupsNow != null) r.groups = groupsNow;
        if (!isActiveToken(token)) return;

        try { enrichPhotoRoomInfo(r); } catch(Exception ignored) {}
        putProfileCache(r, activeSearchNick);
        saveProfileCache(r, activeSearchNick);
    }

    private PageResult fetchBadgesPage(String uniqueId, int page, int limit, boolean hideAchievements) {
        PageResult out = new PageResult();
        out.page = Math.max(1, page);
        try {
            String url = habbodexEndpointUrl(uniqueId, "badges", out.page, limit) + "&hideAchievements=" + (hideAchievements ? "true" : "false");
            JSONObject pageData = unwrap(getJson(url));
            if (pageData == null) return out;
            out.items = extractList(pageData, "badges");
            if (out.items.isEmpty()) out.items = extractList(pageData, "result");
            if (out.items.isEmpty()) out.items = extractList(pageData, null);
            out.total = extractTotalCount(pageData);
            JSONObject next = pageData.optJSONObject("next");
            int nextPage = next == null ? 0 : next.optInt("page", 0);
            if (nextPage <= 0) {
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) nextPage = pagination.optInt("nextPage", 0);
            }
            out.nextPage = nextPage;
            out.hasMore = nextPage > 0 && nextPage != out.page;
        } catch(Exception ignored) {}
        return out;
    }

    private PageResult fetchAllBadges(String uniqueId, boolean hideAchievements, int limit, int maxPages) {
        PageResult out = new PageResult();
        out.page = 1;
        out.total = 0;
        out.hasMore = false;
        int page = 1;
        for (int i = 0; i < Math.max(1, maxPages); i++) {
            PageResult p = fetchBadgesPage(uniqueId, page, limit, hideAchievements);
            if (p == null) break;
            if (p.total > 0) out.total = Math.max(out.total, p.total);
            if (p.items == null || p.items.isEmpty()) break;
            out.items = mergeLists(out.items, p.items);
            if (!p.hasMore || p.nextPage <= 0 || p.nextPage == page) break;
            page = p.nextPage;
        }
        if (out.total <= 0) out.total = out.items.size();
        return out;
    }

    private void enrichSelectedBadgesWithOwnership(ProfileResult r) {
        if (r == null || r.selectedBadges == null || r.selectedBadges.isEmpty()) return;
        HashMap<String, JSONObject> byCode = new HashMap<>();
        addBadgesToLookup(byCode, r.badges);
        addBadgesToLookup(byCode, r.badgesWithAchievements);
        for (JSONObject selected : r.selectedBadges) {
            if (selected == null) continue;
            String code = firstText(selected, "code", "badgeCode");
            if (code.isEmpty()) continue;
            JSONObject full = byCode.get(code.toUpperCase(Locale.ROOT));
            if (full == null) continue;
            try {
                if (!selected.has("totalOwners") && full.has("totalOwners")) selected.put("totalOwners", full.opt("totalOwners"));
                if (firstText(selected, "name", "title").isEmpty() && !firstText(full, "name", "title").isEmpty()) selected.put("name", firstText(full, "name", "title"));
                if (firstText(selected, "description", "desc").isEmpty() && !firstText(full, "description", "desc").isEmpty()) selected.put("description", firstText(full, "description", "desc"));
                if (firstText(selected, "creationTime", "createdAt", "date").isEmpty() && !firstText(full, "creationTime", "createdAt", "date").isEmpty()) selected.put("creationTime", firstText(full, "creationTime", "createdAt", "date"));
            } catch(Exception ignored) {}
        }
    }

    private void addBadgesToLookup(HashMap<String, JSONObject> byCode, ArrayList<JSONObject> list) {
        if (byCode == null || list == null) return;
        for (JSONObject b : list) {
            if (b == null) continue;
            String code = firstText(b, "code", "badgeCode");
            if (!code.isEmpty()) byCode.put(code.toUpperCase(Locale.ROOT), b);
        }
    }

    private ArrayList<JSONObject> fetchAll(String uniqueId, String endpoint, String primaryKey, int limit, int maxPages) {
        ArrayList<JSONObject> out = new ArrayList<>();
        int page = 1;
        for (int i = 0; i < maxPages; i++) {
            try {
                JSONObject pageData = unwrap(getJson(habbodexEndpointUrl(uniqueId, endpoint, page, limit)));
                if (pageData == null) break;
                ArrayList<JSONObject> items = extractList(pageData, primaryKey);
                if (items.isEmpty()) break;
                out.addAll(items);
                JSONObject next = pageData.optJSONObject("next");
                int nextPage = next == null ? 0 : next.optInt("page", 0);
                if (nextPage <= 0) {
                    int totalPages = pageData.optInt("totalPages", pageData.optInt("pages", 0));
                    JSONObject pagination = pageData.optJSONObject("pagination");
                    if (pagination != null) {
                        totalPages = Math.max(totalPages, pagination.optInt("totalPages", pagination.optInt("pages", 0)));
                        nextPage = pagination.optInt("nextPage", 0);
                    }
                    if (nextPage <= 0 && totalPages > page) nextPage = page + 1;
                    if (nextPage <= 0 && items.size() >= limit) nextPage = page + 1;
                }
                if (nextPage <= 0 || nextPage == page || nextPage > maxPages) break;
                page = nextPage;
            } catch (Exception ignored) { break; }
        }
        return out;
    }



    private PageResult fetchPage(String uniqueId, String endpoint, String primaryKey, int page, int limit) {
        PageResult out = new PageResult();
        out.page = Math.max(1, page);
        out.nextPage = 0;
        out.hasMore = false;
        out.total = 0;
        try {
            JSONObject pageData = unwrap(getJson(habbodexEndpointUrl(uniqueId, endpoint, out.page, limit)));
            if (pageData == null) return out;
            out.items = extractList(pageData, primaryKey);
            out.total = extractTotalCount(pageData);
            JSONObject next = pageData.optJSONObject("next");
            int nextPage = next == null ? 0 : next.optInt("page", 0);
            if (nextPage <= 0) {
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) nextPage = pagination.optInt("nextPage", 0);
            }
            if (nextPage <= 0) {
                int totalPages = pageData.optInt("totalPages", pageData.optInt("pages", 0));
                JSONObject pagination = pageData.optJSONObject("pagination");
                if (pagination != null) totalPages = Math.max(totalPages, pagination.optInt("totalPages", pagination.optInt("pages", 0)));
                if (totalPages > out.page) nextPage = out.page + 1;
            }
            if (nextPage <= 0 && out.items.size() >= limit) nextPage = out.page + 1;
            out.nextPage = nextPage > out.page ? nextPage : 0;
            out.hasMore = out.nextPage > 0;
        } catch (Exception ignored) {}
        return out;
    }

    private PageResult fetchPageChunk(String uniqueId, String endpoint, String primaryKey, int startPage, int pageLimit, int desiredCount) {
        PageResult combined = new PageResult();
        combined.page = Math.max(1, startPage);
        combined.nextPage = 0;
        combined.hasMore = false;
        combined.total = 0;

        int page = combined.page;
        int safety = 0;
        int target = Math.max(1, desiredCount);
        int limit = Math.max(1, pageLimit);

        while (page > 0 && safety < 12 && combined.items.size() < target) {
            PageResult part = fetchPage(uniqueId, endpoint, primaryKey, page, limit);
            if (part == null) break;
            if (combined.total <= 0 && part.total > 0) combined.total = part.total;
            if (part.items == null || part.items.isEmpty()) {
                combined.nextPage = 0;
                combined.hasMore = false;
                break;
            }
            for (JSONObject item : part.items) {
                if (combined.items.size() >= target) break;
                combined.items.add(item);
            }
            combined.page = part.page;
            if (part.nextPage <= page || !part.hasMore) {
                combined.nextPage = 0;
                combined.hasMore = false;
                break;
            }
            page = part.nextPage;
            combined.nextPage = page;
            combined.hasMore = true;
            safety++;
        }

        if (combined.total > 0 && combined.items.size() < Math.min(target, combined.total) && combined.nextPage <= 0) {
            combined.nextPage = Math.max(startPage + 1, page + 1);
            combined.hasMore = true;
        }
        if (combined.total > 0 && combined.items.size() >= combined.total) {
            combined.nextPage = 0;
            combined.hasMore = false;
        }
        return combined;
    }

    private int extractTotalCount(JSONObject data) {
        if (data == null) return 0;
        int total = firstPositiveInt(data, "total", "totalItems", "totalCount", "count", "recordsTotal");
        JSONObject pagination = data.optJSONObject("pagination");
        if (total <= 0 && pagination != null) total = firstPositiveInt(pagination, "total", "totalItems", "totalCount", "count");
        JSONObject meta = data.optJSONObject("meta");
        if (total <= 0 && meta != null) total = firstPositiveInt(meta, "total", "totalItems", "totalCount", "count");
        return total;
    }

    private int firstPositiveInt(JSONObject data, String... keys) {
        if (data == null || keys == null) return 0;
        for (String key : keys) {
            if (data.has(key)) {
                int v = data.optInt(key, 0);
                if (v > 0) return v;
            }
        }
        return 0;
    }

    private void applyPhotosPage(ProfileResult r, PageResult page, boolean reset) {
        if (r == null || page == null) return;
        if (reset) r.photos.clear();
        r.photos = mergeLists(r.photos, page.items);
        if (page.total > 0) r.photosTotal = page.total;
        int total = r.photosTotal > 0 ? r.photosTotal : page.total;
        r.photosHasMore = page.hasMore || (total > 0 && r.photos.size() < total);
        r.photosNextPage = page.nextPage;
        if (r.photosHasMore && r.photosNextPage <= 0) r.photosNextPage = Math.max(2, page.page + 1);
        if (!r.photosHasMore) r.photosNextPage = 0;
    }

    private void applyStylesPage(ProfileResult r, PageResult page, boolean reset) {
        if (r == null || page == null) return;
        if (reset) r.previousStyles.clear();
        r.previousStyles = mergeLists(r.previousStyles, page.items);
        if (page.total > 0) r.stylesTotal = page.total;
        int total = r.stylesTotal > 0 ? r.stylesTotal : page.total;
        r.stylesHasMore = page.hasMore || (total > 0 && r.previousStyles.size() < total);
        r.stylesNextPage = page.nextPage;
        if (r.stylesHasMore && r.stylesNextPage <= 0) r.stylesNextPage = Math.max(2, page.page + 1);
        if (!r.stylesHasMore) r.stylesNextPage = 0;
    }

    private void loadMorePhotos(ProfileResult r, HorizontalScrollView photosHsv) {
        if (r == null || r.photosLoading || !r.photosHasMore || r.uniqueId == null || r.uniqueId.isEmpty()) return;
        final int token = activeSearchToken;
        final int page = r.photosNextPage <= 0 ? 2 : r.photosNextPage;
        r.photosLoading = true;
        photosScrollX = photosHsv == null ? 0 : photosHsv.getScrollX();
        renderProfile(r);
        executor.execute(() -> {
            try {
                PageResult next = fetchPageChunk(r.uniqueId, "photos", "photos", page, PAGE_CHUNK, PAGE_CHUNK);
                if (!isActiveToken(token)) return;
                applyPhotosPage(r, next, false);
                try { enrichPhotoRoomInfo(r); } catch(Exception ignored) {}
                putProfileCache(r, activeSearchNick);
                saveProfileCache(r, activeSearchNick);
            } catch (Exception ignored) {
            } finally {
                r.photosLoading = false;
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    renderProfile(r);
                });
            }
        });
    }

    private void loadMoreStyles(ProfileResult r, HorizontalScrollView stylesHsv) {
        if (r == null || r.stylesLoading || !r.stylesHasMore || r.uniqueId == null || r.uniqueId.isEmpty()) return;
        final int token = activeSearchToken;
        final int page = r.stylesNextPage <= 0 ? 2 : r.stylesNextPage;
        r.stylesLoading = true;
        stylesScrollX = stylesHsv == null ? 0 : stylesHsv.getScrollX();
        renderProfile(r);
        executor.execute(() -> {
            try {
                PageResult next = fetchPageChunk(r.uniqueId, "previous-styles", null, page, PAGE_CHUNK, PAGE_CHUNK);
                if (!isActiveToken(token)) return;
                applyStylesPage(r, next, false);
                putProfileCache(r, activeSearchNick);
                saveProfileCache(r, activeSearchNick);
            } catch (Exception ignored) {
            } finally {
                r.stylesLoading = false;
                runOnUiThread(() -> {
                    if (!isActiveToken(token)) return;
                    renderProfile(r);
                });
            }
        });
    }

    private ArrayList<JSONObject> fetchOfficialPhotos(String uniqueId) {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (uniqueId == null || uniqueId.trim().isEmpty()) return out;
        try {
            Object data = getJsonAny(habboApiUrl("/extradata/public/users/" + enc(uniqueId) + "/photos"));
            if (data instanceof JSONArray) {
                JSONArray a = (JSONArray)data;
                for (int i=0; i<a.length(); i++) {
                    JSONObject o = a.optJSONObject(i);
                    if (o != null) out.add(o);
                }
            } else if (data instanceof JSONObject) {
                out.addAll(extractList((JSONObject)data, null));
            }
        } catch(Exception ignored) {}
        return out;
    }

    private void renderProfile(ProfileResult r) {
        normalizeProfileState(r);
        activeRenderedProfile = r;
        rememberOpenedProfile(r);
        currentProfilePrivate = r != null && (r.privateProfile || r.banned);
        if (!searchInProgress) setLoading(false, "");
        resultWrap.removeAllViews();

        if (searchInProgress && inlineProgressMessage != null && !inlineProgressMessage.trim().isEmpty()) {
            resultWrap.addView(loadingProgressCard(inlineProgressMessage, inlineProgressPct), lp(-1, -2, 0, 0, 0, 12));
        }

        LinearLayout profile = card(dp(22));
        applyProfilePrivateBorder(profile, dp(22));
        profile.setPadding(dp(18), dp(18), dp(18), dp(18));
        resultWrap.addView(profile, lp(-1, -2, 0, 0, 0, 18));

        FrameLayout avatarFrame = new FrameLayout(this);
        avatarFrame.setBackground(round(lightTheme ? Color.rgb(252,252,252) : Color.rgb(15, 8, 25), dp(20), lightTheme ? Color.rgb(222,222,226) : Color.argb(22,255,255,255), 1));
        profile.addView(avatarFrame, lp(-1, dp(280), 0, 0, 0, 16));
        ImageView avatar = new ImageView(this);
        avatar.setAdjustViewBounds(true);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setPadding(dp(20), dp(10), dp(20), dp(84));
        avatarFrame.addView(avatar, new FrameLayout.LayoutParams(-1, -1));
        currentAvatarImage = avatar;

        TextView favoriteStar = text("", 22, Color.WHITE, true);
        favoriteStar.setGravity(Gravity.CENTER);
        favoriteStar.setPadding(0, 0, 0, 0);
        favoriteStar.setBackground(new FavoriteStarDrawable(isFavoriteProfile(r)));
        FrameLayout.LayoutParams favoriteStarLp = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP | Gravity.RIGHT);
        favoriteStarLp.topMargin = dp(10);
        favoriteStarLp.rightMargin = dp(10);
        avatarFrame.addView(favoriteStar, favoriteStarLp);
        favoriteStar.setOnClickListener(v -> {
            toggleFavoriteProfile(r);
            favoriteStar.setBackground(new FavoriteStarDrawable(isFavoriteProfile(r)));
        });
        currentProfileFigure = r.figure;
        avatarDirection = 2;
        updateProfileAvatar();

        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams cp = new FrameLayout.LayoutParams(-2, dp(40), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        cp.bottomMargin = dp(10);
        avatarFrame.addView(controls, cp);
        TextView left = roundIconButton("‹");
        TextView clothes = roundIconButton("shirt");
        TextView right = roundIconButton("›");
        controls.addView(left);
        controls.addView(clothes);
        controls.addView(right);
        left.setOnClickListener(v -> { avatarDirection = normalizeDirection(avatarDirection + 1); updateProfileAvatar(); });
        right.setOnClickListener(v -> { avatarDirection = normalizeDirection(avatarDirection - 1); updateProfileAvatar(); });
        clothes.setOnClickListener(v -> showClothesDialog(currentProfileFigure, t("current_look")));

        TextView name = habboText(r.name, 31, true);
        name.setGravity(Gravity.CENTER);
        profile.addView(name, lp(-1, -2, 0, 0, 0, 10));
        if (!r.motto.isEmpty()) {
            TextView motto = habboText(r.motto, 16, false);
            motto.setGravity(Gravity.CENTER);
            motto.setTextColor(lightTheme ? Color.rgb(70,70,70) : Color.argb(220,255,255,255));
            motto.setLineSpacing(dp(2), 1f);
            profile.addView(motto, lp(-1, -2, 0, 0, 0, 14));
        }
        LinearLayout badges = new LinearLayout(this);
        badges.setGravity(Gravity.CENTER);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        profile.addView(badges, lp(-1, -2, 0, 0, 0, 6));
        if (r.privateProfile) badges.addView(profileBadge(t("private"), "lock", red));
        if (r.banned) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, -2); p.leftMargin=dp(8); badges.addView(profileBadge(t("banned"), "banned", red), p); }

        addSelectedBadges(r.selectedBadges);
        addPreviousNames(r.previousNames);
        addPhotos(r);
        addPreviousMottos(r.previousMottos);
        addPreviousStyles(r);
        addStats(r);
        addFriendsTabs(r.friends, r.oldFriends);
        addRoomsTabs(r.rooms, r.oldRooms);
        addGroups(r.groups);
        addBadgesSection(r);
    }

    private LinearLayout profileBadge(String label, String icon, int color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(8), dp(5), dp(10), dp(5));
        row.setBackground(round(adjustAlpha(color, 0.32f), dp(999), adjustAlpha(color, 0.55f), 1));
        View badgeIcon;
        if ("banned".equals(icon)) {
            TextView bannedChar = habboText("ª", 10, true);
            bannedChar.setGravity(Gravity.CENTER);
            bannedChar.setIncludeFontPadding(false);
            bannedChar.setTextColor(Color.WHITE);
            badgeIcon = bannedChar;
        } else {
            badgeIcon = new IconView(this, icon);
        }
        row.addView(badgeIcon, new LinearLayout.LayoutParams(dp(14), dp(14)));
        TextView tv = text(label, 13, Color.WHITE, true);
        tv.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(-2, -2); tp.leftMargin = dp(6);
        row.addView(tv, tp);
        return row;
    }

    private TextView roundIconButton(String label) {
        TextView v = text("", 19, Color.WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setIncludeFontPadding(false);
        v.setElevation(dp(3));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(38), dp(34));
        p.setMargins(dp(4), 0, dp(4), 0);
        v.setLayoutParams(p);
        if ("shirt".equals(label)) {
            v.setBackground(new ShirtDrawable());
        } else {
            v.setBackground(new ArrowButtonDrawable("‹".equals(label)));
        }
        return v;
    }

    private void updateProfileAvatar() {
        if (currentAvatarImage != null && currentProfileFigure != null && !currentProfileFigure.isEmpty()) {
            loadAvatarImage(currentAvatarImage, avatarFull(currentProfileFigure, avatarDirection));
        }
    }

    private int normalizeDirection(int value) {
        while (value < 0) value += 8;
        while (value > 7) value -= 8;
        return value;
    }


    private void addStats(ProfileResult r) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        resultWrap.addView(wrap, lp(-1, -2, 0, 0, 0, 18));
        wrap.addView(statRow(r.online ? "status_online" : "status_offline", t("status"), r.online ? t("online") : t("offline")));
        wrap.addView(statRow("clock", t("last_login"), niceDate(r.lastAccess), timeAgoText(r.lastAccess)));
        wrap.addView(statRow("calendar", t("creation"), niceDateOnly(r.memberSince), timeAgoText(r.memberSince)));
        wrap.addView(statRow("friends", t("friends"), String.valueOf(r.friends.size())));
        wrap.addView(statRow("rooms", t("rooms"), String.valueOf(r.rooms.size())));
        wrap.addView(statRow("groups", t("groups"), String.valueOf(r.groups.size())));
        wrap.addView(statRow("photos", t("photos"), String.valueOf(r.photos.size())));
        wrap.addView(statRow("star", t("stars"), emptyDash(r.starGems)));
        wrap.addView(statRow("level", t("level"), emptyDash(r.level)));
        wrap.addView(statRow("badge", t("badges"), emptyDash(r.totalBadges)));
    }

    private LinearLayout statRow(String icon, String label, String value) {
        return statRow(icon, label, value, "");
    }

    private LinearLayout statRow(String icon, String label, String value, String tooltip) {
        LinearLayout row = card(dp(18));
        applyProfilePrivateBorder(row, dp(18));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        LinearLayout.LayoutParams rp = lp(-1, dp(54), 0, 0, 0, 7);
        row.setLayoutParams(rp);
        if ("status".equals(icon) || "status_online".equals(icon) || "status_offline".equals(icon)) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            row.addView(iv, new LinearLayout.LayoutParams(dp(20), dp(20)));
            boolean onlineStatusIcon = "status_online".equals(icon) || (value != null && value.trim().equalsIgnoreCase(t("online")));
            Glide.with(this).asGif().load(onlineStatusIcon ? R.drawable.online : R.drawable.offline).into(iv);
        } else {
            IconView iv = new IconView(this, icon);
            row.addView(iv, new LinearLayout.LayoutParams(dp(18), dp(18)));
        }
        LinearLayout texts = new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1); tp.leftMargin = dp(9); row.addView(texts, tp);
        texts.addView(text(label, 11, Color.argb(190,255,255,255), false));
        texts.addView(text(value == null || value.isEmpty() || "null".equalsIgnoreCase(value) ? "—" : value, 14, Color.WHITE, true));
        if (tooltip != null && !tooltip.trim().isEmpty() && !"—".equals(tooltip.trim())) {
            row.setOnClickListener(v -> toast(tooltip));
        }
        return row;
    }

    private void addSelectedBadges(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setFillViewport(true);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER);
        row.setMinimumWidth(getResources().getDisplayMetrics().widthPixels - dp(36));
        row.setPadding(dp(2), dp(2), dp(2), dp(2));
        hsv.addView(row);
        resultWrap.addView(hsv, lp(-1, dp(72), 0, 0, 0, 14));
        for (int i=0; i<Math.min(list.size(), 12); i++) {
            JSONObject b = list.get(i); String code = firstText(b, "code", "badgeCode");
            ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setPadding(dp(2), dp(2), dp(2), dp(2));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(50), dp(50)); p.rightMargin = dp(10); row.addView(img, p);
            if (!code.isEmpty()) loadImage(img, badgeImageUrl(code));
            final JSONObject badgeObj = b;
            img.setOnClickListener(v -> showBadgeDialog(badgeObj));
        }
    }

    private void addPreviousNames(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;
        LinearLayout c = sectionCard(t("previous_names"), list.size(), true);
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        sv.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        LinearLayout inner = new LinearLayout(this); inner.setOrientation(LinearLayout.VERTICAL);
        sv.addView(inner, new ScrollView.LayoutParams(-1, -2));
        c.addView(sv, lp(-1, dp(Math.min(220, Math.max(64, 68 * Math.min(list.size(), 4)))), 0, 0, 0, 0));
        for (int i=0; i<Math.min(list.size(), 40); i++) {
            JSONObject o = list.get(i);
            String n = firstText(o, "name");
            String d = firstText(o, "changedAt");
            inner.addView(historyItem(n.isEmpty()?"Nome anterior":n, niceDate(d)));
        }
    }

    private void addPreviousMottos(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;

        ArrayList<JSONObject> valid = new ArrayList<>();
        for (JSONObject item : list) {
            String m = firstText(item, "text");
            if (!m.isEmpty()) valid.add(item);
        }
        if (valid.isEmpty()) return;

        LinearLayout c = sectionCard(t("previous_mottos"), valid.size(), true);
        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        sv.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        sv.addView(inner, new ScrollView.LayoutParams(-1, -2));
        c.addView(sv, lp(-1, dp(Math.min(260, Math.max(74, 76 * Math.min(valid.size(), 4)))), 0, 0, 0, 0));
        for (int i=0; i<valid.size(); i++) {
            JSONObject o = valid.get(i);
            String m = firstText(o, "text");
            String d = firstText(o, "changedAt");
            inner.addView(historyItem(m, niceDate(d)));
        }
    }

    private LinearLayout historyItem(String main, String date) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(12), dp(10), dp(12), dp(10));
        box.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(16), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
        box.setLayoutParams(lp(-1, -2, 0, 0, 0, 10));
        TextView title = habboText(main == null ? "" : main, 16, true);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        title.setLineSpacing(dp(2), 1f);
        box.addView(title, lp(-1, -2, 0, 0, 0, 4));
        if (date != null && !date.isEmpty() && !date.equals("—")) {
            TextView d = text(date, 12, Color.argb(185,255,255,255), false);
            d.setGravity(Gravity.CENTER);
            box.addView(d, lp(-1, -2, 0, 0, 0, 0));
        }
        return box;
    }

    private TextView mottoItem(String main, String date) {
        TextView v = habboText(main + (date == null || date.isEmpty() || date.equals("—") ? "" : "\n" + date), 16, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(12), dp(12), dp(12), dp(12));
        v.setLineSpacing(dp(4), 1f);
        v.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        v.setBackground(round(lightTheme ? Color.rgb(245,245,245) : Color.argb(22,255,255,255), dp(16), Color.argb(24,255,255,255), 1));
        v.setLayoutParams(lp(-1, -2, 0, 0, 0, 10));
        return v;
    }

    private void addPreviousStyles(ProfileResult profileResult) {
        if (profileResult == null) return;
        ArrayList<JSONObject> list = profileResult.previousStyles;
        if (list.isEmpty() && !profileResult.stylesHasMore && !profileResult.stylesLoading) return;
        final int loaded = list.size();
        final int totalLabel = Math.max(profileResult.stylesTotal, loaded);
        LinearLayout c = sectionCardWithLoadMore(t("previous_styles"), loaded, totalLabel > 0 ? totalLabel : loaded, profileResult.stylesHasMore || profileResult.stylesLoading, profileResult.stylesLoading, () -> loadMoreStyles(profileResult, null));
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); hsv.addView(row);
        c.addView(hsv, lp(-1, dp(172), 0, 0, 0, 8));
        final HorizontalScrollView stylesHsv = hsv;
        if (stylesScrollX > 0) stylesHsv.post(() -> stylesHsv.scrollTo(stylesScrollX, 0));
        for (int i=0; i<loaded; i++) {
            JSONObject o = list.get(i);
            String fig = firstText(o, "figureString", "figure", "look");
            if (fig.isEmpty()) continue;
            LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER); box.setPadding(dp(8),dp(8),dp(8),dp(8)); box.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(18), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255),1));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(106), dp(162)); bp.rightMargin = dp(12); row.addView(box, bp);
            ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER); box.addView(img, new LinearLayout.LayoutParams(-1, dp(112)));
            loadImage(img, avatarSmall(fig));
            TextView dt = text(niceDate(firstText(o, "changedAt", "date", "createdAt", "creationTime")), 12, Color.argb(185,255,255,255), false); dt.setGravity(Gravity.CENTER); dt.setMaxLines(2); box.addView(dt, lp(-1,-2,0,4,0,0));
            final String finalFig = fig;
            box.setOnClickListener(v -> showClothesDialog(finalFig, niceDate(firstText(o, "changedAt", "date", "createdAt", "creationTime"))));
        }
        if (profileResult.stylesHasMore && !profileResult.stylesLoading) {
            View more = c.findViewWithTag("load_more_header_button");
            if (more != null) more.setOnClickListener(v -> loadMoreStyles(profileResult, stylesHsv));
        }
        addBannerToResultWrap(buildPreviousStylesBannerAd(), 18);
    }

    private void showClothesDialog(String figure, String date) {
        final Dialog dialog = new Dialog(this);

        LinearLayout rootDialog = new LinearLayout(this);
        rootDialog.setOrientation(LinearLayout.VERTICAL);
        rootDialog.setPadding(dp(18), dp(18), dp(18), dp(18));
        rootDialog.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(rootDialog);

        TextView title = text(t("looks") + " — " + (date == null ? "" : date), 18, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        rootDialog.addView(title, lp(-1,-2,0,0,0,12));

        View line = new View(this);
        line.setBackgroundColor(lightTheme ? Color.rgb(220,220,220) : Color.argb(35,255,255,255));
        rootDialog.addView(line, lp(-1,1,6,0,6,12));

        final ScrollView clothesScroll = new ScrollView(this);
        clothesScroll.setVerticalScrollBarEnabled(true);
        clothesScroll.setScrollbarFadingEnabled(false);
        tintScrollBar(clothesScroll);
        clothesScroll.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        final LinearLayout clothesContainer = new LinearLayout(this);
        clothesContainer.setOrientation(LinearLayout.VERTICAL);
        clothesScroll.addView(clothesContainer, new ScrollView.LayoutParams(-1, -2));
        rootDialog.addView(clothesScroll, lp(-1, dp(390), 0, 0, 0, 14));

        LinearLayout loadingBox = new LinearLayout(this);
        loadingBox.setOrientation(LinearLayout.HORIZONTAL);
        loadingBox.setGravity(Gravity.CENTER);
        ProgressBar clothesSpinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        if (Build.VERSION.SDK_INT >= 21) clothesSpinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(purple));
        loadingBox.addView(clothesSpinner, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView loading = text(t("loading_clothes"), 14, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, false);
        LinearLayout.LayoutParams ltp = new LinearLayout.LayoutParams(-2, -2);
        ltp.leftMargin = dp(10);
        loadingBox.addView(loading, ltp);
        clothesContainer.addView(loadingBox, lp(-1,-2,0,18,0,18));

        Button close = new Button(this);
        close.setText(t("close"));
        close.setAllCaps(false);
        close.setTextColor(Color.WHITE);
        close.setBackground(grad(dp(14), purple2, purple));
        rootDialog.addView(close, lp(-1, dp(48), 0, 0, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }

        executor.execute(() -> {
            try {
                JSONObject data = unwrap(getJson(habbodexFigureUrl(figure)));
                final ArrayList<JSONObject> clothes = normalizeClothingEntries(data);
                runOnUiThread(() -> {
                    clothesContainer.removeAllViews();
                    if (clothes.isEmpty()) {
                        clothesContainer.addView(mottoItem(t("no_clothes_found"), ""));
                        return;
                    }
                    for (int i=0; i<Math.min(clothes.size(), 40); i++) {
                        clothesContainer.addView(clothingRow(clothes.get(i)));
                    }
                });
            } catch (Exception ex) {
                runOnUiThread(() -> loading.setText(t("cannot_load_clothes")));
            }
        });
    }

    private LinearLayout clothingRow(JSONObject o) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(10),dp(12),dp(10)); row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(26,255,255,255), dp(14), lightTheme ? Color.rgb(220,220,220) : Color.argb(28,255,255,255),1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 10));
        ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER); row.addView(img, new LinearLayout.LayoutParams(dp(40), dp(40)));
        String code = firstText(o, "code", "classname", "className", "id");
        String icon = firstText(o, "iconUrl", "imageUrl", "url", "thumbnail");
        if (icon.isEmpty() && !code.isEmpty()) icon = "https://habbodex.com/images/furni/" + enc(code) + "/" + enc(code) + "_icon.png";
        if (!icon.isEmpty()) loadImage(img, icon);
        LinearLayout txt = new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1); tp.leftMargin = dp(12); row.addView(txt,tp);
        String name = clothingName(o, code);
        TextView nm = habboText(name.isEmpty()?t("item"):name, 15, true); nm.setMaxLines(2); nm.setEllipsize(TextUtils.TruncateAt.END); txt.addView(nm);
        String lineCode = clothingLineName(o, code);
        txt.addView(text(lineCode.isEmpty()?code:lineCode, 13, muted, false));
        return row;
    }


    private String clothingName(JSONObject o, String fallback) {
        String n = pickLocalizedValue(o == null ? null : o.optJSONObject("localeNames"), fallback);
        if (n.isEmpty()) n = firstText(o, "name", "publicName", "furniName", "classname", "className", "code");
        return n.isEmpty() ? fallback : n;
    }

    private String clothingLineName(JSONObject o, String fallback) {
        String n = "";
        if (o != null) {
            JSONObject line = o.optJSONObject("line");
            if (line != null) n = pickLocalizedValue(line.optJSONObject("localeNames"), "");
        }
        if (n.isEmpty()) n = firstText(o, "lineCode", "category", "_slot");
        return n.isEmpty() ? fallback : n;
    }

    private ArrayList<String> localeCandidateKeys() {
        ArrayList<String> keys = new ArrayList<>();
        String hotel = normalizeHotelKey(currentHotelKey);
        String lang = currentLang();
        addLocaleKey(keys, hotel);
        if ("com".equals(hotel)) addLocaleKey(keys, "us");
        if ("pt".equals(lang)) { addLocaleKey(keys, "br"); addLocaleKey(keys, "pt"); }
        if ("en".equals(lang)) addLocaleKey(keys, "us");
        addLocaleKey(keys, lang);
        addLocaleKey(keys, Locale.getDefault().getLanguage());
        String[] fallback = {"us", "br", "pt", "es", "fr", "de", "it", "nl", "tr", "fi"};
        for (String f : fallback) addLocaleKey(keys, f);
        return keys;
    }

    private void addLocaleKey(ArrayList<String> keys, String value) {
        if (value == null) return;
        String k = value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if (k.isEmpty()) return;
        if ("com".equals(k)) k = "us";
        if (!keys.contains(k)) keys.add(k);
    }

    private String pickLocalizedValue(JSONObject localeMap, String fallback) {
        if (localeMap == null) return fallback == null ? "" : fallback;
        for (String key : localeCandidateKeys()) {
            String v = localeMap.optString(key, "").trim();
            if (!v.isEmpty() && !"null".equalsIgnoreCase(v)) return v;
        }
        Iterator<String> it = localeMap.keys();
        while (it.hasNext()) {
            String v = localeMap.optString(it.next(), "").trim();
            if (!v.isEmpty() && !"null".equalsIgnoreCase(v)) return v;
        }
        return fallback == null ? "" : fallback;
    }

    private String firstNestedText(JSONObject o, String... path) {
        if (o == null || path == null || path.length == 0) return "";
        Object cur = o;
        for (String k : path) {
            if (!(cur instanceof JSONObject)) return "";
            cur = ((JSONObject)cur).opt(k);
            if (cur == null || cur == JSONObject.NULL) return "";
        }
        String s = String.valueOf(cur).trim();
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private ArrayList<JSONObject> normalizeClothingEntries(JSONObject clothingData) {
        ArrayList<JSONObject> out = new ArrayList<>();
        if (clothingData == null) return out;
        String[] slots = {"hr","hd","ch","lg","sh","ha","he","fa","cp","ca","cc","ea","mc","pt","wa"};
        for (String slot : slots) {
            JSONObject item = clothingData.optJSONObject(slot);
            if (item == null) continue;
            String code = firstText(item, "code", "classname", "className", "id");
            if (code.isEmpty()) continue;
            try { item.put("_slot", slot); } catch(Exception ignored) {}
            out.add(item);
        }
        if (!out.isEmpty()) return out;
        return extractList(clothingData, null);
    }

    private void enrichPhotoRoomInfo(ProfileResult r) {
        if (r == null || r.photos == null || r.photos.isEmpty()) return;
        HashMap<String, JSONObject> byRoom = new HashMap<>();
        if (r.rooms != null) for (JSONObject room : r.rooms) {
            String id = firstText(room, "id", "roomId", "room_id");
            if (!id.isEmpty()) byRoom.put(id, room);
        }
        for (JSONObject photo : r.photos) {
            String rid = getPhotoRoomId(photo);
            if (rid.isEmpty()) continue;
            JSONObject room = byRoom.get(rid);
            if (room != null) {
                try {
                    String rn = firstText(room, "name", "roomName", "caption", "title");
                    String ro = firstNestedText(room, "owner", "name");
                    String roFig = firstNestedText(room, "owner", "figureString");
                    if (roFig.isEmpty()) roFig = firstNestedText(room, "owner", "figure");
                    if (ro.isEmpty()) ro = firstText(room, "ownerName", "owner_name", "roomOwner");
                    if (roFig.isEmpty()) roFig = firstText(room, "ownerFigureString", "ownerFigure", "owner_figure_string");
                    if (!rn.isEmpty() && firstText(photo, "room_name", "roomName", "roomname").isEmpty()) photo.put("room_name", rn);
                    if (!ro.isEmpty() && getPhotoRoomOwnerName(photo).isEmpty()) photo.put("roomOwner", ro);
                    if (!roFig.isEmpty() && getPhotoRoomOwnerFigure(photo).isEmpty()) photo.put("roomOwnerFigureString", roFig);
                } catch(Exception ignored) {}
            }
            if (getPhotoRoomName(photo).isEmpty() || getPhotoRoomOwner(photo).isEmpty()) {
                JSONObject info = fetchRoomInfoById(rid);
                if (info != null) {
                    try {
                        String rn = firstText(info, "name", "roomName", "room_name", "caption", "title");
                        String ro = extractNameFromUnknown(info.opt("owner"));
                        if (ro.isEmpty()) ro = firstText(info, "ownerName", "owner_name", "roomOwner");
                        String roFig = extractFigureFromUnknown(info.opt("owner"));
                        if (roFig.isEmpty()) roFig = firstText(info, "ownerFigureString", "ownerFigure", "owner_figure_string");
                        if (!rn.isEmpty()) photo.put("room_name", rn);
                        if (!ro.isEmpty()) photo.put("roomOwner", ro);
                        if (!roFig.isEmpty()) photo.put("roomOwnerFigureString", roFig);
                    } catch(Exception ignored) {}
                }

            }
        }
    }

    private JSONObject fetchRoomInfoById(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) return null;
        String id = roomId.trim();
        String[] urls = new String[] {
            HABBODEX + "/roominfo/" + enc(habbodexHotelCode(currentHotelKey)) + "/room/" + enc(id),
            habboApiUrl("/api/public/rooms/" + enc(id)),
            "https://www.habbo.com/api/public/rooms/" + enc(id),
            HABBODEX + "/rooms/" + enc(habbodexHotelCode(currentHotelKey)) + "/" + enc(id),
            HABBODEX + "/room/" + enc(habbodexHotelCode(currentHotelKey)) + "/" + enc(id)
        };
        for (String url : urls) {
            try {
                JSONObject o = unwrap(getJson(url));
                if (o != null) return o;
            } catch(Exception ignored) {}
        }
        return null;
    }

    private String getPhotoRoomId(JSONObject photo) {
        String rid = firstText(photo, "roomId", "room_id", "roomid");
        if (!rid.isEmpty()) return rid;
        JSONObject room = photo == null ? null : photo.optJSONObject("room");
        if (room != null) rid = firstText(room, "id", "roomId", "room_id");
        return rid;
    }

    private String getPhotoRoomName(JSONObject photo) {
        String room = firstText(photo, "room_name", "roomName", "roomname");
        JSONObject roomObj = photo == null ? null : photo.optJSONObject("room");
        if (room.isEmpty() && roomObj != null) room = firstText(roomObj, "name", "roomName", "caption", "title");
        return room;
    }

    private String getPhotoRoomOwner(JSONObject photo) {
        return getPhotoRoomOwnerName(photo);
    }

    private String getPhotoRoomOwnerName(JSONObject photo) {
        if (photo == null) return "";
        String[] directKeys = {"roomOwner", "roomOwnerName", "ownerName", "owner_name"};
        for (String key : directKeys) {
            Object value = photo.opt(key);
            String name = extractNameFromUnknown(value);
            if (!name.isEmpty()) return name;
        }

        JSONObject roomObj = photo.optJSONObject("room");
        if (roomObj != null) {
            JSONObject owner = roomObj.optJSONObject("owner");
            String name = extractNameFromUnknown(owner);
            if (!name.isEmpty()) return name;

            for (String key : directKeys) {
                name = extractNameFromUnknown(roomObj.opt(key));
                if (!name.isEmpty()) return name;
            }
        }
        return "";
    }

    private String getPhotoRoomOwnerId(JSONObject photo) {
        if (photo == null) return "";
        String[] directKeys = {"roomOwner", "roomOwnerId", "ownerId", "owner", "user", "habbo"};
        for (String key : directKeys) {
            String id = extractUniqueIdFromUnknown(photo.opt(key));
            if (!id.isEmpty()) return id;
        }
        JSONObject roomObj = photo.optJSONObject("room");
        if (roomObj != null) {
            String id = extractUniqueIdFromUnknown(roomObj.opt("owner"));
            if (!id.isEmpty()) return id;
            for (String key : directKeys) {
                id = extractUniqueIdFromUnknown(roomObj.opt(key));
                if (!id.isEmpty()) return id;
            }
        }
        return "";
    }

    private String getPhotoRoomOwnerFigure(JSONObject photo) {
        if (photo == null) return "";
        String[] directKeys = {"roomOwnerFigureString", "ownerFigureString", "ownerFigure", "figureString", "figure"};

        for (String key : directKeys) {
            String figure = extractFigureFromUnknown(photo.opt(key));
            if (!figure.isEmpty()) return figure;
        }

        JSONObject roomObj = photo.optJSONObject("room");
        if (roomObj != null) {
            JSONObject owner = roomObj.optJSONObject("owner");
            String figure = extractFigureFromUnknown(owner);
            if (!figure.isEmpty()) return figure;

            for (String key : directKeys) {
                figure = extractFigureFromUnknown(roomObj.opt(key));
                if (!figure.isEmpty()) return figure;
            }
        }
        return "";
    }

    private String extractNameFromUnknown(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        if (value instanceof JSONObject) {
            JSONObject o = (JSONObject) value;
            String name = firstText(o, "name", "username", "habboName", "ownerName");
            if (!name.isEmpty()) return name;
            JSONObject owner = o.optJSONObject("owner");
            if (owner != null) return extractNameFromUnknown(owner);
            return "";
        }
        if (value instanceof JSONArray) {
            JSONArray a = (JSONArray) value;
            for (int i = 0; i < a.length(); i++) {
                String name = extractNameFromUnknown(a.opt(i));
                if (!name.isEmpty()) return name;
            }
            return "";
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            try {
                if (s.startsWith("{")) return extractNameFromUnknown(new JSONObject(s));
                return extractNameFromUnknown(new JSONArray(s));
            } catch (Exception ignored) {}
        }
        return s;
    }

    private String extractUniqueIdFromUnknown(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        if (value instanceof JSONObject) {
            JSONObject o = (JSONObject) value;
            String id = firstText(o, "uniqueId", "id", "habboId", "ownerId", "userId");
            if (!id.isEmpty()) return id;
            JSONObject owner = o.optJSONObject("owner");
            if (owner != null) return extractUniqueIdFromUnknown(owner);
            return "";
        }
        if (value instanceof JSONArray) {
            JSONArray a = (JSONArray) value;
            for (int i = 0; i < a.length(); i++) {
                String id = extractUniqueIdFromUnknown(a.opt(i));
                if (!id.isEmpty()) return id;
            }
            return "";
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            try {
                if (s.startsWith("{")) return extractUniqueIdFromUnknown(new JSONObject(s));
                return extractUniqueIdFromUnknown(new JSONArray(s));
            } catch(Exception ignored) {}
        }
        return "";
    }

    private String extractFigureFromUnknown(Object value) {
        if (value == null || value == JSONObject.NULL) return "";
        if (value instanceof JSONObject) {
            JSONObject o = (JSONObject) value;
            String figure = firstText(o, "figureString", "figure_string", "figure", "avatarFigureString", "ownerFigureString");
            if (!figure.isEmpty()) return figure;
            JSONObject owner = o.optJSONObject("owner");
            if (owner != null) return extractFigureFromUnknown(owner);
            return "";
        }
        if (value instanceof JSONArray) {
            JSONArray a = (JSONArray) value;
            for (int i = 0; i < a.length(); i++) {
                String figure = extractFigureFromUnknown(a.opt(i));
                if (!figure.isEmpty()) return figure;
            }
            return "";
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "";
        if ((s.startsWith("{") && s.endsWith("}")) || (s.startsWith("[") && s.endsWith("]"))) {
            try {
                if (s.startsWith("{")) return extractFigureFromUnknown(new JSONObject(s));
                return extractFigureFromUnknown(new JSONArray(s));
            } catch (Exception ignored) {}
        }
        return s.contains("-") ? s : "";
    }

    private String getRoomImageUrl(JSONObject room) {
        String url = normalizeUrl(firstText(room, "thumbnailUrl", "url"));
        return url == null ? "" : url.trim();
    }

    private void addPhotos(ProfileResult profileResult) {
        if (profileResult == null) return;
        ArrayList<JSONObject> list = profileResult.photos;
        if (list.isEmpty() && !profileResult.photosHasMore && !profileResult.photosLoading) return;
        final int loaded = list.size();
        final int totalLabel = Math.max(profileResult.photosTotal, loaded);
        LinearLayout c = sectionCardWithLoadMore(t("user_photos"), loaded, totalLabel > 0 ? totalLabel : loaded, profileResult.photosHasMore || profileResult.photosLoading, profileResult.photosLoading, () -> loadMorePhotos(profileResult, null));
        HorizontalScrollView hsv = new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); hsv.addView(row);
        c.addView(hsv, lp(-1, dp(165), 0, 0, 0, 0));
        final HorizontalScrollView photosHsv = hsv;
        if (photosScrollX > 0) photosHsv.post(() -> photosHsv.scrollTo(photosScrollX, 0));
        for (int i=0; i<loaded; i++) {
            JSONObject o = list.get(i);
            String url = getPhotoUrl(o);
            String date = getPhotoTimestamp(o);
            LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(16), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(160), dp(160)); bp.rightMargin = dp(12); row.addView(box, bp);
            ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.CENTER_CROP); applyRoundedClip(img, dp(14)); box.addView(img, new LinearLayout.LayoutParams(-1, dp(112)));
            TextView dt = text(date, 12, Color.argb(190,255,255,255), false); dt.setGravity(Gravity.CENTER); box.addView(dt, lp(-1,-2,0,8,0,0));
            if (!url.isEmpty()) { loadImage(img, url); final JSONObject photoObj = o; box.setOnClickListener(v -> showPhotoDialog(photoObj)); }
        }
        if (profileResult.photosHasMore && !profileResult.photosLoading) {
            View more = c.findViewWithTag("load_more_header_button");
            if (more != null) more.setOnClickListener(v -> loadMorePhotos(profileResult, photosHsv));
        }
    }

    private TextView loadMoreButton(String label, int shown, int total) {
        TextView more = new TextView(this);
        more.setGravity(Gravity.CENTER);
        more.setTextColor(Color.WHITE);
        more.setPadding(0, 0, 0, 0);
        more.setBackground(new AddButtonDrawable());
        return more;
    }

    private String getPhotoUrl(JSONObject photo) {
        String url = firstText(photo, "previewUrl", "url", "imageUrl", "photoUrl");
        if (url.isEmpty()) url = findImageUrlDeep(photo);
        return normalizeUrl(url);
    }

    private String getPhotoTimestamp(JSONObject photo) {
        String formatted = firstText(photo, "formatted_time", "formattedTime");
        if (!formatted.isEmpty()) return formatted;
        return niceDate(firstText(photo, "creationTime", "time"));
    }

    private int getPhotoLikesCount(JSONObject photo) {
        return getPhotoLikerNames(photo).size();
    }

    private void showPhotoDialog(JSONObject photo) {
        String url = getPhotoUrl(photo);
        if (url.isEmpty()) return;

        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(14), dp(14), dp(14), dp(14));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);

        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(-1, -2);
        }

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        applyRoundedClip(img, dp(16));
        wrap.addView(img, lp(-1, dp(260), 0,0,0,12));
        loadImage(img, url);

        String room = getPhotoRoomName(photo);
        String ownerName = getPhotoRoomOwnerName(photo);
        String ownerFigure = getPhotoRoomOwnerFigure(photo);
        String ownerId = getPhotoRoomOwnerId(photo);
        ArrayList<String> likers = getPhotoLikerNames(photo);

        LinearLayout infoGrid = new LinearLayout(this);
        infoGrid.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(infoGrid, lp(-1, -2, 0, 0, 0, 12));

        infoGrid.addView(photoInfoCard(t("date"), getPhotoTimestamp(photo), "", ""));
        if (!room.isEmpty()) infoGrid.addView(photoInfoCard(t("room"), room, "", ""));
        if (!ownerName.isEmpty()) {
            LinearLayout ownerCard = photoInfoCard(t("owner"), ownerName, ownerFigure, ownerName, ownerId);
            ownerCard.setOnClickListener(v -> {
                dialog.dismiss();
                openProfileReference(ownerName, ownerId, ownerFigure, currentHotelKey);
            });
            infoGrid.addView(ownerCard);
        }
        infoGrid.addView(photoInfoCard(t("likes"), String.valueOf(likers.size()), "", ""));

        if (!likers.isEmpty()) {
            TextView likesTitle = habboText(t("liked_by"), 17, true);
            likesTitle.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
            wrap.addView(likesTitle, lp(-1, -2, 0, 0, 0, 8));

            ScrollView likesScroll = new ScrollView(this);
            likesScroll.setVerticalScrollBarEnabled(true);
            likesScroll.setScrollbarFadingEnabled(false);
            tintScrollBar(likesScroll);
            likesScroll.setOnTouchListener((view, event) -> {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });

            LinearLayout likesList = new LinearLayout(this);
            likesList.setOrientation(LinearLayout.VERTICAL);
            likesScroll.addView(likesList, new ScrollView.LayoutParams(-1, -2));
            wrap.addView(likesScroll, lp(-1, dp(Math.min(230, Math.max(82, 54 * Math.min(likers.size(), 4)))), 0, 0, 0, 12));

            for (String liker : likers) {
                likesList.addView(likerRow(liker, dialog));
            }
        }

        Button close = new Button(this);
        close.setText(t("close"));
        close.setAllCaps(false);
        close.setTextColor(Color.WHITE);
        close.setBackground(grad(dp(14), purple2, purple));
        wrap.addView(close, lp(-1, dp(46), 0, 0, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            shownWindow.setAttributes(params);
        }
    }

    private LinearLayout photoInfoCard(String label, String value, String figure, String nickToOpen) {
        return photoInfoCard(label, value, figure, nickToOpen, "");
    }

    private LinearLayout photoInfoCard(String label, String value, String figure, String nickToOpen, String uniqueIdToOpen) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(24,255,255,255), dp(15), lightTheme ? Color.rgb(220,220,220) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 8));

        boolean hasHead = (figure != null && !figure.isEmpty()) || (nickToOpen != null && !nickToOpen.trim().isEmpty());
        if (hasHead) {
            ImageView head = new ImageView(this);
            head.setScaleType(ImageView.ScaleType.FIT_CENTER);
            row.addView(head, new LinearLayout.LayoutParams(dp(42), dp(42)));
            loadHeadImageForKnownProfile(head, figure, uniqueIdToOpen, nickToOpen, currentHotelKey);
        }

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        if (hasHead) tp.leftMargin = dp(10);
        row.addView(texts, tp);

        TextView lb = text(label, 12, Color.argb(185,255,255,255), false);
        texts.addView(lb);
        TextView val = habboText(value == null || value.isEmpty() ? "—" : value, 15, true);
        val.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        val.setMaxLines(2);
        val.setEllipsize(TextUtils.TruncateAt.END);
        texts.addView(val);

        if (nickToOpen != null && !nickToOpen.trim().isEmpty()) {
            final String nick = nickToOpen.trim();
            row.setOnClickListener(v -> {
                openProfileReference(nick, uniqueIdToOpen, figure, currentHotelKey);
            });
        }
        return row;
    }

    private LinearLayout likerRow(String nick, Dialog dialogToClose) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(14), lightTheme ? Color.rgb(220,220,220) : Color.argb(25,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 7));

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        row.addView(head, new LinearLayout.LayoutParams(dp(42), dp(42)));
        loadHeadImage(head, avatarHeadByName(nick));

        TextView name = habboText(nick, 15, true);
        name.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, -2, 1);
        np.leftMargin = dp(10);
        row.addView(name, np);

        row.setOnClickListener(v -> {
            if (dialogToClose != null) dialogToClose.dismiss();
            setSearchTextProgrammatically(nick);
            search();
        });

        return row;
    }

    private ArrayList<String> getPhotoLikerNames(JSONObject photo) {
        ArrayList<String> names = new ArrayList<>();
        if (photo == null) return names;

        Object raw = photo.opt("likerNames");
        addLikerNamesFromUnknown(names, raw);

        if (names.isEmpty()) addLikerNamesFromUnknown(names, photo.opt("likes"));
        if (names.isEmpty()) addLikerNamesFromUnknown(names, photo.opt("likers"));

        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String n : names) {
            String clean = n == null ? "" : n.trim();
            if (!clean.isEmpty() && !"null".equalsIgnoreCase(clean)) unique.add(clean);
        }
        return new ArrayList<>(unique);
    }

    private void addLikerNamesFromUnknown(ArrayList<String> out, Object raw) {
        if (out == null || raw == null || raw == JSONObject.NULL) return;

        if (raw instanceof JSONArray) {
            JSONArray a = (JSONArray) raw;
            for (int i = 0; i < a.length(); i++) addLikerNamesFromUnknown(out, a.opt(i));
            return;
        }

        if (raw instanceof JSONObject) {
            String name = extractNameFromUnknown(raw);
            if (!name.isEmpty()) out.add(name);
            return;
        }

        String s = String.valueOf(raw).trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return;

        if (s.startsWith("[") || s.startsWith("{")) {
            try {
                if (s.startsWith("[")) addLikerNamesFromUnknown(out, new JSONArray(s));
                else addLikerNamesFromUnknown(out, new JSONObject(s));
                return;
            } catch (Exception ignored) {}
        }

        out.add(s);
    }


    private String newBadgeLabel() {
        return "br".equals(normalizeHotelKey(currentHotelKey)) ? "NOVO" : "new";
    }

    private void addFriendsTabs(ArrayList<JSONObject> friendsList, ArrayList<JSONObject> removedList) {
        if (friendsList.isEmpty() && removedList.isEmpty()) return;
        LinearLayout c = sectionCard(null, 0, false);
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        c.addView(tabs, lp(-1, dp(58), 0, 0, 0, 14));

        TextView btFriends = tabButton(t("friends") + " (" + friendsList.size() + ")", true);
        TextView btRemoved = trashTabButton(false);

        tabs.addView(btFriends);
        Space tabSpace = new Space(this);
        tabs.addView(tabSpace, new LinearLayout.LayoutParams(0, 1, 1));
        tabs.addView(btRemoved);

        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); c.addView(content, lp(-1, -2, 0, 0, 0, 0));
        final boolean[] showingRemoved = {false}; final int[] page = {1};
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            content.removeAllViews();
            btFriends.setBackground(showingRemoved[0] ? tabBg(false) : tabBg(true));
            btFriends.setTextColor(showingRemoved[0] ? tabInactiveTextColor() : Color.WHITE);
            btRemoved.setBackground(new TrashTabDrawable(showingRemoved[0]));
            btRemoved.setText("");
            ArrayList<JSONObject> data = showingRemoved[0] ? removedList : friendsList;
            renderFriendsPage(content, data, page[0], 10, showingRemoved[0]);
            renderPager(content, data.size(), 10, page, render[0]);
        };
        btFriends.setOnClickListener(v -> { showingRemoved[0] = false; page[0] = 1; render[0].run(); });
        btRemoved.setOnClickListener(v -> { showingRemoved[0] = true; page[0] = 1; render[0].run(); });
        render[0].run();
        addBannerToResultWrap(buildFriendsRemovedBannerAd(), 18);
    }

    private int tabInactiveTextColor() { return lightTheme ? Color.rgb(70,70,70) : Color.argb(150,255,255,255); }

    private TextView tabButton(String s, boolean active) {
        TextView v = habboText(s, 16, true); v.setTextColor(active ? Color.WHITE : tabInactiveTextColor()); v.setGravity(Gravity.CENTER); v.setPadding(dp(13),0,dp(13),0); v.setBackground(tabBg(active));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-2, dp(44)); p.rightMargin = dp(8); v.setLayoutParams(p); return v;
    }

    private TextView trashTabButton(boolean active) {
        TextView v = text("", 16, Color.WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setPadding(0, 0, 0, 0);
        v.setBackground(new TrashTabDrawable(active));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(46), dp(44));
        p.leftMargin = dp(8);
        v.setLayoutParams(p);
        return v;
    }

    private Drawable tabBg(boolean active) { return active ? grad(dp(13), purple2, Color.rgb(166, 42, 235)) : round(lightTheme ? Color.rgb(244,244,246) : Color.argb(12,255,255,255), dp(13), lightTheme ? Color.rgb(210,210,214) : Color.argb(28,255,255,255), 1); }

    private void renderFriendsPage(LinearLayout content, ArrayList<JSONObject> data, int page, int per, boolean removed) {
        if (data.isEmpty()) { content.addView(centerNote(removed ? t("no_removed_friend_found") : t("no_friend_found"))); return; }
        int start = Math.max(0, (page-1)*per), end = Math.min(data.size(), start+per);
        for (int i=start; i<end; i+=2) {
            LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); content.addView(row, lp(-1, -2, 0, 0, 0, 12));
            row.addView(friendCard(data.get(i), removed), new LinearLayout.LayoutParams(0, dp(124), 1));
            if (i+1<end) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(124), 1); p.leftMargin = dp(12); row.addView(friendCard(data.get(i+1), removed), p); }
            else { Space sp = new Space(this); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, dp(124), 1); p.leftMargin=dp(12); row.addView(sp,p); }
        }
    }

    private LinearLayout friendCard(JSONObject f, boolean removed) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(8), dp(4), dp(8), dp(8));
        card.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(18), (removed || currentProfilePrivate) ? Color.argb(75, 255, 64, 64) : (lightTheme ? Color.rgb(220,220,220) : Color.argb(25,255,255,255)), 1));

        String n = firstText(f, "name", "username", "habboName"); if (n.isEmpty()) n = t("profile");
        String fig = firstText(f, "figureString", "figure", "look", "avatarFigureString");
        String fid = firstText(f, "uniqueId", "id", "habboId");
        String date = firstText(f, "creationTime", "friendSince", "createdAt", "date", "removedAt");

        FrameLayout headWrap = new FrameLayout(this);
        card.addView(headWrap, new LinearLayout.LayoutParams(-1, dp(64)));

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1, dp(62), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        hp.bottomMargin = dp(-2);
        headWrap.addView(head, hp);
        loadHeadImageForKnownProfile(head, fig, fid, n, currentHotelKey);

        if (isToday(date)) {
            TextView novo = text(newBadgeLabel(), 9, Color.WHITE, true);
            novo.setGravity(Gravity.CENTER);
            novo.setBackground(removed
                ? grad(dp(999), Color.rgb(190, 45, 58), Color.rgb(255, 92, 92))
                : grad(dp(999), Color.rgb(31,184,106), Color.rgb(54,210,127)));
            FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(dp(48), dp(18), Gravity.TOP|Gravity.CENTER_HORIZONTAL);
            headWrap.addView(novo,np);
        }
        if (optBoolAny(f, false, "online", "isOnline")) {
            IconView dot = new IconView(this, "dot");
            FrameLayout.LayoutParams dpv = new FrameLayout.LayoutParams(dp(22), dp(22), Gravity.RIGHT|Gravity.TOP);
            dpv.topMargin=dp(8); dpv.rightMargin=dp(8);
            headWrap.addView(dot, dpv);
        }

        TextView name = habboText(n, 14, true);
        name.setGravity(Gravity.CENTER);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);
        card.addView(name, lp(-1,-2,0,2,0,6));

        TextView d = text(niceDate(date), 12, Color.argb(185,255,255,255), false);
        d.setGravity(Gravity.CENTER);
        card.addView(d, lp(-1,-2,0,0,0,0));

        final String fname = n;
        final String friendId = fid;
        bindProfileCardOpenAndHold(card, fname, currentHotelKey, fig, friendId, () -> openProfileReference(fname, friendId, fig, currentHotelKey));
        return card;
    }

    private void renderPager(LinearLayout content, int total, int per, int[] page, Runnable rerender) {
        int totalPages = Math.max(1, (int)Math.ceil(total/(double)per));
        if (totalPages <= 1) return;
        TextView label = text(tr("page_of", page[0], totalPages), 16, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true); label.setGravity(Gravity.CENTER); content.addView(label, lp(-1,-2,0,6,0,12));
        LinearLayout p = new LinearLayout(this); p.setGravity(Gravity.CENTER); p.setOrientation(LinearLayout.HORIZONTAL); content.addView(p, lp(-1, dp(58), 0, 0, 0, 0));
        TextView prev = pageButton("‹", page[0] > 1); p.addView(prev);
        TextView one = pageButton(String.valueOf(page[0]), true); one.setBackground(grad(dp(14), purple2, purple)); p.addView(one);
        TextView next = pageButton("›", page[0] < totalPages); p.addView(next);
        prev.setOnClickListener(v -> { if (page[0] > 1) { page[0]--; rerender.run(); } });
        next.setOnClickListener(v -> { if (page[0] < totalPages) { page[0]++; rerender.run(); } });
    }

    private TextView pageButton(String s, boolean enabled) { TextView v = text(s, 20, enabled?Color.WHITE:Color.argb(70,255,255,255), true); v.setGravity(Gravity.CENTER); v.setBackground(round(Color.argb(14,255,255,255), dp(14), Color.argb(24,255,255,255), 1)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(44), dp(44)); p.setMargins(dp(6),0,dp(6),0); v.setLayoutParams(p); return v; }

    private void addRoomsTabs(ArrayList<JSONObject> rooms, ArrayList<JSONObject> oldRooms) {
        if (rooms.isEmpty() && oldRooms.isEmpty()) return;
        LinearLayout c = sectionCard(null, 0, false);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setGravity(Gravity.CENTER_VERTICAL);
        c.addView(tabs, lp(-1, dp(58), 0, 0, 0, 14));

        TextView btRooms = tabButton(t("rooms") + " (" + rooms.size() + ")", true);
        TextView btOld = trashTabButton(false);
        tabs.addView(btRooms);
        Space tabSpace = new Space(this);
        tabs.addView(tabSpace, new LinearLayout.LayoutParams(0, 1, 1));
        tabs.addView(btOld);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        c.addView(content, lp(-1,-2,0,0,0,0));

        final boolean[] old = {false};
        final int[] page = {1};
        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            content.removeAllViews();
            btRooms.setBackground(old[0] ? tabBg(false) : tabBg(true));
            btRooms.setTextColor(old[0] ? tabInactiveTextColor() : Color.WHITE);
            btOld.setBackground(new TrashTabDrawable(old[0]));
            btOld.setText("");
            ArrayList<JSONObject> data = old[0] ? oldRooms : rooms;
            renderRoomsPage(content, data, page[0], 5, old[0]);
            renderPager(content, data.size(), 5, page, render[0]);
        };
        btRooms.setOnClickListener(v -> { old[0] = false; page[0] = 1; render[0].run(); });
        btOld.setOnClickListener(v -> { old[0] = true; page[0] = 1; render[0].run(); });
        render[0].run();
    }

    private void renderRoomsPage(LinearLayout content, ArrayList<JSONObject> list, int page, int per, boolean oldRoom) {
        if (list.isEmpty()) { content.addView(centerNote(t("no_rooms_found"))); return; }
        int start=(page-1)*per, end=Math.min(list.size(), start+per);
        for (int i=start;i<end;i++) content.addView(roomRow(list.get(i), oldRoom));
    }

    private LinearLayout roomRow(JSONObject room, boolean oldRoom) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(10),dp(12),dp(10)); row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(16), (oldRoom || currentProfilePrivate) ? Color.argb(75, 255, 64, 64) : (lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255)), 1)); row.setLayoutParams(lp(-1, dp(116), 0, 0, 0, 12));
        ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.CENTER_CROP); img.setBackground(round(lightTheme ? Color.rgb(245,245,245) : Color.argb(25,255,255,255), dp(12), lightTheme ? Color.rgb(220,220,220) : Color.argb(20,255,255,255),1)); applyRoundedClip(img, dp(12)); row.addView(img, new LinearLayout.LayoutParams(dp(112), dp(78)));
        String image = getRoomImageUrl(room);
        if (!image.isEmpty()) Glide.with(this).load(image).error(R.drawable.quarto).into(img); else img.setImageResource(R.drawable.quarto);
        LinearLayout txt = new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,-2,1); tp.leftMargin=dp(12); row.addView(txt,tp);
        TextView roomName = habboText(firstText(room,"name","roomName","caption","title").isEmpty()?t("room"):firstText(room,"name","roomName","caption","title"), 16, true); roomName.setMaxLines(1); roomName.setEllipsize(TextUtils.TruncateAt.END); txt.addView(roomName);
        String score = firstText(room,"score","rating"); String date = niceDate(firstText(room,"createdAt","creationTime","date"));
        TextView meta = habboText("•  " + emptyDash(score) + "   " + date, 13, false);
        meta.setTextColor(lightTheme ? Color.rgb(97,97,97) : Color.argb(215,255,255,255));
        txt.addView(meta);
        String desc = firstText(room,"description","desc"); if(!desc.isEmpty()) { TextView rd = habboText(desc, 13, false); rd.setTextColor(Color.argb(210,255,255,255)); rd.setMaxLines(1); rd.setEllipsize(TextUtils.TruncateAt.END); txt.addView(rd); }
        return row;
    }

    private void addGroups(ArrayList<JSONObject> list) {
        if (list.isEmpty()) return;
        LinearLayout c = sectionCard(t("groups"), list.size(), true);

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        sv.setOnTouchListener((view, event) -> {
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        sv.addView(inner, new ScrollView.LayoutParams(-1, -2));
        c.addView(sv, lp(-1, dp(Math.min(430, Math.max(120, 98 * Math.min(list.size(), 4)))), 0, 0, 0, 0));

        for (int i=0; i<Math.min(list.size(), 60); i++) {
            inner.addView(groupRow(list.get(i)));
        }
    }

    private void addBadgesSection(ProfileResult r) {
        if (r == null) return;
        ArrayList<JSONObject> normal = r.badges == null ? new ArrayList<>() : r.badges;
        ArrayList<JSONObject> withAchievements = r.badgesWithAchievements == null ? new ArrayList<>() : r.badgesWithAchievements;

        int total = 0;
        try { total = Integer.parseInt(String.valueOf(r.totalBadges)); } catch(Exception ignored) {}
        total = Math.max(total, Math.max(normal.size(), withAchievements.size()));
        if (total <= 0 && normal.isEmpty() && withAchievements.isEmpty()) return;

        LinearLayout c = sectionCard(t("badges"), total, true);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        c.addView(controls, lp(-1, dp(46), 0, 0, 0, 10));

        final boolean[] hideAchievementBadges = {true};
        final int[] page = {1};

        TextView hideLabel = text(t("hide_badges"), 15, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        hideLabel.setGravity(Gravity.CENTER_VERTICAL);
        controls.addView(hideLabel, new LinearLayout.LayoutParams(0, dp(42), 1));

        TextView hideToggle = text("", 14, Color.WHITE, true);
        hideToggle.setGravity(Gravity.CENTER);
        hideToggle.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams toggleLp = new LinearLayout.LayoutParams(dp(58), dp(32));
        toggleLp.leftMargin = dp(10);
        controls.addView(hideToggle, toggleLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        c.addView(content, lp(-1, -2, 0, 0, 0, 0));

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            content.removeAllViews();

            hideToggle.setText("");
            hideToggle.setBackground(new AchievementSwitchDrawable(hideAchievementBadges[0]));
            
            ArrayList<JSONObject> data = hideAchievementBadges[0] ? normal : withAchievements;
            if (data == null) data = new ArrayList<>();
            renderBadgePage(content, data, page[0], 24);
            renderPager(content, data.size(), 24, page, render[0]);
        };

        View.OnClickListener toggleAction = v -> {
            hideAchievementBadges[0] = !hideAchievementBadges[0];
            page[0] = 1;
            render[0].run();
        };
        hideToggle.setOnClickListener(toggleAction);
        hideLabel.setOnClickListener(toggleAction);

        render[0].run();
    }


    private boolean isTodayCreationTime(String raw) {
        if (raw == null) return false;
        String s = raw.trim();
        if (s.isEmpty()) return false;
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(new Date());
        if (s.length() >= 10 && s.substring(0, 10).equals(today)) return true;

        String[] patterns = new String[]{
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat fmt = new SimpleDateFormat(pattern, Locale.ROOT);
                if (pattern.endsWith("'Z'")) fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = fmt.parse(s);
                if (d != null && new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(d).equals(today)) return true;
            } catch(Exception ignored) {}
        }
        return false;
    }

    private void renderBadgePage(LinearLayout content, ArrayList<JSONObject> list, int page, int per) {
        if (list == null || list.isEmpty()) {
            content.addView(centerNote(t("no_badges_found")));
            return;
        }

        int start = Math.max(0, (page - 1) * per);
        int end = Math.min(list.size(), start + per);
        int perRow = 4;
        LinearLayout row = null;

        for (int i = start; i < end; i++) {
            if ((i - start) % perRow == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                content.addView(row, lp(-1, dp(60), 0, 0, 0, 8));
            }

            JSONObject badgeObj = list.get(i);
            String code = firstText(badgeObj, "code", "badgeCode");

            FrameLayout cell = new FrameLayout(this);
            cell.setPadding(0, 0, 0, 0);
            cell.setBackgroundColor(Color.TRANSPARENT);
            cell.setClickable(true);

            ImageView img = new ImageView(this);
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setPadding(dp(2), dp(2), dp(2), dp(2));
            cell.addView(img, new FrameLayout.LayoutParams(dp(50), dp(50), Gravity.CENTER));
            if (!code.isEmpty()) loadImage(img, badgeImageUrl(code));

            if (isTodayCreationTime(firstText(badgeObj, "creationTime", "createdAt", "date"))) {
                TextView newBadge = text(newBadgeLabel(), 8, Color.WHITE, true);
                newBadge.setGravity(Gravity.CENTER);
                newBadge.setPadding(dp(5), 0, dp(5), 0);
                newBadge.setBackground(round(Color.rgb(39, 174, 96), dp(999), Color.argb(95,255,255,255), 1));
                FrameLayout.LayoutParams nlp = new FrameLayout.LayoutParams(-2, dp(16), Gravity.TOP | Gravity.RIGHT);
                nlp.topMargin = dp(2);
                nlp.rightMargin = dp(2);
                cell.addView(newBadge, nlp);
            }

            cell.setOnClickListener(v -> showBadgeDialog(badgeObj));

            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, dp(54), 1);
            cp.leftMargin = dp(2);
            cp.rightMargin = dp(2);
            if (row != null) row.addView(cell, cp);
        }
    }

    private LinearLayout groupRow(JSONObject g) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(12),dp(12),dp(12),dp(12)); row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(16), currentProfilePrivate ? Color.argb(75, 255, 64, 64) : (lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255)), 1)); row.setLayoutParams(lp(-1, -2, 0, 0, 0, 12));
        ImageView img = new ImageView(this); img.setScaleType(ImageView.ScaleType.FIT_CENTER); row.addView(img, new LinearLayout.LayoutParams(dp(58), dp(58)));
        String badge = firstText(g,"badgeCode","code"); String badgeUrl = normalizeUrl(firstText(g, "badgeUrl", "imageUrl", "url")); if(!badgeUrl.isEmpty()) loadImage(img, badgeUrl); else if(!badge.isEmpty()) loadImage(img,habboImagingUrl("/habbo-imaging/badge/"+enc(badge)+".gif")); else img.setImageDrawable(new PlaceholderDrawable("groups"));
        LinearLayout txt = new LinearLayout(this); txt.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1); tp.leftMargin=dp(12); row.addView(txt,tp);
        TextView groupName = habboText(firstText(g,"name","groupName").isEmpty()?"Grupo":firstText(g,"name","groupName"), 17, true); groupName.setMaxLines(1); groupName.setEllipsize(TextUtils.TruncateAt.END); txt.addView(groupName);
        String desc=firstText(g,"description","desc"); if(!desc.isEmpty()) { TextView gd = habboText(desc, 14, false); gd.setTextColor(Color.argb(220,255,255,255)); gd.setMaxLines(2); gd.setEllipsize(TextUtils.TruncateAt.END); txt.addView(gd); }
        txt.addView(text(niceDate(firstText(g,"createdAt","creationTime","date")), 13, Color.argb(190,255,255,255), false));
        return row;
    }

    private LinearLayout sectionCard(String title, int count, boolean showTitle) {
        LinearLayout c = card(dp(20));
        applyProfilePrivateBorder(c, dp(20));
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        resultWrap.addView(c, lp(-1, -2, 0, 0, 0, 18));
        if (showTitle && title != null) {
            TextView t = habboText(title + " (" + count + ")", 20, true); c.addView(t, lp(-1, -2, 0, 0, 0, 14));
        }
        return c;
    }

    private LinearLayout sectionCardWithLoadMore(String title, int shown, int total, boolean showButton, boolean loading, final Runnable action) {
        LinearLayout c = card(dp(20));
        applyProfilePrivateBorder(c, dp(20));
        c.setPadding(dp(16), dp(16), dp(16), dp(16));
        resultWrap.addView(c, lp(-1, -2, 0, 0, 0, 18));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView t = habboText(title + " (" + shown + "/" + Math.max(shown, total) + ")", 20, true);
        header.addView(t, new LinearLayout.LayoutParams(0, -2, 1));

        if (showButton) {
            FrameLayout more = new FrameLayout(this);
            more.setTag("load_more_header_button");
            more.setBackground(new AddButtonDrawable());
            more.setPadding(0, 0, 0, 0);
            LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(dp(28), dp(28));
            mp.leftMargin = dp(8);
            header.addView(more, mp);

            if (loading) {
                more.setBackground(grad(dp(7), purple2, purple));
                ProgressBar pb = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
                if (Build.VERSION.SDK_INT >= 21) pb.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
                FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(14), dp(14), Gravity.CENTER);
                more.addView(pb, pp);
            } else if (action != null) {
                more.setOnClickListener(v -> action.run());
            }
        }

        c.addView(header, lp(-1, dp(38), 0, 0, 0, 12));
        return c;
    }

    private TextView centerNote(String msg) { TextView v = text(msg, 14, muted, false); v.setGravity(Gravity.CENTER); v.setLineSpacing(dp(2),1f); v.setPadding(dp(8), dp(12), dp(8), dp(12)); return v; }



    private void setSuggestionsVisible(boolean visible) {
        if (suggestionsScroll != null) suggestionsScroll.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (suggestionsBox != null) suggestionsBox.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setSuggestionsHeight(int desiredDp) {
        if (suggestionsScroll == null) return;
        ViewGroup.LayoutParams raw = suggestionsScroll.getLayoutParams();
        if (raw != null) {
            raw.height = dp(Math.max(52, Math.min(230, desiredDp)));
            suggestionsScroll.setLayoutParams(raw);
        }
    }

    private void requestDisallowParents(View v, boolean disallow) {
        ViewParent p = v == null ? null : v.getParent();
        while (p != null) {
            p.requestDisallowInterceptTouchEvent(disallow);
            p = p.getParent();
        }
    }

    private void bindNickSuggestions() {
        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!programmaticSearchTextChange && searchInput != null && searchInput.hasFocus()) suppressSuggestions = false;
                scheduleSuggestions(String.valueOf(s));
            }
            public void afterTextChanged(Editable e) {}
        });
    }

    private void scheduleSuggestions(String raw) {
        final String q = raw == null ? "" : raw.trim();
        suggestionRequestId++;
        final int requestId = suggestionRequestId;
        suggestionsBox.removeAllViews();
        setSuggestionsVisible(false);

        if (suppressSuggestions || searchInProgress || searchInput == null || !searchInput.hasFocus()) return;
        if (q.length() < 2) return;

        showSuggestionsLoading();

        executor.execute(() -> {
            ArrayList<JSONObject> suggestions = fetchLiveNickSuggestions(q);
            runOnUiThread(() -> {
                if (requestId == suggestionRequestId && !suppressSuggestions && !searchInProgress && searchInput != null && searchInput.hasFocus()) renderLiveSuggestions(q, suggestions);
            });
        });
    }

    private void showSuggestionsLoading() {
        suggestionsBox.removeAllViews();
        setSuggestionsVisible(true);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));
        row.setBackground(round(lightTheme ? Color.rgb(248,248,250) : Color.argb(22,255,255,255), dp(14), lightTheme ? Color.rgb(222,222,226) : Color.argb(30,255,255,255), 1));

        ProgressBar pb = new ProgressBar(this);
        pb.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) pb.setIndeterminateTintList(ColorStateList.valueOf(purple));
        row.addView(pb, new LinearLayout.LayoutParams(dp(28), dp(28)));

        TextView tv = text(t("loading_suggestions"), 13, lightTheme ? Color.rgb(70,70,70) : Color.argb(220,255,255,255), true);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(10);
        row.addView(tv, tp);

        suggestionsBox.addView(row, lp(-1, -2, 0, 0, 0, 8));
        setSuggestionsHeight(58);
    }

    private void renderLiveSuggestions(String query, ArrayList<JSONObject> list) {
        suggestionsBox.removeAllViews();
        if (list == null || list.isEmpty()) { setSuggestionsVisible(false); return; }
        setSuggestionsVisible(true);
        TextView title = text(t("suggestions"), 12, Color.argb(210,255,255,255), true);
        suggestionsBox.addView(title, lp(-1, -2, 2, 2, 2, 6));
        int count = Math.min(list.size(), 8);
        for (int i=0; i<count; i++) suggestionsBox.addView(suggestionRow(query, list.get(i), true));
        setSuggestionsHeight(34 + (count * 76));
    }

    private ArrayList<JSONObject> fetchPreviousNickSuggestions(String query) {
        try {
            JSONObject payload = unwrap(getJson(habbodexSuggestUrl(query)));
            return filterExactPreviousNickSuggestions(payload, query);
        } catch(Exception e) { return new ArrayList<>(); }
    }

    private ArrayList<JSONObject> fetchLiveNickSuggestions(String query) {
        try {
            JSONObject payload = unwrap(getJson(habbodexSuggestUrl(query)));
            return filterLiveNickSuggestions(payload, query);
        } catch(Exception e) { return new ArrayList<>(); }
    }

    private ArrayList<JSONObject> filterLiveNickSuggestions(JSONObject suggest, String query) {
        ArrayList<JSONObject> out = new ArrayList<>();
        HashSet<String> seen = new HashSet<>();
        String q = normalizeNickKey(query);
        if (q.length() < 2 || suggest == null) return out;

        ArrayList<JSONObject> users = extractList(suggest, null);

        // 1) Primeiro, nicks atuais parecidos com o digitado.
        for (JSONObject user : users) {
            String current = firstText(user, "name", "username", "habboName");
            String currentKey = normalizeNickKey(current);
            if (currentKey.isEmpty()) continue;
            if (currentKey.startsWith(q)) {
                String id = stableSuggestionKey(user);
                if (seen.add(id)) out.add(user);
            }
            if (out.size() >= 8) return out;
        }

        // 2) Depois, apenas se o texto digitado for 100% igual a um nick antigo.
        for (JSONObject user : users) {
            String current = firstText(user, "name", "username", "habboName");
            String currentKey = normalizeNickKey(current);
            if (currentKey.isEmpty() || currentKey.equals(q)) continue;
            if (hasExactPreviousNick(user, q)) {
                String id = stableSuggestionKey(user);
                if (seen.add(id)) out.add(user);
            }
            if (out.size() >= 8) break;
        }

        return out;
    }

    private ArrayList<JSONObject> filterExactPreviousNickSuggestions(JSONObject suggest, String query) {
        ArrayList<JSONObject> out = new ArrayList<>();
        String q = normalizeNickKey(query);
        if (q.length() < 2 || suggest == null) return out;
        ArrayList<JSONObject> users = extractList(suggest, null);
        for (JSONObject user : users) {
            String current = firstText(user, "name", "username", "habboName");
            String currentKey = normalizeNickKey(current);
            if (currentKey.isEmpty() || currentKey.equals(q)) continue;
            if (hasExactPreviousNick(user, q)) out.add(user);
            if (out.size() >= 6) break;
        }
        return out;
    }

    private boolean hasExactPreviousNick(JSONObject user, String normalizedQuery) {
        String q = normalizedQuery == null ? "" : normalizedQuery.trim().toLowerCase(Locale.ROOT);
        if (q.length() < 2) return false;
        return getExactPreviousNameMatch(user, q) != null;
    }

    private String stableSuggestionKey(JSONObject user) {
        String id = firstText(user, "uniqueId", "id", "habboId");
        if (!id.isEmpty()) return id;
        return normalizeNickKey(firstText(user, "name", "username", "habboName"));
    }

    private JSONObject getExactPreviousNameMatch(JSONObject user, String query) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        for (JSONObject prev : extractList(user, "previousNames")) {
            String old = firstText(prev, "name", "oldName", "username");
            if (!old.isEmpty() && old.trim().toLowerCase(Locale.ROOT).equals(q)) return prev;
        }
        return null;
    }

    private LinearLayout suggestionRow(String query, JSONObject user, boolean compact) {
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(24,255,255,255), dp(14), lightTheme ? Color.rgb(220,220,220) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, compact ? dp(68) : dp(82), 0, 0, 0, 8));
        FrameLayout headWrap = new FrameLayout(this);
        row.addView(headWrap, new LinearLayout.LayoutParams(dp(compact?50:58), dp(compact?54:62)));
        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        headWrap.addView(head, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));

        String name = firstText(user, "name", "username", "habboName");
        String fig = firstText(user, "figureString", "figure", "look");
        String uniqueId = firstText(user, "uniqueId", "id", "habboId");
        loadHeadImageForKnownProfile(head, fig, uniqueId, name, currentHotelKey);

        if (optBoolAny(user, false, "online", "isOnline")) {
            IconView dot = new IconView(this, "dot");
            FrameLayout.LayoutParams dpv = new FrameLayout.LayoutParams(dp(16), dp(16), Gravity.RIGHT | Gravity.TOP);
            dpv.topMargin = dp(2);
            dpv.rightMargin = dp(2);
            headWrap.addView(dot, dpv);
        }
        JSONObject previous = getExactPreviousNameMatch(user, query);
        String oldName = previous == null ? "" : firstText(previous, "name", "oldName", "username");
        String changed = previous == null ? "" : niceDate(firstText(previous, "changedAt", "date", "timestamp", "createdAt"));
        LinearLayout texts = new LinearLayout(this); texts.setOrientation(LinearLayout.VERTICAL); LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1); tp.leftMargin = dp(10); row.addView(texts, tp);
        TextView nm = habboText(name, compact ? 15 : 17, true); nm.setMaxLines(1); nm.setEllipsize(TextUtils.TruncateAt.END); texts.addView(nm);
        if (previous != null && !oldName.isEmpty()) {
            TextView old = text(t("old_nick") + ": " + oldName, compact ? 12 : 13, Color.argb(210,255,255,255), false); old.setMaxLines(1); old.setEllipsize(TextUtils.TruncateAt.END); texts.addView(old);
            if (!changed.isEmpty() && !"—".equals(changed)) texts.addView(text(t("changed_at") + ": " + changed, compact ? 11 : 12, muted, false));
        }
        TextView arrow = text("›", compact ? 24 : 28, Color.WHITE, true); row.addView(arrow, new LinearLayout.LayoutParams(dp(26), -1));
        row.setOnClickListener(v -> openProfileReference(name, uniqueId, fig, currentHotelKey));
        return row;
    }

    private void showNotFoundState(String nick, ArrayList<JSONObject> suggestions) {
        resultWrap.removeAllViews();
        LinearLayout c = sectionCard(null, 0, false);
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        TextView title = habboText(t("no_profile_found"), 22, true); title.setGravity(Gravity.CENTER); c.addView(title, lp(-1,-2,0,0,0,8));
        TextView body = text(tr("not_found_body", nick), 14, muted, false); body.setGravity(Gravity.CENTER); body.setLineSpacing(dp(2),1f); c.addView(body, lp(-1,-2,0,0,0,14));
        if (suggestions != null && !suggestions.isEmpty()) {
            TextView st = habboText(t("old_nick_suggestions_title"), 17, true); c.addView(st, lp(-1,-2,0,0,0,10));
            for (JSONObject user : suggestions) c.addView(suggestionRow(nick, user, false));
        } else {
            c.addView(centerNote(t("no_old_nick_suggestions")));
        }
    }

    private void tintScrollBar(View v) {
        if (Build.VERSION.SDK_INT >= 29) {
            v.setVerticalScrollbarThumbDrawable(round(purple, dp(999), purple, 0));
            v.setVerticalScrollbarTrackDrawable(round(Color.argb(20,255,255,255), dp(999), Color.argb(20,255,255,255), 0));
        }
    }

    private void applyRoundedClip(View v, int radius) {
        if (Build.VERSION.SDK_INT >= 21) {
            v.setClipToOutline(true);
            v.setOutlineProvider(new ViewOutlineProvider() {
                @Override public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                }
            });
        }
    }

    private void showError(String msg) { resultWrap.removeAllViews(); LinearLayout c = sectionCard("Erro", 0, false); TextView t = text(msg, 15, Color.WHITE, true); t.setGravity(Gravity.CENTER); c.addView(t); }
    private void setLoading(boolean loading, String message) {
        if (loading) { suppressSuggestions = true; suggestionRequestId++; setSuggestionsVisible(false); }
        searchBtn.setEnabled(!loading);
        searchBtn.setText(loading ? t("searching_profile") : t("search_button"));
        progress.setVisibility(View.GONE);
        statusText.setText(loading ? "" : (message == null ? "" : message));
        if (loading) showLoadingSkeleton(message == null ? t("searching_profile") : message);
    }

    private void showInlineLoading(String message) {
        inlineProgressMessage = message == null ? "" : message;
        inlineProgressPct = loadingProgressFor(message);
        statusText.setText("");
    }

    private View inlineProgressBar(int pct) {
        FrameLayout bar = new FrameLayout(this);
        bar.setBackground(round(lightTheme ? Color.rgb(232,232,232) : Color.argb(34,255,255,255), dp(999), lightTheme ? Color.rgb(216,216,216) : Color.argb(28,255,255,255), 1));

        View fill = new View(this);
        fill.setBackground(grad(dp(999), purple2, purple));
        int available = Math.max(dp(80), getResources().getDisplayMetrics().widthPixels - dp(72));
        int width = Math.max(dp(22), (int)(available * (Math.max(0, Math.min(100, pct)) / 100f)));
        bar.addView(fill, new FrameLayout.LayoutParams(width, dp(9), Gravity.LEFT | Gravity.CENTER_VERTICAL));
        return bar;
    }

    
    private LinearLayout loadingProgressCard(String message, int pct) {
        LinearLayout card = card(dp(18));
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        card.addView(row, lp(-1, -2, 0, 0, 0, 10));

        ProgressBar spinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        if (Build.VERSION.SDK_INT >= 21) {
            spinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(purple));
        }
        row.addView(spinner, new LinearLayout.LayoutParams(dp(30), dp(30)));

        TextView tv = text(message == null ? t("generic_loading") : message, 13, Color.argb(230,255,255,255), true);
        tv.setSingleLine(true);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(10);
        row.addView(tv, tp);

        card.addView(inlineProgressBar(Math.max(8, pct)), lp(-1, dp(8), 0, 0, 0, 0));
        return card;
    }

private int loadingProgressFor(String message) {
        String m = message == null ? "" : message.toLowerCase(Locale.ROOT);
        if (m.contains("detalhes")) return 20;
        if (m.contains("histórico") || m.contains("historico")) return 42;
        if (m.contains("visuais") || m.contains("amigos")) return 66;
        if (m.contains("quartos") || m.contains("grupos")) return 86;
        return 10;
    }

    private String loadingProfileAvatarUrl(String figure) {
        String clean = figure == null || figure.trim().isEmpty() ? "hd-6295" : figure.trim();
        return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(clean) + "&size=l&direction=2&head_direction=3&gesture=std&action=wav&headonly=0&img_format=png");
    }

    private void updateLoadingProfileFigureHint(final String figure, final int token) {
        final String clean = figure == null ? "" : figure.trim();
        if (clean.isEmpty()) return;
        loadingProfileFigureHint = clean;
        runOnUiThread(() -> {
            if (!isActiveToken(token) || !searchInProgress) return;
            ImageView target = loadingProfileAvatarImage;
            if (target == null) return;
            String url = loadingProfileAvatarUrl(clean);
            try {
                Glide.with(MainActivity.this).load(url).into(target);
            } catch (Exception ex) {
                loadImage(target, url);
            }
        });
    }

    private void showLoadingSkeleton(String message) {
        resultWrap.removeAllViews();

        LinearLayout c = card(dp(22));
        c.setPadding(dp(18), dp(18), dp(18), dp(18));
        resultWrap.addView(c, lp(-1, -2, 0, 0, 0, 18));

        TextView title = habboText(message, 18, true);
        title.setGravity(Gravity.CENTER);
        c.addView(title, lp(-1,-2,0,0,0,8));
        ProgressBar skeletonSpinner = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);
        if (Build.VERSION.SDK_INT >= 21) skeletonSpinner.setIndeterminateTintList(android.content.res.ColorStateList.valueOf(purple));
        LinearLayout spinnerLine = new LinearLayout(this);
        spinnerLine.setGravity(Gravity.CENTER);
        spinnerLine.addView(skeletonSpinner, new LinearLayout.LayoutParams(dp(30), dp(30)));
        c.addView(spinnerLine, lp(-1, dp(34), 0,0,0,12));

        FrameLayout avatar = new FrameLayout(this);
        avatar.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.rgb(15, 8, 25), dp(20), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
        c.addView(avatar, lp(-1, dp(280), 0,0,0,16));

        ImageView walker = new ImageView(this);
        loadingProfileAvatarImage = walker;
        walker.setScaleType(ImageView.ScaleType.FIT_CENTER);
        walker.setPadding(dp(20), dp(10), dp(20), dp(84));
        avatar.addView(walker, new FrameLayout.LayoutParams(-1, -1));
        String nick = searchInput == null ? "" : searchInput.getText().toString().trim();
        String cachedFigure = loadingProfileFigureHint == null ? "" : loadingProfileFigureHint.trim();
        String lookupKey = loadingProfileUniqueIdHint == null || loadingProfileUniqueIdHint.trim().isEmpty() ? nick : loadingProfileUniqueIdHint.trim();
        ProfileResult cachedProfile = getCachedProfile(lookupKey);
        if ((cachedFigure == null || cachedFigure.trim().isEmpty()) && cachedProfile != null) cachedFigure = cachedProfile.figure;
        if (cachedFigure == null || cachedFigure.trim().isEmpty()) {
            // Figure neutra usada somente durante o loader quando ainda não sabemos a figure real.
            cachedFigure = "hd-6295";
        }
        // Nunca usa ?user=nick no loader: nicks repetidos podem carregar o avatar de outra conta.
        // Durante o loader, usa a pose solicitada para deixar o avatar pesquisado mais vivo.
        String walkerUrl = loadingProfileAvatarUrl(cachedFigure);
        String fallbackUrl = walkerUrl;
        try {
            Glide.with(this).load(walkerUrl).error(Glide.with(this).load(fallbackUrl)).into(walker);
        } catch (Exception ex) {
            loadImage(walker, fallbackUrl);
        }

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        c.addView(grid, lp(-1, -2, 0, 0, 0, 0));
        grid.addView(skeletonLine(dp(180), dp(28), true));
        grid.addView(skeletonLine(-1, dp(16), false));
        grid.addView(skeletonLine(-1, dp(16), false));

        for (int rowIndex = 0; rowIndex < 3; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            grid.addView(row, lp(-1, dp(58), 0, 6, 0, 8));
            for (int col = 0; col < 2; col++) {
                LinearLayout mini = new LinearLayout(this);
                mini.setOrientation(LinearLayout.HORIZONTAL);
                mini.setGravity(Gravity.CENTER_VERTICAL);
                mini.setPadding(dp(10), dp(7), dp(10), dp(7));
                mini.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(16), lightTheme ? Color.rgb(220,220,220) : Color.argb(24,255,255,255), 1));
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -1, 1);
                if (col == 1) mp.leftMargin = dp(8);
                row.addView(mini, mp);
                mini.addView(skeletonBlock(dp(24), dp(24), dp(999)));
                LinearLayout lines = new LinearLayout(this);
                lines.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams lpLines = new LinearLayout.LayoutParams(0, -2, 1);
                lpLines.leftMargin = dp(9);
                mini.addView(lines, lpLines);
                lines.addView(skeletonLine(dp(70), dp(10), false));
                lines.addView(skeletonLine(dp(110), dp(14), false));
            }
        }
    }

    private View skeletonLine(int width, int height, boolean centered) {
        View v = skeletonBlock(width < 0 ? -1 : width, height, dp(999));
        LinearLayout.LayoutParams p = lp(width < 0 ? -1 : width, height, centered ? 40 : 0, 0, centered ? 40 : 0, 10);
        v.setLayoutParams(p);
        return v;
    }

    private View skeletonBlock(int width, int height, int radius) {
        View v = new View(this);
        v.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(28,255,255,255), radius, lightTheme ? Color.rgb(220,220,220) : Color.argb(18,255,255,255), 1));
        v.setAlpha(0.72f);
        v.animate().alpha(1f).setDuration(650).withEndAction(() -> v.animate().alpha(0.55f).setDuration(650).withEndAction(() -> pulseSkeleton(v)).start()).start();
        v.setLayoutParams(new LinearLayout.LayoutParams(width, height));
        return v;
    }

    private void pulseSkeleton(View v) {
        if (v == null || v.getWindowToken() == null) return;
        v.animate().alpha(1f).setDuration(650).withEndAction(() -> {
            if (v.getWindowToken() != null) v.animate().alpha(0.55f).setDuration(650).withEndAction(() -> pulseSkeleton(v)).start();
        }).start();
    }

    private void startFloating(View v) {
        if (v == null) return;
        v.setTranslationY(dp(5));
        v.animate().translationY(-dp(7)).setDuration(900).withEndAction(() -> {
            if (v.getWindowToken() != null) v.animate().translationY(dp(5)).setDuration(900).withEndAction(() -> startFloating(v)).start();
        }).start();
    }


    private String habbodexProfileByNameUrl(String name) {
        return HABBODEX + "/habboinfo/" + enc(habbodexHotelCode(currentHotelKey)) + "/habbo?name=" + enc(name);
    }

    private String habbodexProfileByUniqueUrl(String uniqueId) {
        return habbodexProfileByUniqueUrlForHotel(uniqueId, currentHotelKey);
    }

    private String habbodexProfileByUniqueUrlForHotel(String uniqueId, String hotelKey) {
        return HABBODEX + "/habboinfo/" + enc(uniqueId) + "?hotel=" + enc(habbodexHotelCode(hotelKey));
    }

    private String habbodexEndpointUrl(String uniqueId, String endpoint, int page, int limit) {
        return HABBODEX + "/habboinfo/" + enc(uniqueId) + "/" + enc(endpoint) + "?page=" + page + "&limit=" + limit + "&hotel=" + enc(habbodexHotelCode(currentHotelKey));
    }

    private String habbodexFigureUrl(String figure) {
        return HABBODEX + "/furnidex/furni/from-figure-string?figureString=" + enc(figure) + "&hotel=" + enc(habbodexHotelCode(currentHotelKey));
    }

    private String habbodexSuggestUrl(String name) {
        return HABBODEX + "/habboinfo/habbos?name=" + enc(name) + "&includePreviousNames=true&hotel=" + enc(habbodexHotelCode(currentHotelKey));
    }

    private Object getJsonAny(String u) throws Exception { HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection(); c.setConnectTimeout(12000); c.setReadTimeout(24000); c.setRequestProperty("Accept", "application/json, text/plain, */*"); c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android"); int code = c.getResponseCode(); InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream(); String body = readAll(is); if (code < 200 || code >= 300 || body == null || body.trim().isEmpty()) throw new IOException("HTTP " + code); String clean = body.trim(); return clean.startsWith("[") ? new JSONArray(clean) : new JSONObject(clean); }
    private JSONObject getJson(String u) throws Exception { Object any = getJsonAny(u); if (any instanceof JSONObject) return (JSONObject)any; JSONObject wrap = new JSONObject(); wrap.put("data", any); return wrap; }
    private JSONObject tryJson(String u) { try { return getJson(u); } catch (Exception e) { return null; } }
    private String readAll(InputStream is) throws IOException {
        if (is == null) return "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[32768];
        try {
            int n;
            while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        } finally {
            try { is.close(); } catch(Exception ignored) {}
        }
    }
    private void loadImage(ImageView view, String url) { 
        if (view == null || url == null || url.trim().isEmpty()) return; 
        String clean = normalizeUrl(url); 
        runOnUiThread(() -> Glide.with(MainActivity.this).load(clean).into(view)); 
    }

    private void loadHeadImage(ImageView view, String url) {
        if (view == null) return;
        view.setImageResource(R.drawable.pre_load_head);
        if (url == null || url.trim().isEmpty()) return;
        String clean = normalizeUrl(url);
        runOnUiThread(() -> Glide.with(MainActivity.this)
            .load(clean)
            .placeholder(R.drawable.pre_load_head)
            .error(R.drawable.pre_load_head)
            .into(view));
    }

    private void loadHeadImageForKnownProfile(ImageView view, String figure, String uniqueId, String fallbackNick, String hotelKey) {
        if (view == null) return;
        view.setImageResource(R.drawable.pre_load_head);
        String fig = figure == null ? "" : figure.trim();
        if (!fig.isEmpty()) {
            loadHeadImage(view, avatarHead(fig));
            return;
        }
        String id = uniqueId == null ? "" : uniqueId.trim();
        String hotel = normalizeHotelKey(hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        if (!id.isEmpty()) {
            final String finalHotel = hotel;
            executor.execute(() -> {
                try {
                    JSONObject dex = unwrap(tryJson(habbodexProfileByUniqueUrlForHotel(id, finalHotel)));
                    String fetchedFigure = firstText(dex, "figureString", "figure", "figure_string");
                    if (!fetchedFigure.isEmpty()) {
                        runOnUiThread(() -> loadHeadImage(view, avatarHead(fetchedFigure)));
                    }
                } catch(Exception ignored) {}
            });
            return;
        }
        // Só usa nick como último fallback, quando o dado não possui ID.
        if (fallbackNick != null && !fallbackNick.trim().isEmpty()) {
            loadHeadImage(view, avatarHeadByNameForHotel(fallbackNick.trim(), hotel));
        }
    }

    private void loadAvatarImage(ImageView view, String url) {
        if (view == null) return;
        view.setImageResource(R.drawable.pre_load);
        if (url == null || url.trim().isEmpty()) return;
        String clean = normalizeUrl(url);
        runOnUiThread(() -> Glide.with(MainActivity.this)
            .load(clean)
            .placeholder(R.drawable.pre_load)
            .error(R.drawable.pre_load)
            .into(view));
    }


    private JSONObject validProfileObject(JSONObject obj) {
        if (obj == null) return null;
        if (obj.has("ok") && !obj.optBoolean("ok", true) && !obj.has("data")) return null;
        if (!firstText(obj, "uniqueId", "id", "habboId", "name", "username", "habboName", "figureString", "figure").isEmpty()) return obj;
        JSONObject d = obj.optJSONObject("data");
        if (d != null && !firstText(d, "uniqueId", "id", "habboId", "name", "username", "habboName", "figureString", "figure").isEmpty()) return d;
        return null;
    }

    private JSONObject unwrap(JSONObject obj) { if (obj == null) return null; if (obj.has("ok") && obj.has("data")) { Object data = obj.opt("data"); return data instanceof JSONObject ? (JSONObject)data : obj; } return obj; }
    private JSONObject firstObject(JSONObject... objects) { for (JSONObject o : objects) if (o != null && o.length() > 0) return o; return null; }
    private JSONObject firstFromList(JSONObject obj) { ArrayList<JSONObject> list = extractList(obj, null); return list.isEmpty() ? null : list.get(0); }
    private ArrayList<JSONObject> extractPreviousNamesFromSuggest(JSONObject suggest, String currentName) { ArrayList<JSONObject> out = new ArrayList<>(); ArrayList<JSONObject> users = extractList(suggest, null); String low = currentName == null ? "" : currentName.toLowerCase(Locale.ROOT); for (JSONObject user : users) { String uname = firstText(user, "name", "username").toLowerCase(Locale.ROOT); if (!low.isEmpty() && !uname.equals(low)) continue; out.addAll(extractList(user, "previousNames")); } return out; }
    private ArrayList<JSONObject> extractListFromKeys(JSONObject obj, String... keys) { ArrayList<JSONObject> out = new ArrayList<>(); if (obj == null) return out; for (String k : keys) out = mergeLists(out, extractList(obj, k)); return out; }
    private ArrayList<JSONObject> extractList(JSONObject data, String primaryKey) { ArrayList<JSONObject> out = new ArrayList<>(); if (data == null) return out; JSONArray arr = null; if (primaryKey != null && !primaryKey.isEmpty()) arr = data.optJSONArray(primaryKey); if (arr == null) arr = data.optJSONArray("result"); if (arr == null) arr = data.optJSONArray("results"); if (arr == null) arr = data.optJSONArray("data"); if (arr == null) arr = data.optJSONArray("items"); JSONObject d = data.optJSONObject("data"); if (arr == null && d != null) { if (primaryKey != null && !primaryKey.isEmpty()) arr = d.optJSONArray(primaryKey); if (arr == null) arr = d.optJSONArray("result"); if (arr == null) arr = d.optJSONArray("results"); if (arr == null) arr = d.optJSONArray("items"); } if (arr != null) for (int i=0; i<arr.length(); i++) { JSONObject o = arr.optJSONObject(i); if (o != null) out.add(o); } return out; }
    private ArrayList<JSONObject> mergeLists(ArrayList<JSONObject> a, ArrayList<JSONObject> b) { ArrayList<JSONObject> out = new ArrayList<>(); HashSet<String> seen = new HashSet<>(); if (a != null) addUnique(out, seen, a); if (b != null) addUnique(out, seen, b); return out; }
    private void addUnique(ArrayList<JSONObject> out, HashSet<String> seen, ArrayList<JSONObject> src) { for (JSONObject o : src) { String key = stableItemKey(o); if (seen.add(key)) out.add(o); } }
    private String stableItemKey(JSONObject o) {
        if (o == null) return String.valueOf(System.identityHashCode(o));
        String key = firstText(o, "uniqueId", "id", "badgeCode", "code");
        if (!key.isEmpty()) return key;
        String figure = firstText(o, "figureString", "figure");
        String when = firstText(o, "changedAt", "date", "createdAt", "creationTime", "time");
        if (!figure.isEmpty() || !when.isEmpty()) return "fig:" + figure + "|" + when;
        String name = firstText(o, "name", "username", "habboName", "motto");
        if (!name.isEmpty() || !when.isEmpty()) return "txt:" + name + "|" + when;
        return String.valueOf(o.toString().hashCode());
    }
    private String firstText(JSONObject o, String... keys) { if (o == null) return ""; for (String k : keys) { Object v = o.opt(k); if (v == null || v == JSONObject.NULL) continue; String s = String.valueOf(v).trim(); if (!s.isEmpty() && !"null".equalsIgnoreCase(s)) return s; } return ""; }
    private boolean optBoolTrue(JSONObject o, String... keys) { if (o == null) return false; for (String k : keys) { if (!o.has(k)) continue; Object v = o.opt(k); if (v instanceof Boolean) return ((Boolean)v); String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT); if (s.equals("true") || s.equals("1") || s.equals("yes")) return true; } return false; }

    private boolean optBoolAny(JSONObject o, boolean fallback, String... keys) { if (o == null) return fallback; for (String k : keys) if (o.has(k)) return o.optBoolean(k, fallback); return fallback; }

    private Boolean optBoolNullable(JSONObject o, String... keys) {
        if (o == null) return null;
        for (String k : keys) {
            if (!o.has(k)) continue;
            Object v = o.opt(k);
            if (v == null || v == JSONObject.NULL) continue;
            if (v instanceof Boolean) return (Boolean)v;
            String s = String.valueOf(v).trim().toLowerCase(Locale.ROOT);
            if (s.equals("true") || s.equals("1") || s.equals("yes")) return true;
            if (s.equals("false") || s.equals("0") || s.equals("no")) return false;
        }
        return null;
    }

    private JSONObject nestedObject(JSONObject o, String key) {
        if (o == null || key == null || key.isEmpty()) return null;
        Object v = o.opt(key);
        return v instanceof JSONObject ? (JSONObject)v : null;
    }

    private Boolean optBoolNullableDeep(JSONObject o, String... keys) {
        if (o == null) return null;
        Boolean direct = optBoolNullable(o, keys);
        if (direct != null) return direct;
        String[] nested = new String[]{"data", "user", "profile", "habbo"};
        for (String n : nested) {
            JSONObject child = nestedObject(o, n);
            if (child == null || child == o) continue;
            Boolean value = optBoolNullable(child, keys);
            if (value != null) return value;
        }
        return null;
    }

    private boolean isPrivateProfileFromSources(JSONObject... sources) {
        if (sources == null) return false;
        for (JSONObject source : sources) {
            if (source == null) continue;
            Boolean visible = optBoolNullableDeep(source, "profileVisible", "isProfileVisible", "visible");
            if (visible != null && !visible) return true;
            Boolean priv = optBoolNullableDeep(source, "privateProfile", "profilePrivate", "isPrivate", "isProfilePrivate", "private");
            if (priv != null && priv) return true;
        }
        return false;
    }

    private boolean isBannedProfileFromSources(JSONObject... sources) {
        if (sources == null) return false;
        for (JSONObject source : sources) {
            if (source == null) continue;
            Boolean banned = optBoolNullableDeep(source, "isBanned", "banned", "ban", "is_banned");
            if (banned != null && banned) return true;
        }
        return false;
    }

    private void normalizeProfileState(ProfileResult r) {
        if (r == null) return;
        JSONObject officialUser = r.officialProfile == null ? null : r.officialProfile.optJSONObject("user");
        if (isPrivateProfileFromSources(r.habboPublic, r.dex, r.dexProfile, r.officialProfile, officialUser)) r.privateProfile = true;
        if (isBannedProfileFromSources(r.habboPublic, r.dex, r.dexProfile, r.officialProfile, officialUser)) r.banned = true;
    }

    private String avatarFull(String figure) { return avatarFull(figure, 2); }
    private String avatarFull(String figure, int direction) { return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=l&direction=" + direction + "&head_direction=" + direction + "&gesture=std&action=std&headonly=0"); }
    private String avatarSmall(String figure) { return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=m&direction=2&head_direction=2&gesture=std&action=std&headonly=0"); }
    private String avatarHead(String figure) { return habboImagingUrl("/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=m&direction=2&head_direction=2&headonly=1"); }
    private String avatarHeadByName(String name) { return avatarHeadByNameForHotel(name, currentHotelKey); }
    private String avatarHeadByNameForHotel(String name, String hotelKey) { return "https://" + hotelDomain(hotelKey) + "/habbo-imaging/avatarimage?user=" + enc(name) + "&size=m&direction=2&head_direction=2&headonly=1"; }

    private Drawable makeBg() {
        if (lightTheme) return new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.rgb(250, 250, 250), Color.rgb(242, 242, 242), Color.rgb(247, 247, 247)});
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{Color.rgb(30, 11, 45), Color.rgb(24,14,35), Color.rgb(12,12,18)});
    }
    private LinearLayout card(int radius) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        int stroke = currentProfilePrivate ? Color.argb(112, 211, 47, 47) : (lightTheme ? Color.rgb(216, 216, 216) : cardStroke);
        int fill = lightTheme ? Color.rgb(255,255,255) : cardFill;
        l.setBackground(round(fill, radius, stroke, 1));
        return l;
    }

    private LinearLayout neutralCard(int radius) {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.VERTICAL);
        int stroke = lightTheme ? Color.rgb(216, 216, 216) : cardStroke;
        int fill = lightTheme ? Color.rgb(255,255,255) : cardFill;
        l.setBackground(round(fill, radius, stroke, 1));
        return l;
    }

    private void applyProfilePrivateBorder(LinearLayout view, int radius) {
        if (currentProfilePrivate && view != null) {
            view.setBackground(round(lightTheme ? Color.WHITE : cardFill, radius, Color.argb(112, 211, 47, 47), 1));
        }
    }
    private int themeTextColor(int color) {
        if (!lightTheme) return color;
        if (Color.alpha(color) < 255) {
            return Color.rgb(95, 95, 95);
        }
        if (color == Color.WHITE || (Color.red(color) > 180 && Color.green(color) > 180 && Color.blue(color) > 180)) {
            return Color.rgb(33, 33, 33);
        }
        return color;
    }
    private int themeMutedColor() { return lightTheme ? Color.rgb(97, 97, 97) : muted; }
    private TextView text(String s, int sp, int color, boolean bold) { TextView v = new TextView(this); v.setText(s == null ? "" : s); v.setTextSize(sp); v.setTextColor(themeTextColor(color)); if (bold) v.setTypeface(Typeface.DEFAULT_BOLD); return v; }
    private TextView habboText(String s, int sp, boolean bold) { TextView v = text(s, sp, lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE, bold); v.setTypeface(habboFont); return v; }
    private TextView toxicLogoText(String s, int sp) {
        TextView v = habboText(s, sp, true);
        v.setTextColor(lightTheme ? Color.rgb(151, 38, 220) : Color.rgb(238, 104, 255));
        v.setShadowLayer(lightTheme ? dp(1) : dp(4), 0, lightTheme ? dp(1) : dp(2), lightTheme ? Color.argb(80,120,40,170) : Color.rgb(103, 26, 180));
        v.setIncludeFontPadding(false);
        v.setLetterSpacing(0.02f);
        return v;
    }
    private TextView pill(String s, int color) { TextView v = text(s, 13, Color.WHITE, true); v.setGravity(Gravity.CENTER); v.setPadding(dp(14), dp(9), dp(14), dp(9)); v.setBackground(round(adjustAlpha(color, 0.32f), dp(999), adjustAlpha(color,0.55f), 1)); return v; }
    private GradientDrawable round(int fill, int radius, int stroke, int sw) { GradientDrawable d = new GradientDrawable(); d.setColor(fill); d.setCornerRadius(radius); if (sw > 0) d.setStroke(dp(sw), stroke); return d; }
    private GradientDrawable grad(int radius, int c1, int c2) { GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{c1,c2}); d.setCornerRadius(radius); return d; }
    private LinearLayout.LayoutParams lp(int w, int h, int l, int t, int r, int b) { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w,h); p.setMargins(dp(l),dp(t),dp(r),dp(b)); return p; }
    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private int adjustAlpha(int color, float f) { return Color.argb(Math.round(Color.alpha(color)*f), Color.red(color), Color.green(color), Color.blue(color)); }
    private String enc(String s) { try { return URLEncoder.encode(s == null ? "" : s, "UTF-8"); } catch(Exception e){ return s; } }
    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }
    private void hideKeyboard(){ try{ ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(searchInput.getWindowToken(),0);}catch(Exception ignored){} }
    private void clearSearchFocus(){
        try {
            setSuggestionsVisible(false);
            if (searchInput != null) {
                searchInput.clearFocus();
                searchInput.setCursorVisible(false);
                hideKeyboard();
            }
        } catch(Exception ignored) {}
    }
    private boolean isTouchInsideView(View view, MotionEvent event) {
        if (view == null || event == null) return false;
        int[] loc = new int[2];
        view.getLocationOnScreen(loc);
        float x = event.getRawX();
        float y = event.getRawY();
        return x >= loc[0] && x <= loc[0] + view.getWidth() && y >= loc[1] && y <= loc[1] + view.getHeight();
    }
    private void openUrl(String url){ try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch(Exception ignored){} }
    private String normalizeUrl(String url) { String s = url == null ? "" : url.trim(); if (s.startsWith("//")) return "https:" + s; if (s.startsWith("/")) return "https://atoxic.com.br" + s; return s; }

    private String emptyDash(String s) { return s == null || s.trim().isEmpty() ? "—" : s.trim(); }

    private Date parseHabboDate(String in) {
        if (in == null || in.trim().isEmpty()) return null;
        String s = in.trim();
        try {
            if (s.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                SimpleDateFormat only = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                only.setTimeZone(TimeZone.getTimeZone("UTC"));
                return only.parse(s);
            }
            if (s.matches("^\\d{10,13}$")) {
                long ts = Long.parseLong(s);
                if (s.length() == 10) ts *= 1000;
                return new Date(ts);
            }
            String iso = s.replace("Z", "+0000").replaceAll("([+-]\\d{2}):(\\d{2})$", "$1$2");
            String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat f = new SimpleDateFormat(pattern, Locale.US);
                    f.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return f.parse(iso);
                } catch(Exception ignored) {}
            }
        } catch(Exception ignored) {}
        return null;
    }

    private String niceDate(String in) {
        if (in == null || in.trim().isEmpty() || "null".equalsIgnoreCase(in.trim())) return "—";
        Date d = parseHabboDate(in);
        if (d == null) return in;
        String pattern = "com".equals(normalizeHotelKey(currentHotelKey)) ? "MM/dd/yyyy HH:mm" : "dd/MM/yyyy HH:mm";
        return new SimpleDateFormat(pattern, Locale.ROOT).format(d);
    }

    private String niceDateOnly(String in) {
        if (in == null || in.trim().isEmpty() || "null".equalsIgnoreCase(in.trim())) return "—";
        Date d = parseHabboDate(in);
        if (d == null) return in;
        String pattern = "com".equals(normalizeHotelKey(currentHotelKey)) ? "MM/dd/yyyy" : "dd/MM/yyyy";
        return new SimpleDateFormat(pattern, Locale.ROOT).format(d);
    }

    private String timeAgoText(String in) {
        Date d = parseHabboDate(in);
        if (d == null) return "";
        long diff = Math.max(0L, System.currentTimeMillis() - d.getTime()) / 1000L;
        long value;
        String unitKey;
        if (diff < 60) { value = Math.max(1, diff); unitKey = value == 1 ? "ago_second" : "ago_seconds"; }
        else if (diff < 3600) { value = diff / 60; unitKey = value == 1 ? "ago_minute" : "ago_minutes"; }
        else if (diff < 86400) { value = diff / 3600; unitKey = value == 1 ? "ago_hour" : "ago_hours"; }
        else if (diff < 604800) { value = diff / 86400; unitKey = value == 1 ? "ago_day" : "ago_days"; }
        else if (diff < 2629800) { value = diff / 604800; unitKey = value == 1 ? "ago_week" : "ago_weeks"; }
        else if (diff < 31557600) { value = diff / 2629800; unitKey = value == 1 ? "ago_month" : "ago_months"; }
        else { value = diff / 31557600; unitKey = value == 1 ? "ago_year" : "ago_years"; }
        return tr("time_ago", value, t(unitKey));
    }

    private boolean isToday(String in) { if (in == null || in.trim().isEmpty()) return false; String d = niceDate(in); String today = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt","BR")).format(new Date()); return d.startsWith(today); }


    private String findImageUrlDeep(Object obj) {
        HashSet<Object> seen = new HashSet<>();
        return findImageUrlDeep(obj, seen);
    }

    private String findImageUrlDeep(Object obj, HashSet<Object> seen) {
        if (obj == null || obj == JSONObject.NULL || seen.contains(obj)) return "";
        seen.add(obj);
        if (obj instanceof String) {
            String s = ((String)obj).trim();
            if (s.startsWith("http") && (s.matches("(?i).*\\.(png|jpg|jpeg|gif|webp)(\\?.*)?$") || s.contains("habbo") || s.contains("habbodex"))) return s;
            return "";
        }
        if (obj instanceof JSONObject) {
            JSONObject jo = (JSONObject)obj;
            String[] priority = {"url","previewUrl","imageUrl","photoUrl","largeUrl","smallUrl","thumbnailUrl","thumbnail","image","photo","roomImage","badgeUrl"};
            for (String k : priority) {
                Object v = jo.opt(k);
                String found = findImageUrlDeep(v, seen);
                if (!found.isEmpty()) return found;
            }
            Iterator<String> it = jo.keys();
            while (it.hasNext()) {
                String found = findImageUrlDeep(jo.opt(it.next()), seen);
                if (!found.isEmpty()) return found;
            }
        }
        if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray)obj;
            for (int i=0;i<a.length();i++) {
                String found = findImageUrlDeep(a.opt(i), seen);
                if (!found.isEmpty()) return found;
            }
        }
        return "";
    }

    private static class ProfileNotFoundException extends Exception {
        final String nick; final ArrayList<JSONObject> suggestions;
        ProfileNotFoundException(String nick, ArrayList<JSONObject> suggestions) { super("not_found"); this.nick = nick; this.suggestions = suggestions == null ? new ArrayList<>() : suggestions; }
    }


    private String profileCacheKey(String raw, String hotelKey) {
        String key = normalizeNickKey(raw);
        if (key.isEmpty()) return "";
        String hotel = normalizeHotelKey(hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        return hotel + ":" + key;
    }

    private ProfileResult getCachedProfile(String nickKey) {
        String key = profileCacheKey(nickKey, currentHotelKey);
        if (key.isEmpty()) return null;
        ProfileResult cached = profileCache.get(key);
        Long cachedAt = profileCacheTimes.get(key);
        if (cached == null || cachedAt == null) return null;
        if (System.currentTimeMillis() - cachedAt > SESSION_CACHE_TTL_MS) {
            profileCache.remove(key);
            profileCacheTimes.remove(key);
            return null;
        }
        return cached;
    }

    private void putProfileCache(ProfileResult r, String aliasKey) {
        if (r == null) return;
        cleanupSessionProfileCache();
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        long now = System.currentTimeMillis();

        // Cache por nick é inseguro no Habbo porque o mesmo nick pode existir/reaparecer em outra conta
        // enquanto outra conta antiga com o mesmo nick está banida. Cacheie por uniqueId e, no máximo,
        // pelo alias quando o alias já é o próprio uniqueId.
        String idKey = profileCacheKey(r.uniqueId, hotel);
        if (!idKey.isEmpty()) {
            profileCache.put(idKey, r);
            profileCacheTimes.put(idKey, now);
        }

        String aliasRaw = normalizeNickKey(aliasKey);
        String idRaw = normalizeNickKey(r.uniqueId);
        if (!aliasRaw.isEmpty() && (idRaw.isEmpty() || aliasRaw.equals(idRaw))) {
            String alias = profileCacheKey(aliasRaw, hotel);
            if (!alias.isEmpty()) {
                profileCache.put(alias, r);
                profileCacheTimes.put(alias, now);
            }
        }
    }

    private void cleanupSessionProfileCache() {
        long now = System.currentTimeMillis();
        ArrayList<String> expired = new ArrayList<>();
        for (Map.Entry<String, Long> e : profileCacheTimes.entrySet()) {
            if (now - e.getValue() > SESSION_CACHE_TTL_MS) expired.add(e.getKey());
        }
        for (String k : expired) {
            profileCache.remove(k);
            profileCacheTimes.remove(k);
        }
    }

    private File profileCacheDir() {
        File dir = new File(getFilesDir(), "profile_cache");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private File profileCacheFile(String key) {
        String safe = normalizeNickKey(key).replaceAll("[^a-z0-9._-]", "_");
        if (safe.isEmpty()) safe = "profile";
        return new File(profileCacheDir(), safe + ".json");
    }

    private ProfileResult loadProfileCache(String key) {
        try {
            File f = profileCacheFile(key);
            if (!f.isFile()) return null;
            int days = getCacheDaysSetting();
            if (days > 0) {
                long age = System.currentTimeMillis() - f.lastModified();
                if (age > days * 86400000L) {
                    f.delete();
                    return null;
                }
            }
            String raw = readFile(f);
            if (raw == null || raw.trim().isEmpty()) return null;
            return profileFromJson(new JSONObject(raw));
        } catch (Exception ignored) { return null; }
    }

    private void saveProfileCache(ProfileResult r, String aliasKey) {
        // Cache apenas de sessão: gravar em disco foi removido para evitar dados pesados/desatualizados.
    }

    private void cleanupProfileCache() {
        try {
            File[] files = profileCacheDir().listFiles();
            if (files == null || files.length == 0) return;
            long now = System.currentTimeMillis();
            int days = getCacheDaysSetting();
            for (File f : files) {
                if (f.isFile() && days > 0 && now - f.lastModified() > days * 86400000L) f.delete();
            }
            files = profileCacheDir().listFiles();
            if (files == null) return;
            Arrays.sort(files, (a,b) -> Long.compare(b.lastModified(), a.lastModified()));
            int maxProfiles = getMaxProfilesSetting();
            for (int i = maxProfiles; i < files.length; i++) if (files[i].isFile()) files[i].delete();
            int maxMb = getMaxCacheMbSetting();
            if (maxMb > 0) {
                long maxBytes = maxMb * 1024L * 1024L;
                files = profileCacheDir().listFiles();
                if (files == null) return;
                Arrays.sort(files, (a,b) -> Long.compare(b.lastModified(), a.lastModified()));
                long total = cacheDirSize(profileCacheDir());
                for (int i = files.length - 1; i >= 0 && total > maxBytes; i--) {
                    if (files[i].isFile()) {
                        long len = files[i].length();
                        if (files[i].delete()) total -= len;
                    }
                }
            }
        } catch(Exception ignored) {}
    }


    private int getMaxProfilesSetting() {
        return 50;
    }

    private int getCacheDaysSetting() {
        return 1;
    }

    private int getMaxCacheMbSetting() {
        return 0;
    }

    private long cacheDirSize(File dir) {
        long total = 0;
        File[] files = dir == null ? null : dir.listFiles();
        if (files == null) return 0;
        for (File f : files) {
            if (f.isDirectory()) total += cacheDirSize(f);
            else total += Math.max(0, f.length());
        }
        return total;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        double kb = bytes / 1024.0;
        if (kb < 1024) return String.format(Locale.ROOT, "%.1f KB", kb);
        double mb = kb / 1024.0;
        return String.format(Locale.ROOT, "%.1f MB", mb);
    }


    private int sessionProfileCount() {
        cleanupSessionProfileCache();
        HashSet<String> seen = new HashSet<>();
        for (ProfileResult r : profileCache.values()) {
            if (r == null) continue;
            String key = "";
            if (r.uniqueId != null && !r.uniqueId.trim().isEmpty()) key = "id:" + r.uniqueId.trim().toLowerCase(Locale.ROOT);
            else if (r.name != null && !r.name.trim().isEmpty()) key = "name:" + normalizeHotelKey(r.hotelKey) + "|" + normalizeNickKey(r.name);
            else if (r.searchedNick != null && !r.searchedNick.trim().isEmpty()) key = "searched:" + normalizeHotelKey(r.hotelKey) + "|" + normalizeNickKey(r.searchedNick);
            if (!key.isEmpty()) seen.add(key);
        }
        return seen.size();
    }

    private long fileSize(File file) {
        try { return file != null && file.isFile() ? Math.max(0L, file.length()) : 0L; } catch(Exception ignored) { return 0L; }
    }

    private ArrayList<File> clearableCacheDirs() {
        ArrayList<File> dirs = new ArrayList<>();
        addClearableCacheDir(dirs, getCacheDir());
        try { addClearableCacheDir(dirs, getExternalCacheDir()); } catch(Exception ignored) {}
        addClearableCacheDir(dirs, profileCacheDir());
        return dirs;
    }

    private ArrayList<File> clearableCacheFiles() {
        ArrayList<File> files = new ArrayList<>();
        addClearableCacheFile(files, visualFigureDataDiskCacheFile());
        return files;
    }

    private void addClearableCacheDir(ArrayList<File> dirs, File dir) {
        if (dir == null) return;
        try {
            File canonical = dir.getCanonicalFile();
            for (File existing : dirs) {
                if (existing == null) continue;
                File ec = existing.getCanonicalFile();
                String e = ec.getPath();
                String c = canonical.getPath();
                if (c.equals(e) || c.startsWith(e + File.separator)) return;
            }
            dirs.add(canonical);
        } catch(Exception ignored) {
            if (!dirs.contains(dir)) dirs.add(dir);
        }
    }

    private void addClearableCacheFile(ArrayList<File> files, File file) {
        if (file == null) return;
        try {
            File canonical = file.getCanonicalFile();
            if (!files.contains(canonical)) files.add(canonical);
        } catch(Exception ignored) {
            if (!files.contains(file)) files.add(file);
        }
    }

    private boolean isInsideAnyDir(File file, ArrayList<File> dirs) {
        if (file == null || dirs == null) return false;
        try {
            String f = file.getCanonicalPath();
            for (File dir : dirs) {
                if (dir == null) continue;
                String d = dir.getCanonicalPath();
                if (f.equals(d) || f.startsWith(d + File.separator)) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private long clearableCacheBytes() {
        long total = 0L;
        ArrayList<File> dirs = clearableCacheDirs();
        for (File dir : dirs) total += cacheDirSize(dir);
        for (File file : clearableCacheFiles()) {
            if (!isInsideAnyDir(file, dirs)) total += fileSize(file);
        }
        return total;
    }

    private String cacheStatsText() {
        cleanupSessionProfileCache();
        return t("app_cache") + ": " + formatBytes(clearableCacheBytes());
    }

    private void updateCacheStatsLabelAsync(final TextView info) {
        if (info == null) return;
        executor.execute(() -> {
            final String txt = cacheStatsText();
            runOnUiThread(() -> {
                try { info.setText(txt); } catch(Exception ignored) {}
            });
        });
    }

    private void rebuildUiPreservingProfile() {
        ProfileResult keep = activeRenderedProfile;
        buildUi();
        if (keep != null) renderProfile(keep);
    }

    private void clearProfileCache() {
        clearProfileCache(null);
    }

    private void clearProfileCache(Runnable done) {
        profileCache.clear();
        profileCacheTimes.clear();
        visualFigureDataCache = null;
        visualFigureDataLoadedAt = 0L;
        visualEditorCachedFigure = DEFAULT_VISUAL_FIGURE;
        visualEditorCachedGender = "M";
        visualEditorCachedType = "hd";
        visualEditorCachedDirection = 2;
        visualItemViewsSessionCache.clear();
        visualItemRenderLimits.clear();
        try {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                .remove(PREF_VISUAL_EDITOR_FIGURE)
                .remove(PREF_VISUAL_EDITOR_GENDER)
                .remove(PREF_VISUAL_EDITOR_TYPE)
                .remove(PREF_VISUAL_EDITOR_DIRECTION)
                .remove(PREF_FAVORITE_ONLINE_STATES)
                .apply();
        } catch(Exception ignored) {}

        // Evita travar a UI ao limpar cache; o cache em disco do Glide é removido abaixo em thread separada.

        executor.execute(() -> {
            try { Glide.get(MainActivity.this).clearDiskCache(); } catch (Exception ignored) {}

            // Usa a mesma lista usada em clearableCacheBytes(), para o tamanho mostrado
            // em Configurações bater com o que o botão realmente remove.
            ArrayList<File> dirs = clearableCacheDirs();
            File profileRoot = null;
            try { profileRoot = profileCacheDir().getCanonicalFile(); } catch(Exception ignored) { profileRoot = profileCacheDir(); }
            for (File dir : dirs) {
                boolean deleteRoot = false;
                try { deleteRoot = profileRoot != null && dir != null && dir.getCanonicalFile().equals(profileRoot); } catch(Exception ignored) {}
                deleteContents(dir, deleteRoot);
            }
            for (File file : clearableCacheFiles()) {
                if (!isInsideAnyDir(file, dirs)) {
                    try { if (file != null && file.exists()) file.delete(); } catch(Exception ignored) {}
                }
            }

            // Não apagamos codeCacheDir aqui para evitar travamentos/descompilações desnecessárias.
            try { profileCacheDir().mkdirs(); } catch(Exception ignored) {}

            runOnUiThread(() -> {
                profileCache.clear();
                profileCacheTimes.clear();
                if (done != null) done.run();
            });
        });
    }

    private void deleteContents(File dir, boolean deleteRoot) {
        if (dir == null || !dir.exists()) return;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f == null) continue;
                if (f.isDirectory()) deleteContents(f, true);
                else { try { f.delete(); } catch(Exception ignored) {} }
            }
        }
        if (deleteRoot) { try { dir.delete(); } catch(Exception ignored) {} }
    }

    private int dialogFillColor() { return lightTheme ? Color.rgb(255,255,255) : Color.rgb(28, 18, 42); }
    private int dialogStrokeColor() { return lightTheme ? Color.rgb(216,216,216) : Color.argb(42,255,255,255); }

    private void loadOpenedProfilesHistory() {
        openedProfilesHistory.clear();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_OPENED_HISTORY, "");
        if (raw == null || raw.trim().isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length() && openedProfilesHistory.size() < 50; i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String nick = o.optString("nick", "").trim();
                if (nick.isEmpty()) continue;
                String hotel = normalizeHotelKey(o.optString("hotel", "br"));
                if (hotel.isEmpty()) hotel = "br";
                openedProfilesHistory.add(new ProfileHistoryItem(nick, o.optString("figure", ""), hotel, o.optString("uniqueId", o.optString("id", ""))));
            }
        } catch(Exception ignored) {}
    }

    private void saveOpenedProfilesHistory() {
        JSONArray arr = new JSONArray();
        try {
            for (ProfileHistoryItem item : openedProfilesHistory) {
                JSONObject o = new JSONObject();
                o.put("nick", item.nick);
                o.put("figure", item.figure);
                o.put("uniqueId", item.uniqueId);
                String hotel = normalizeHotelKey(item.hotelKey);
                o.put("hotel", hotel.isEmpty() ? "br" : hotel);
                arr.put(o);
            }
        } catch(Exception ignored) {}
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_OPENED_HISTORY, arr.toString()).apply();
    }


    private int bottomNavIconColor(boolean selected) {
        if (selected) return lightTheme ? Color.rgb(18,18,18) : Color.WHITE;
        return lightTheme ? Color.rgb(120,120,128) : Color.argb(155,255,255,255);
    }

    private int bottomNavDividerColor() {
        return lightTheme ? Color.rgb(224,224,228) : Color.rgb(44,44,52);
    }

    private Drawable bottomNavBackground() {
        return new BottomNavBarDrawable();
    }

    private void addBottomNavigation(FrameLayout host, int selectedTab, Dialog activeDialog) {
        if (host == null) return;

        FrameLayout navWrap = new FrameLayout(this);
        navWrap.setBackground(bottomNavBackground());
        if (Build.VERSION.SDK_INT >= 21) navWrap.setElevation(dp(14));

        View divider = new View(this);
        divider.setBackgroundColor(bottomNavDividerColor());
        FrameLayout.LayoutParams dividerLp = new FrameLayout.LayoutParams(-1, dp(1), Gravity.TOP);
        navWrap.addView(divider, dividerLp);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(12), dp(4), dp(12), dp(4));
        FrameLayout.LayoutParams navInnerLp = new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER);
        navWrap.addView(nav, navInnerLp);

        FrameLayout.LayoutParams navLp = new FrameLayout.LayoutParams(-1, dp(56), Gravity.BOTTOM);
        navLp.leftMargin = 0;
        navLp.rightMargin = 0;
        navLp.bottomMargin = 0;
        host.addView(navWrap, navLp);

        nav.addView(bottomNavItem("home", selectedTab == 0, () -> {
            if (activeDialog != null) activeDialog.dismiss();
        }), new LinearLayout.LayoutParams(0, -1, 1));

        nav.addView(bottomNavItem("visuals", selectedTab == 1, () -> {
            if (selectedTab == 1) return;
            maybeShowProfileInterstitial();
            showVisualEditorDialog();
            if (activeDialog != null) uiHandler.postDelayed(() -> {
                try { activeDialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        }), new LinearLayout.LayoutParams(0, -1, 1));

        nav.addView(bottomNavItem("heart", selectedTab == 2, () -> {
            if (selectedTab == 2) return;
            showFavoriteProfilesDialog();
            if (activeDialog != null) uiHandler.postDelayed(() -> {
                try { activeDialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        }), new LinearLayout.LayoutParams(0, -1, 1));

        nav.addView(bottomNavItem("settings", selectedTab == 3, () -> {
            if (selectedTab == 3) return;
            showSettingsDialog();
            if (activeDialog != null) uiHandler.postDelayed(() -> {
                try { activeDialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        }), new LinearLayout.LayoutParams(0, -1, 1));
    }

    private View bottomNavItem(String icon, boolean selected, final Runnable action) {
        FrameLayout item = new FrameLayout(this);
        item.setClickable(true);
        item.setFocusable(true);
        item.setBackgroundColor(Color.TRANSPARENT);
        item.setPadding(0, 0, 0, 0);

        TextView iv = text("", 1, bottomNavIconColor(selected), true);
        iv.setGravity(Gravity.CENTER);
        iv.setPadding(0, 0, 0, 0);
        iv.setBackground(new BottomNavIconDrawable(icon, selected));
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.CENTER);
        item.addView(iv, ip);

        if ("heart".equals(icon)) {
            TextView badge = text("", 10, Color.WHITE, true);
            badge.setTextColor(Color.WHITE);
            badge.setGravity(Gravity.CENTER);
            badge.setIncludeFontPadding(false);
            badge.setPadding(dp(4), 0, dp(4), 0);
            int count = favoriteOnlineCount();
            int bw = count >= 10 ? dp(24) : dp(18);
            badge.setMinWidth(bw);
            badge.setBackground(round(lightTheme ? Color.rgb(15, 15, 18) : purple, dp(999), lightTheme ? Color.rgb(255,255,255) : Color.argb(150,0,0,0), 1));
            badge.setText(count > 0 ? String.valueOf(count) : "");
            badge.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(bw, dp(18), Gravity.CENTER);
            bp.leftMargin = dp(18);
            bp.topMargin = -dp(12);
            item.addView(badge, bp);
            favoriteOnlineBadgeViews.add(badge);
            updateFavoriteOnlineBadgeText();
        }

        item.setOnClickListener(v -> {
            if (action != null) action.run();
        });
        return item;
    }




    private View visualPurpleLoader(String message) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(12), dp(14), dp(12), dp(14));
        box.setMinimumHeight(dp(84));
        box.setBackground(round(Color.argb(lightTheme ? 18 : 26, 139, 52, 217), dp(18), Color.argb(70, 139, 52, 217), 1));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) {
            spinner.setIndeterminateTintList(ColorStateList.valueOf(Color.rgb(150, 58, 242)));
        }
        box.addView(spinner, new LinearLayout.LayoutParams(dp(34), dp(34)));

        String cleanMessage = message == null ? "" : message.trim();
        if (!cleanMessage.isEmpty()) {
            TextView label = text(cleanMessage, 13, lightTheme ? Color.rgb(70, 36, 92) : Color.WHITE, true);
            label.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.topMargin = dp(10);
            box.addView(label, lp);
        }
        return box;
    }

    private View visualFigureDataLoadingView() {
        FrameLayout outer = new FrameLayout(this);
        outer.setPadding(dp(18), dp(18), dp(18), dp(18));
        outer.setBackgroundColor(Color.TRANSPARENT);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(22), dp(24), dp(22), dp(24));
        card.setMinimumHeight(dp(300));

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                lightTheme
                        ? new int[]{Color.rgb(255, 255, 255), Color.rgb(247, 241, 255), Color.rgb(238, 228, 255)}
                        : new int[]{Color.rgb(25, 13, 40), Color.rgb(42, 20, 67), Color.rgb(20, 12, 34)}
        );
        bg.setCornerRadius(dp(24));
        bg.setStroke(dp(1), lightTheme ? Color.rgb(224, 204, 247) : Color.argb(95, 190, 115, 255));
        card.setBackground(bg);
        if (Build.VERSION.SDK_INT >= 21) card.setElevation(dp(8));

        FrameLayout spinnerWrap = new FrameLayout(this);
        GradientDrawable halo = new GradientDrawable();
        halo.setShape(GradientDrawable.OVAL);
        halo.setColor(lightTheme ? Color.rgb(248, 242, 255) : Color.argb(48, 160, 62, 255));
        halo.setStroke(dp(1), lightTheme ? Color.rgb(224, 204, 247) : Color.argb(80, 255, 255, 255));
        spinnerWrap.setBackground(halo);
        if (Build.VERSION.SDK_INT >= 21) spinnerWrap.setElevation(dp(3));

        ProgressBar spinner = new ProgressBar(this);
        spinner.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) {
            spinner.setIndeterminateTintList(ColorStateList.valueOf(Color.rgb(139, 52, 217)));
        }
        FrameLayout.LayoutParams spLp = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.CENTER);
        spinnerWrap.addView(spinner, spLp);
        LinearLayout.LayoutParams haloLp = new LinearLayout.LayoutParams(dp(72), dp(72));
        card.addView(spinnerWrap, haloLp);

        TextView title = text(t("loading_visuals"), 16, lightTheme ? Color.rgb(40, 23, 55) : Color.WHITE, true);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.topMargin = dp(16);
        card.addView(title, titleLp);

        TextView sub = text("Preparando roupas e cores...", 12, lightTheme ? Color.rgb(110, 86, 130) : Color.argb(180, 255, 255, 255), false);
        sub.setGravity(Gravity.CENTER);
        sub.setIncludeFontPadding(false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.topMargin = dp(7);
        card.addView(sub, subLp);

        LinearLayout dots = new LinearLayout(this);
        dots.setOrientation(LinearLayout.HORIZONTAL);
        dots.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams dotsLp = new LinearLayout.LayoutParams(-1, dp(8));
        dotsLp.topMargin = dp(16);
        for (int i = 0; i < 3; i++) {
            View dot = new View(this);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(i == 1 ? Color.rgb(139, 52, 217) : Color.argb(lightTheme ? 90 : 130, 139, 52, 217));
            dot.setBackground(dotBg);
            LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(dp(7), dp(7));
            dlp.leftMargin = dp(3);
            dlp.rightMargin = dp(3);
            dots.addView(dot, dlp);
        }
        card.addView(dots, dotsLp);

        outer.addView(card, new FrameLayout.LayoutParams(-1, -1, Gravity.CENTER));
        return outer;
    }

    private void bindNestedScrollTouch(final ScrollView scroll) {
        if (scroll == null) return;
        final float[] lastY = {0f};
        scroll.setNestedScrollingEnabled(false);
        scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
        scroll.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastY[0] = event.getY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float y = event.getY();
                int dy = (int)(lastY[0] - y);
                lastY[0] = y;
                scroll.scrollBy(0, dy);
                if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
                return true;
            }
            return true;
        });
    }

    private void bindColorPanelTouchLock(final View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                if (!(parent instanceof View)) break;
                parent = ((View) parent).getParent();
            }
            return false;
        });
    }

    private void loadVisualEditorState() {
        try {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            visualEditorCachedFigure = sp.getString(PREF_VISUAL_EDITOR_FIGURE, DEFAULT_VISUAL_FIGURE);
            visualEditorCachedGender = sp.getString(PREF_VISUAL_EDITOR_GENDER, "M");
            visualEditorCachedType = sp.getString(PREF_VISUAL_EDITOR_TYPE, "hd");
            visualEditorCachedDirection = sp.getInt(PREF_VISUAL_EDITOR_DIRECTION, 2);
            if (visualEditorCachedFigure == null || visualEditorCachedFigure.trim().isEmpty()) visualEditorCachedFigure = DEFAULT_VISUAL_FIGURE;
            if (visualEditorCachedGender == null || visualEditorCachedGender.trim().isEmpty()) visualEditorCachedGender = "M";
            if (visualEditorCachedType == null || visualEditorCachedType.trim().isEmpty()) visualEditorCachedType = "hd";
        } catch(Exception ignored) {}
    }

    private void saveVisualEditorState() {
        try {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(PREF_VISUAL_EDITOR_FIGURE, visualEditorCachedFigure == null ? DEFAULT_VISUAL_FIGURE : visualEditorCachedFigure)
                    .putString(PREF_VISUAL_EDITOR_GENDER, visualEditorCachedGender == null ? "M" : visualEditorCachedGender)
                    .putString(PREF_VISUAL_EDITOR_TYPE, visualEditorCachedType == null ? "hd" : visualEditorCachedType)
                    .putInt(PREF_VISUAL_EDITOR_DIRECTION, visualEditorCachedDirection)
                    .apply();
        } catch(Exception ignored) {}
    }

    private String avatarMedium(String figure, int direction) {
        return "https://www.habbo.com.br/habbo-imaging/avatarimage?figure=" + enc(figure) + "&size=m&direction=" + direction + "&head_direction=" + direction + "&gesture=std&action=std";
    }

    private void showVisualEditorDialog() {
        final Dialog dialog = new Dialog(this);
        PullDispatchFrameLayout full = new PullDispatchFrameLayout(this);
        full.setBackground(makeBg());

        ScrollView visualScroll = new ScrollView(this);
        visualScroll.setFillViewport(false);
        visualScroll.setVerticalScrollBarEnabled(true);
        visualScroll.setScrollbarFadingEnabled(false);
        tintScrollBar(visualScroll);
        full.addView(visualScroll, new FrameLayout.LayoutParams(-1, -1));

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(16), dp(28), dp(16), dp(88));
        wrap.setBackgroundColor(Color.TRANSPARENT);
        visualScroll.addView(wrap, new ScrollView.LayoutParams(-1, -2));

        addBottomNavigation(full, 1, dialog);
        dialog.setContentView(full);

        TextView title = habboText(t("visual_editor"), 24, true);
        title.setGravity(Gravity.CENTER);
        title.setIncludeFontPadding(false);
        wrap.addView(title, lp(-1, dp(28), 0, 0, 0, 12));

        LinearLayout nickRow = new LinearLayout(this);
        nickRow.setOrientation(LinearLayout.HORIZONTAL);
        nickRow.setGravity(Gravity.CENTER_VERTICAL);
        EditText nickInput = new EditText(this);
        nickInput.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
        nickInput.setHintTextColor(lightTheme ? Color.rgb(125,125,125) : Color.argb(150,255,255,255));
        nickInput.setTextSize(14);
        nickInput.setTypeface(habboFont);
        nickInput.setSingleLine(true);
        nickInput.setHint(t("type_nick"));
        nickInput.setPadding(dp(12), 0, dp(12), 0);
        nickInput.setBackground(round(lightTheme ? Color.WHITE : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255), 1));
        nickRow.addView(nickInput, new LinearLayout.LayoutParams(0, dp(46), 1));

        TextView loadNick = dialogButton(t("search_button"));
        LinearLayout.LayoutParams loadLp = new LinearLayout.LayoutParams(dp(92), dp(46));
        loadLp.leftMargin = dp(8);
        nickRow.addView(loadNick, loadLp);
        wrap.addView(nickRow, lp(-1, dp(46), 0, 0, 0, 8));
        View visualNickBanner = buildVisualNickSearchBannerAd();
        if (visualNickBanner != null) {
            wrap.addView(visualNickBanner, lp(-1, dp(68), 0, 0, 0, 10));
            requestVisualNickSearchBannerLoadIfNeeded();
        }

        nickInput.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                loadNick.performClick();
                try {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    if (imm != null) imm.hideSoftInputFromWindow(nickInput.getWindowToken(), 0);
                } catch(Exception ignored) {}
                nickInput.clearFocus();
                return true;
            }
            return false;
        });

        FrameLayout visualPreviewFrame = new FrameLayout(this);
        visualPreviewFrame.setBackground(round(lightTheme ? Color.rgb(252,252,252) : Color.rgb(15, 8, 25), dp(20), lightTheme ? Color.rgb(222,222,226) : Color.argb(22,255,255,255), 1));
        wrap.addView(visualPreviewFrame, lp(-1, dp(220), 0, 0, 0, 10));

        ImageView preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setScaleType(ImageView.ScaleType.FIT_CENTER);
        preview.setPadding(dp(20), dp(8), dp(20), dp(8));
        visualPreviewFrame.addView(preview, new FrameLayout.LayoutParams(-1, -1));


        final String[] currentFigure = {visualEditorCachedFigure == null || visualEditorCachedFigure.trim().isEmpty() ? DEFAULT_VISUAL_FIGURE : visualEditorCachedFigure};
        final String[] currentGender = {visualEditorCachedGender == null || visualEditorCachedGender.trim().isEmpty() ? "M" : visualEditorCachedGender};
        final String[] currentType = {"hd"};
        final int[] visualDirection = {visualEditorCachedDirection};
        final Runnable[] refreshAll = new Runnable[1];
        final JSONObject[] figureDataRef = {visualFigureDataCache};
        final boolean[] visualInitialContentRendered = {false};
        final boolean[] visualDialogClosed = {false};
        final Runnable[] finishVisualInitialLoadIfPossible = new Runnable[1];
        final Runnable[] visualInitialLoadWatchdog = new Runnable[1];
        dialog.setOnDismissListener(d -> visualDialogClosed[0] = true);

        TextView saveLookBtn = visualCornerIconButton(new VisualSaveLookDrawable());
        FrameLayout.LayoutParams saveLookLp = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.TOP | Gravity.LEFT);
        saveLookLp.topMargin = dp(28);
        saveLookLp.leftMargin = dp(15);
        full.addView(saveLookBtn, saveLookLp);
        if (Build.VERSION.SDK_INT >= 21) saveLookBtn.setElevation(dp(18));
        saveLookBtn.setOnClickListener(v -> saveVisualEditorLook(currentFigure[0], currentGender[0]));

        TextView savedLooksBtn = visualCornerIconButton(new VisualSavedLooksDrawable());
        FrameLayout.LayoutParams savedLooksLp = new FrameLayout.LayoutParams(dp(28), dp(28), Gravity.TOP | Gravity.RIGHT);
        savedLooksLp.topMargin = dp(28);
        savedLooksLp.rightMargin = dp(15);
        full.addView(savedLooksBtn, savedLooksLp);
        if (Build.VERSION.SDK_INT >= 21) savedLooksBtn.setElevation(dp(18));
        savedLooksBtn.setOnClickListener(v -> showSavedVisualsDialog(currentFigure, currentGender, currentType, figureDataRef, refreshAll));

        LinearLayout catTabs = new LinearLayout(this);
        catTabs.setOrientation(LinearLayout.VERTICAL);
        catTabs.setPadding(0, 0, 0, 0);
        wrap.addView(catTabs, lp(-1, dp(110), 0, 0, 0, 12));

        ScrollView itemScroll = new ScrollView(this);
        itemScroll.setVerticalScrollBarEnabled(true);
        itemScroll.setScrollbarFadingEnabled(false);
        itemScroll.setNestedScrollingEnabled(true);
        itemScroll.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            if (parent != null) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    parent.requestDisallowInterceptTouchEvent(true);
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
        tintScrollBar(itemScroll);
        LinearLayout itemsArea = new LinearLayout(this);
        itemsArea.setOrientation(LinearLayout.VERTICAL);
        itemScroll.addView(itemsArea, new ScrollView.LayoutParams(-1, -2));
        itemScroll.setBackground(round(Color.argb(lightTheme ? 18 : 44, 0, 0, 0), dp(20), Color.argb(lightTheme ? 30 : 35, 255,255,255), 1));

        FrameLayout visualItemsHost = new FrameLayout(this);
        visualItemsHost.setBackgroundColor(Color.TRANSPARENT);
        visualItemsHost.addView(itemScroll, new FrameLayout.LayoutParams(-1, -1));
        View visualFigureLoader = visualFigureDataLoadingView();
        visualItemsHost.addView(visualFigureLoader, new FrameLayout.LayoutParams(-1, -1));
        wrap.addView(visualItemsHost, lp(-1, dp(310), 0, 0, 0, 10));

        final Runnable showVisualFigureLoader = () -> {
            itemScroll.setVisibility(View.INVISIBLE);
            visualFigureLoader.setVisibility(View.VISIBLE);
        };
        final Runnable hideVisualFigureLoader = () -> {
            visualFigureLoader.setVisibility(View.GONE);
            itemScroll.setVisibility(View.VISIBLE);
        };

        LinearLayout colorPanel = new LinearLayout(this);
        colorPanel.setOrientation(LinearLayout.VERTICAL);
        colorPanel.setPadding(dp(10), dp(10), dp(10), dp(10));
        colorPanel.setBackground(round(Color.argb(lightTheme ? 24 : 44, 0, 0, 0), dp(20), Color.argb(lightTheme ? 30 : 35, 255,255,255), 1));
        colorPanel.setVisibility(View.GONE);
        bindColorPanelTouchLock(colorPanel);
        wrap.addView(colorPanel, lp(-1, -2, 0, 6, 0, 8));
        View visualColorsBanner = buildVisualColorsBannerAd();
        if (visualColorsBanner != null) {
            wrap.addView(visualColorsBanner, lp(-1, dp(68), 0, 0, 0, 10));
            requestVisualColorsBannerLoadIfNeeded();
        }

        Runnable updatePreview = () -> {
            visualEditorCachedFigure = currentFigure[0];
            visualEditorCachedGender = currentGender[0];
            visualEditorCachedType = currentType[0];
            visualEditorCachedDirection = visualDirection[0];
            saveVisualEditorState();
            Glide.with(MainActivity.this)
                .load(avatarFull(currentFigure[0], visualDirection[0]))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.pre_load)
                .error(R.drawable.pre_load)
                .into(preview);
        };
        updatePreview.run();

        final float[] visualSwipeStartX = {0f};
        final float[] visualSwipeStartY = {0f};
        final boolean[] visualSwipeConsumed = {false};
        preview.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) requestDisallowParents(v, true);
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    visualSwipeStartX[0] = event.getX();
                    visualSwipeStartY[0] = event.getY();
                    visualSwipeConsumed[0] = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float moveDx = event.getX() - visualSwipeStartX[0];
                    float moveDy = event.getY() - visualSwipeStartY[0];
                    if (!visualSwipeConsumed[0] && Math.abs(moveDx) >= dp(34) && Math.abs(moveDx) > Math.abs(moveDy)) {
                        if (moveDx > 0) visualDirection[0] = (visualDirection[0] + 1) % 8;
                        else {
                            visualDirection[0] = visualDirection[0] - 1;
                            if (visualDirection[0] < 0) visualDirection[0] = 7;
                        }
                        visualSwipeConsumed[0] = true;
                        visualSwipeStartX[0] = event.getX();
                        visualSwipeStartY[0] = event.getY();
                        updatePreview.run();
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    requestDisallowParents(v, false);
                    return true;
            }
            return true;
        });

        refreshAll[0] = () -> {
            currentGender[0] = detectVisualGenderFromFigure(currentFigure[0], figureDataRef[0], currentGender[0]);
            JSONObject currentData = figureDataRef[0];
            String activeItemType = getVisualItemTypeForUiCategory(currentType[0]);
            if (currentData != null && visualCategory(currentData, activeItemType) != null) {
                hideVisualFigureLoader.run();
                colorPanel.setVisibility(View.VISIBLE);
            }
            renderVisualCategories(catTabs, currentType, currentGender, currentFigure, currentData, refreshAll[0]);
            renderVisualItems(itemsArea, colorPanel, currentFigure, currentGender, currentType, currentData, updatePreview);
            updatePreview.run();
        };

        finishVisualInitialLoadIfPossible[0] = () -> {
            if (visualInitialContentRendered[0] || visualDialogClosed[0]) return;
            JSONObject availableData = figureDataRef[0] != null ? figureDataRef[0] : visualFigureDataCache;
            if (availableData == null) return;
            String requestedType = getVisualItemTypeForUiCategory(currentType[0]);
            boolean hasRequestedCategory = visualCategory(availableData, requestedType) != null;
            boolean hasAnyCategory = countVisualLoadedCategories(availableData) > 0;
            if (!hasRequestedCategory && !hasAnyCategory) return;
            figureDataRef[0] = availableData;
            visualInitialContentRendered[0] = true;
            hideVisualFigureLoader.run();
            refreshAll[0].run();
        };

        visualInitialLoadWatchdog[0] = () -> {
            if (visualInitialContentRendered[0] || visualDialogClosed[0]) return;
            finishVisualInitialLoadIfPossible[0].run();
            if (!visualInitialContentRendered[0] && !visualDialogClosed[0]) {
                uiHandler.postDelayed(visualInitialLoadWatchdog[0], 250L);
            }
        };

        loadNick.setOnClickListener(v -> {
            try {
                InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) imm.hideSoftInputFromWindow(nickInput.getWindowToken(), 0);
            } catch(Exception ignored) {}
            nickInput.clearFocus();
            String nick = nickInput.getText().toString().trim();
            if (nick.isEmpty()) {
                toast(t("type_nick_toast"));
                return;
            }
            loadNick.setText("...");
            executor.submit(() -> {
                try {
                    JSONObject p = validProfileObject(tryJson(habboApiUrl("/api/public/users?name=" + enc(nick))));
                    String fig = firstText(p, "figureString", "figure");
                    String g = firstText(p, "gender", "sex");
                    if (fig.isEmpty()) {
                        JSONObject d = unwrap(tryJson(habbodexProfileByNameUrl(nick)));
                        fig = firstText(d, "figureString", "figure");
                        if (g.isEmpty()) g = firstText(d, "gender", "sex");
                    }
                    final String f = fig;
                    final String gender = g;
                    runOnUiThread(() -> {
                        loadNick.setText(t("search_button"));
                        if (f == null || f.trim().isEmpty()) {
                            toast(t("not_found_simple"));
                            return;
                        }
                        currentFigure[0] = f.trim();
                        currentGender[0] = detectVisualGenderFromFigure(currentFigure[0], figureDataRef[0], normalizeVisualGender(gender, currentGender[0]));
                        visualItemViewsSessionCache.clear();
        visualItemRenderLimits.clear();
                        refreshAll[0].run();
                    });
                } catch(Exception e) {
                    runOnUiThread(() -> {
                        loadNick.setText(t("search_button"));
                        toast(t("cannot_load_visuals"));
                    });
                }
            });
        });

        if (figureDataRef[0] == null || countVisualLoadedCategories(figureDataRef[0]) == 0) {
            itemsArea.removeAllViews();
            showVisualFigureLoader.run();
            uiHandler.postDelayed(visualInitialLoadWatchdog[0], 250L);
            loadVisualFigureData(data -> {
                figureDataRef[0] = data;
                if (!visualInitialContentRendered[0]) {
                    visualInitialContentRendered[0] = true;
                }
                hideVisualFigureLoader.run();
                refreshAll[0].run();
            }, () -> {
                finishVisualInitialLoadIfPossible[0].run();
                if (!visualInitialContentRendered[0]) {
                    hideVisualFigureLoader.run();
                    itemsArea.removeAllViews();
                    itemsArea.addView(centerNote(t("cannot_load_visuals")));
                }
            });
        } else {
            visualInitialContentRendered[0] = true;
            hideVisualFigureLoader.run();
            refreshAll[0].run();
        }

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            w.setWindowAnimations(0);
            w.setAttributes(params);
        }
    }


    private TextView visualCornerIconButton(Drawable drawable) {
        TextView btn = text("", 1, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(0, 0, 0, 0);
        btn.setIncludeFontPadding(false);
        btn.setBackground(drawable);
        btn.setClickable(true);
        btn.setFocusable(true);
        return btn;
    }

    private ArrayList<SavedVisualLook> loadSavedVisualLooks() {
        ArrayList<SavedVisualLook> out = new ArrayList<>();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_SAVED_VISUALS, "[]");
        try {
            JSONArray arr = new JSONArray(raw == null || raw.trim().isEmpty() ? "[]" : raw);
            HashSet<String> seen = new HashSet<>();
            for (int i = 0; i < arr.length() && out.size() < MAX_SAVED_VISUALS; i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String figure = o.optString("figure", "").trim();
                if (figure.isEmpty() || seen.contains(figure)) continue;
                seen.add(figure);
                String gender = normalizeVisualGender(o.optString("gender", "M"), "M");
                out.add(new SavedVisualLook(figure, gender));
            }
        } catch(Exception ignored) {}
        return out;
    }

    private void saveSavedVisualLooks(ArrayList<SavedVisualLook> looks) {
        JSONArray arr = new JSONArray();
        try {
            if (looks != null) {
                HashSet<String> seen = new HashSet<>();
                for (SavedVisualLook look : looks) {
                    if (look == null || look.figure == null || look.figure.trim().isEmpty()) continue;
                    String figure = look.figure.trim();
                    if (seen.contains(figure)) continue;
                    seen.add(figure);
                    JSONObject o = new JSONObject();
                    o.put("figure", figure);
                    o.put("gender", normalizeVisualGender(look.gender, "M"));
                    arr.put(o);
                    if (arr.length() >= MAX_SAVED_VISUALS) break;
                }
            }
        } catch(Exception ignored) {}
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_SAVED_VISUALS, arr.toString()).apply();
    }

    private void saveVisualEditorLook(String figure, String gender) {
        String cleanFigure = figure == null ? "" : figure.trim();
        if (cleanFigure.isEmpty()) return;
        ArrayList<SavedVisualLook> looks = loadSavedVisualLooks();
        for (SavedVisualLook look : looks) {
            if (look != null && cleanFigure.equals(look.figure)) {
                toast(t("visual_already_saved"));
                return;
            }
        }
        if (looks.size() >= MAX_SAVED_VISUALS) {
            toast(tr("visual_saved_limit", MAX_SAVED_VISUALS));
            return;
        }
        String detectedGender = detectVisualGenderFromFigure(cleanFigure, visualFigureDataCache, normalizeVisualGender(gender, "M"));
        looks.add(0, new SavedVisualLook(cleanFigure, detectedGender));
        saveSavedVisualLooks(looks);
        toast(t("visual_saved"));
    }

    private void showSavedVisualsDialog(final String[] currentFigure, final String[] currentGender, final String[] currentType, final JSONObject[] figureDataRef, final Runnable[] refreshAll) {
        final Dialog savedDialog = new Dialog(this);
        LinearLayout rootDialog = new LinearLayout(this);
        rootDialog.setOrientation(LinearLayout.VERTICAL);
        rootDialog.setPadding(dp(18), dp(18), dp(18), dp(18));
        rootDialog.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        savedDialog.setContentView(rootDialog);

        TextView title = habboText(t("saved_visuals"), 22, true);
        title.setGravity(Gravity.CENTER);
        rootDialog.addView(title, lp(-1, -2, 0, 0, 0, 14));

        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(false);
        sv.setVerticalScrollBarEnabled(true);
        tintScrollBar(sv);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new ScrollView.LayoutParams(-1, -2));
        rootDialog.addView(sv, lp(-1, Math.min(dp(430), getResources().getDisplayMetrics().heightPixels - dp(220)), 0, 0, 0, 14));

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            list.removeAllViews();
            ArrayList<SavedVisualLook> looks = loadSavedVisualLooks();
            if (looks.isEmpty()) {
                list.addView(centerNote(t("no_saved_visuals")));
                return;
            }
            for (int i = 0; i < looks.size(); i++) {
                final int index = i;
                final SavedVisualLook look = looks.get(i);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setPadding(dp(10), dp(8), dp(10), dp(8));
                row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(16), lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255), 1));
                list.addView(row, lp(-1, dp(86), 0, 0, 0, 8));

                ImageView avatar = new ImageView(this);
                avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
                Glide.with(MainActivity.this)
                    .load(avatarMedium(look.figure, 2))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.pre_load)
                    .error(R.drawable.pre_load)
                    .into(avatar);
                row.addView(avatar, new LinearLayout.LayoutParams(dp(66), dp(76)));

                LinearLayout mid = new LinearLayout(this);
                mid.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
                mp.leftMargin = dp(10);
                row.addView(mid, mp);

                TextView label = habboText("#" + (index + 1), 16, true);
                label.setTextColor(lightTheme ? Color.rgb(33,33,33) : Color.WHITE);
                mid.addView(label, lp(-1, -2, 0, 0, 0, 2));

                TextView code = text(look.figure, 11, lightTheme ? Color.rgb(78,78,86) : Color.argb(185,255,255,255), false);
                code.setSingleLine(true);
                code.setEllipsize(TextUtils.TruncateAt.END);
                mid.addView(code, new LinearLayout.LayoutParams(-1, -2));

                TextView remove = text("", 18, Color.WHITE, true);
                remove.setGravity(Gravity.CENTER);
                remove.setBackground(new RemoveXDrawable());
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(38), dp(38));
                rp.leftMargin = dp(8);
                row.addView(remove, rp);

                row.setOnClickListener(v -> {
                    Runnable applyLook = () -> {
                        if (currentFigure != null && currentFigure.length > 0) currentFigure[0] = look.figure;
                        if (currentGender != null && currentGender.length > 0) {
                            JSONObject data = figureDataRef == null ? null : figureDataRef[0];
                            String selectedGenderFallback = normalizeVisualGender(currentGender[0], "M");
                            currentGender[0] = detectVisualGenderFromFigure(look.figure, data, selectedGenderFallback);
                        }
                        if (currentType != null && currentType.length > 0) currentType[0] = "hd";
                        visualItemViewsSessionCache.clear();
        visualItemRenderLimits.clear();
                        try { savedDialog.dismiss(); } catch(Exception ignored) {}
                        if (refreshAll != null && refreshAll.length > 0 && refreshAll[0] != null) refreshAll[0].run();
                    };
                    if ((figureDataRef == null || figureDataRef[0] == null) && visualFigureDataCache == null) {
                        loadVisualFigureData(data -> {
                            if (figureDataRef != null) figureDataRef[0] = data;
                            applyLook.run();
                        }, applyLook);
                    } else {
                        if (figureDataRef != null && figureDataRef[0] == null) figureDataRef[0] = visualFigureDataCache;
                        applyLook.run();
                    }
                });

                remove.setOnClickListener(v -> {
                    ArrayList<SavedVisualLook> fresh = loadSavedVisualLooks();
                    if (index >= 0 && index < fresh.size()) fresh.remove(index);
                    saveSavedVisualLooks(fresh);
                    toast(t("visual_removed"));
                    render[0].run();
                });
            }
        };
        render[0].run();

        TextView close = dialogButton(t("close"));
        close.setOnClickListener(v -> savedDialog.dismiss());
        rootDialog.addView(close, lp(-1, dp(48), 0, 0, 0, 0));

        savedDialog.show();
        Window w = savedDialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }
    }

    private static class SavedVisualLook {
        final String figure;
        final String gender;
        SavedVisualLook(String figure, String gender) {
            this.figure = figure == null ? "" : figure.trim();
            this.gender = gender == null ? "M" : gender.trim();
        }
    }

    private File visualFigureDataDiskCacheFile() {
        return new File(getFilesDir(), VISUAL_FIGUREDATA_DISK_CACHE_FILE);
    }

    private JSONObject readVisualFigureDataDiskCache(boolean requireFresh) {
        File file = visualFigureDataDiskCacheFile();
        try {
            if (file == null || !file.exists() || file.length() <= 0L) return null;
            long age = System.currentTimeMillis() - Math.max(0L, file.lastModified());
            if (requireFresh && age > VISUAL_FIGUREDATA_CACHE_TTL_MS) return null;
            StringBuilder sb = new StringBuilder((int)Math.min(file.length(), 1024L * 1024L));
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"), 32768);
            try {
                char[] buf = new char[32768];
                int n;
                while ((n = br.read(buf)) > 0) sb.append(buf, 0, n);
            } finally {
                try { br.close(); } catch(Exception ignored) {}
            }
            JSONObject data = new JSONObject(sb.toString());
            return hasVisualPreloadedCategories(data) ? data : null;
        } catch(Exception ignored) {
            return null;
        }
    }

    private void writeVisualFigureDataDiskCache(JSONObject data) {
        if (data == null || !hasVisualPreloadedCategories(data)) return;
        try {
            File file = visualFigureDataDiskCacheFile();
            File tmp = new File(file.getParentFile(), VISUAL_FIGUREDATA_DISK_CACHE_FILE + ".tmp");
            FileOutputStream fos = new FileOutputStream(tmp, false);
            OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
            try {
                writer.write(data.toString());
                writer.flush();
                try { fos.getFD().sync(); } catch(Exception ignored) {}
            } finally {
                try { writer.close(); } catch(Exception ignored) {}
            }
            if (file.exists()) {
                try { file.delete(); } catch(Exception ignored) {}
            }
            if (!tmp.renameTo(file)) {
                FileInputStream in = new FileInputStream(tmp);
                FileOutputStream out = new FileOutputStream(file, false);
                try {
                    byte[] buf = new byte[32768];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                    out.flush();
                    try { out.getFD().sync(); } catch(Exception ignored) {}
                } finally {
                    try { in.close(); } catch(Exception ignored) {}
                    try { out.close(); } catch(Exception ignored) {}
                    try { tmp.delete(); } catch(Exception ignored) {}
                }
            }
            try { file.setLastModified(System.currentTimeMillis()); } catch(Exception ignored) {}
        } catch(Exception ignored) {}
    }

    private void loadVisualFigureData(final VisualDataCallback ok, final Runnable fail) {
        if (visualFigureDataCache != null && System.currentTimeMillis() - visualFigureDataLoadedAt < 86400000L && hasVisualPreloadedCategories(visualFigureDataCache)) {
            if (ok != null) ok.onLoaded(visualFigureDataCache);
            return;
        }

        final JSONObject data = visualFigureDataCache != null ? visualFigureDataCache : createVisualFigureDataShell();
        visualFigureDataCache = data;
        visualFigureDataLoadedAt = System.currentTimeMillis();

        // Mesmo usando a base pronta do site, não transforme o JSON inteiro em JSONObject no Android.
        // Isso é o que causava demora/travamento após limpar cache. Extraímos as categorias por streaming
        // diretamente de https://atoxic.com.br/cache/figuredata-ui.json, igual ao fluxo rápido da v14.
        loadAllVisualCategoriesInto(data, () -> {
            if (ok != null) ok.onLoaded(data);
        }, () -> {
            if (visualCategory(data, "hd") != null || countVisualLoadedCategories(data) > 0) {
                if (ok != null) ok.onLoaded(data);
            } else if (fail != null) {
                fail.run();
            }
        });
    }

    private boolean hasVisualPreloadedCategories(JSONObject data) {
        try {
            if (data == null) return false;
            for (String type : VISUAL_PRELOAD_TYPES) {
                if (visualCategory(data, type) == null) return false;
            }
            return true;
        } catch(Exception ignored) {
            return false;
        }
    }

    private int countVisualLoadedCategories(JSONObject data) {
        try {
            JSONObject cats = data == null ? null : data.optJSONObject("categories");
            return cats == null ? 0 : cats.length();
        } catch(Exception ignored) {
            return 0;
        }
    }

    private void loadAllVisualCategoriesInto(final JSONObject data, final Runnable ok, final Runnable fail) {
        executor.submit(() -> {
            int loaded = 0;
            int failed = 0;
            try {
                for (String type : VISUAL_PRELOAD_TYPES) {
                    if (type == null || type.trim().isEmpty()) continue;
                    if (visualCategory(data, type) != null) {
                        loaded++;
                        continue;
                    }
                    try {
                        JSONObject category = loadVisualCategoryFromServer(type);
                        if (category == null || category.optJSONArray("items") == null) throw new RuntimeException("invalid category");
                        synchronized (data) {
                            JSONObject cats = data.optJSONObject("categories");
                            if (cats == null) {
                                cats = new JSONObject();
                                data.put("categories", cats);
                            }
                            cats.put(type, category);
                        }
                        loaded++;
                    } catch(Exception e) {
                        failed++;
                    }
                }
                visualFigureDataCache = data;
                visualFigureDataLoadedAt = System.currentTimeMillis();
                final int finalLoaded = loaded;
                runOnUiThread(() -> {
                    if (finalLoaded > 0) {
                        if (ok != null) ok.run();
                    } else {
                        if (fail != null) fail.run();
                    }
                });
            } catch(Exception e) {
                runOnUiThread(() -> {
                    if (countVisualLoadedCategories(data) > 0) {
                        if (ok != null) ok.run();
                    } else {
                        if (fail != null) fail.run();
                    }
                });
            }
        });
    }

    private JSONObject createVisualFigureDataShell() {
        JSONObject data = new JSONObject();
        try {
            data.put("ok", true);
            data.put("categories", new JSONObject());
        } catch(Exception ignored) {}
        return data;
    }

    private void loadVisualCategoryDataInto(final JSONObject data, final String rawType, final Runnable ok, final Runnable fail) {
        final String type = rawType == null ? "" : rawType.trim();
        if (type.isEmpty()) {
            if (fail != null) runOnUiThread(fail);
            return;
        }

        try {
            if (visualCategory(data, type) != null) {
                if (ok != null) runOnUiThread(ok);
                return;
            }
        } catch(Exception ignored) {}

        final String loadingKey = currentHotelKey + ":" + type;
        if (!visualCategoryLoading.add(loadingKey)) {
            waitForVisualCategoryLoaded(data, type, loadingKey, ok, fail, System.currentTimeMillis());
            return;
        }

        executor.submit(() -> {
            try {
                JSONObject category = loadVisualCategoryFromServer(type);
                if (category == null || category.optJSONArray("items") == null) throw new RuntimeException("invalid category");
                synchronized (data) {
                    JSONObject cats = data.optJSONObject("categories");
                    if (cats == null) {
                        cats = new JSONObject();
                        data.put("categories", cats);
                    }
                    cats.put(type, category);
                }
                visualFigureDataCache = data;
                visualFigureDataLoadedAt = System.currentTimeMillis();
                runOnUiThread(() -> { if (ok != null) ok.run(); });
            } catch(Exception e) {
                runOnUiThread(() -> { if (fail != null) fail.run(); });
            } finally {
                visualCategoryLoading.remove(loadingKey);
            }
        });
    }

    private void waitForVisualCategoryLoaded(final JSONObject data, final String type, final String loadingKey, final Runnable ok, final Runnable fail, final long startedAt) {
        uiHandler.postDelayed(() -> {
            try {
                if (visualCategory(data, type) != null) {
                    if (ok != null) ok.run();
                    return;
                }
            } catch(Exception ignored) {}

            boolean stillLoading = visualCategoryLoading.contains(loadingKey);
            if (!stillLoading || System.currentTimeMillis() - startedAt > 18000L) {
                if (fail != null) fail.run();
                return;
            }
            waitForVisualCategoryLoaded(data, type, loadingKey, ok, fail, startedAt);
        }, 300L);
    }

    private JSONObject loadVisualCategoryFromServer(String type) throws Exception {
        String encType = URLEncoder.encode(type, "UTF-8");

        // Primeiro tenta endpoints pequenos/específicos. Se o servidor ignorar o parâmetro
        // e devolver o figuredata inteiro, o leitor limitado interrompe antes de pesar a memória.
        String[] directUrls = new String[] {
            VISUAL_FIGUREDATA_URL + "&category=" + encType,
            VISUAL_FIGUREDATA_URL + "&type=" + encType
        };

        for (String url : directUrls) {
            try {
                String body = getTextLimited(url, 10000, 16000, 4_000_000);
                JSONObject cat = parseVisualCategoryResponse(body, type);
                if (cat != null && cat.optJSONArray("items") != null) return cat;
            } catch(Exception ignored) {}
        }

        // Depois usa exatamente a base que o site /visuais usa, mas extraindo por streaming.
        // Isso evita transformar um JSON de dezenas de MB em JSONObject no Samsung A10s.
        String[] fullUrls = new String[] {
            VISUAL_FIGUREDATA_CACHE_URL,
            VISUAL_FIGUREDATA_URL
        };

        for (String url : fullUrls) {
            try {
                String categoryText = getVisualCategoryObjectTextFromUrl(url, type, 10000, isLowMemoryVisualDevice() ? 26000 : 42000);
                if (categoryText != null && !categoryText.trim().isEmpty()) {
                    JSONObject category = new JSONObject(categoryText);
                    if (category.optJSONArray("items") != null) return category;
                }
            } catch(Exception ignored) {}
        }

        throw new IOException("visual category not found: " + type);
    }

    private JSONObject parseVisualCategoryResponse(String body, String type) throws Exception {
        if (body == null || body.trim().isEmpty()) return null;
        String clean = body.trim();
        JSONObject parsed = new JSONObject(clean);
        JSONObject cat = visualCategory(parsed, type);
        if (cat != null && cat.optJSONArray("items") != null) return cat;
        JSONObject data = parsed.optJSONObject("data");
        if (data != null) {
            cat = visualCategory(data, type);
            if (cat != null && cat.optJSONArray("items") != null) return cat;
            if (data.optJSONArray("items") != null) return data;
        }
        if (parsed.optJSONArray("items") != null) return parsed;
        return null;
    }

    private String getTextLimited(String u, int connectTimeoutMs, int readTimeoutMs, int maxBytes) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setConnectTimeout(connectTimeoutMs);
        c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        if (code < 200 || code >= 300 || is == null) throw new IOException("HTTP " + code);
        ByteArrayOutputStream out = new ByteArrayOutputStream(Math.min(maxBytes, 262144));
        byte[] buf = new byte[8192];
        int n;
        int total = 0;
        while ((n = is.read(buf)) > 0) {
            total += n;
            if (total > maxBytes) throw new IOException("visual response too large");
            out.write(buf, 0, n);
        }
        String body = out.toString("UTF-8");
        if (body.trim().isEmpty()) throw new IOException("empty visual response");
        return body;
    }

    private String getVisualCategoryObjectTextFromUrl(String u, String categoryKey, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setConnectTimeout(connectTimeoutMs);
        c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        if (code < 200 || code >= 300 || is == null) throw new IOException("HTTP " + code);
        try {
            return extractVisualCategoryObjectTextStreaming(is, categoryKey);
        } finally {
            try { is.close(); } catch(Exception ignored) {}
        }
    }

    private String extractVisualCategoryObjectTextStreaming(InputStream is, String categoryKey) throws Exception {
        if (is == null || categoryKey == null || categoryKey.trim().isEmpty()) return null;
        final String categoriesPattern = "\"categories\"";
        final String categoryPattern = "\"" + categoryKey + "\"";
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 32768);
        char[] buf = new char[8192];
        int phase = 0; // 0=procura categories, 1=procura key, 2=apos key, 3=apos dois-pontos, 4=captura objeto
        int match = 0;
        int depth = 0;
        boolean inString = false;
        boolean esc = false;
        StringBuilder out = null;
        int n;
        while ((n = reader.read(buf)) > 0) {
            for (int i = 0; i < n; i++) {
                char ch = buf[i];
                if (phase == 0) {
                    if (ch == categoriesPattern.charAt(match)) {
                        match++;
                        if (match == categoriesPattern.length()) {
                            phase = 1;
                            match = 0;
                        }
                    } else {
                        match = (ch == categoriesPattern.charAt(0)) ? 1 : 0;
                    }
                    continue;
                }

                if (phase == 1) {
                    if (ch == categoryPattern.charAt(match)) {
                        match++;
                        if (match == categoryPattern.length()) {
                            phase = 2;
                            match = 0;
                        }
                    } else {
                        match = (ch == categoryPattern.charAt(0)) ? 1 : 0;
                    }
                    continue;
                }

                if (phase == 2) {
                    if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') continue;
                    if (ch == ':') {
                        phase = 3;
                    } else {
                        phase = 1;
                        match = 0;
                    }
                    continue;
                }

                if (phase == 3) {
                    if (ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t') continue;
                    if (ch == '{') {
                        phase = 4;
                        out = new StringBuilder(262144);
                        out.append(ch);
                        depth = 1;
                        inString = false;
                        esc = false;
                    } else {
                        phase = 1;
                        match = 0;
                    }
                    continue;
                }

                if (phase == 4) {
                    out.append(ch);
                    if (inString) {
                        if (esc) { esc = false; continue; }
                        if (ch == '\\') { esc = true; continue; }
                        if (ch == '"') inString = false;
                        continue;
                    }
                    if (ch == '"') { inString = true; continue; }
                    if (ch == '{') depth++;
                    else if (ch == '}') {
                        depth--;
                        if (depth == 0) return out.toString();
                    }
                }
            }
        }
        return null;
    }

    private String getText(String u, int connectTimeoutMs, int readTimeoutMs) throws Exception {
        HttpURLConnection c = (HttpURLConnection)new URL(u).openConnection();
        c.setConnectTimeout(connectTimeoutMs);
        c.setReadTimeout(readTimeoutMs);
        c.setRequestProperty("Accept", "application/json, text/plain, */*");
        c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
        int code = c.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        String body = readAll(is);
        if (code < 200 || code >= 300 || body == null || body.trim().isEmpty()) throw new IOException("HTTP " + code);
        return body;
    }

    private String extractVisualCategoryObjectText(String json, String categoryKey) {
        if (json == null || categoryKey == null || categoryKey.trim().isEmpty()) return null;
        int catKey = json.indexOf("\"categories\"");
        if (catKey < 0) return null;
        int catObjStart = json.indexOf('{', catKey);
        if (catObjStart < 0) return null;

        int i = catObjStart + 1;
        int len = json.length();
        while (i < len) {
            i = skipJsonWhitespaceAndCommas(json, i);
            if (i >= len || json.charAt(i) == '}') return null;
            if (json.charAt(i) != '"') { i++; continue; }
            int keyEnd = findJsonStringEnd(json, i);
            if (keyEnd < 0) return null;
            String key = json.substring(i + 1, keyEnd);
            i = skipJsonWhitespace(json, keyEnd + 1);
            if (i >= len || json.charAt(i) != ':') return null;
            i = skipJsonWhitespace(json, i + 1);
            if (i >= len) return null;

            if (categoryKey.equals(key) && json.charAt(i) == '{') {
                int end = findJsonBalancedEnd(json, i);
                if (end > i) return json.substring(i, end + 1);
                return null;
            }

            int valueEnd = skipJsonValue(json, i);
            if (valueEnd <= i) i++; else i = valueEnd;
        }
        return null;
    }

    private int skipJsonWhitespaceAndCommas(String s, int i) {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ',' || c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
            else break;
        }
        return i;
    }

    private int skipJsonWhitespace(String s, int i) {
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') i++;
            else break;
        }
        return i;
    }

    private int findJsonStringEnd(String s, int quoteStart) {
        boolean esc = false;
        for (int i = quoteStart + 1; i < s.length(); i++) {
            char c = s.charAt(i);
            if (esc) { esc = false; continue; }
            if (c == '\\') { esc = true; continue; }
            if (c == '"') return i;
        }
        return -1;
    }

    private int findJsonBalancedEnd(String s, int start) {
        char open = s.charAt(start);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;
        boolean esc = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inString) {
                if (esc) { esc = false; continue; }
                if (c == '\\') { esc = true; continue; }
                if (c == '"') inString = false;
                continue;
            }
            if (c == '"') { inString = true; continue; }
            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }

    private int skipJsonValue(String s, int start) {
        if (start >= s.length()) return start;
        char c = s.charAt(start);
        if (c == '{' || c == '[') {
            int end = findJsonBalancedEnd(s, start);
            return end < 0 ? s.length() : end + 1;
        }
        if (c == '"') {
            int end = findJsonStringEnd(s, start);
            return end < 0 ? s.length() : end + 1;
        }
        int i = start;
        while (i < s.length()) {
            c = s.charAt(i);
            if (c == ',' || c == '}') break;
            i++;
        }
        return i;
    }

    private interface VisualDataCallback {
        void onLoaded(JSONObject data);
    }

    private void renderVisualCategories(LinearLayout tabs, String[] currentType, String[] currentGender, String[] currentFigure, JSONObject data, Runnable refresh) {
        if (tabs == null) return;
        tabs.removeAllViews();

        HorizontalScrollView mainScroll = new HorizontalScrollView(this);
        mainScroll.setHorizontalScrollBarEnabled(false);
        mainScroll.setFillViewport(true);
        LinearLayout mainRow = new LinearLayout(this);
        mainRow.setOrientation(LinearLayout.HORIZONTAL);
        mainRow.setGravity(Gravity.CENTER);
        mainScroll.addView(mainRow, new HorizontalScrollView.LayoutParams(-1, dp(50)));
        tabs.addView(mainScroll, new LinearLayout.LayoutParams(-1, dp(52)));

        HorizontalScrollView subScroll = new HorizontalScrollView(this);
        subScroll.setHorizontalScrollBarEnabled(false);
        subScroll.setFillViewport(true);
        LinearLayout subRow = new LinearLayout(this);
        subRow.setOrientation(LinearLayout.HORIZONTAL);
        subRow.setGravity(Gravity.CENTER);
        subRow.setPadding(0, 0, 0, 0);
        subScroll.addView(subRow, new HorizontalScrollView.LayoutParams(-1, dp(50)));
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, dp(52));
        subLp.topMargin = dp(6);
        tabs.addView(subScroll, subLp);

        VisualGroup[] groups = visualCategoryGroups(data);
        String activeGroup = visualActiveGroup(currentType[0], groups);

        for (VisualGroup group : groups) {
            View item = visualIconTab(group.icon, group.id.equals(activeGroup), dp(48));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(55), dp(48));
            lp.rightMargin = dp(7);
            mainRow.addView(item, lp);
            item.setOnClickListener(v -> {
                String first = group.types.length > 0 ? group.types[0] : "hd";
                currentType[0] = first;
                if (refresh != null) refresh.run();
            });
        }

        VisualGroup active = visualFindGroup(activeGroup, groups);
        if (active == null) return;

        if (active.genderTabs) {
            View male = visualIconTab("https://lite.habbonews.net/ferramentas/visuais/male.png", "M".equals(currentGender[0]), dp(48));
            male.setAlpha("M".equals(currentGender[0]) ? 1f : 0.50f);
            LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(dp(55), dp(48));
            mlp.rightMargin = dp(7);
            subRow.addView(male, mlp);
            male.setOnClickListener(v -> {
                if (!"M".equals(currentGender[0])) {
                    currentGender[0] = "M";
                    currentType[0] = "hd";
                    currentFigure[0] = setFigurePart(currentFigure[0], "hd", figurePart(DEFAULT_VISUAL_FIGURE_MALE, "hd"));
                }
                if (refresh != null) refresh.run();
            });

            View female = visualIconTab("https://lite.habbonews.net/ferramentas/visuais/female.png", "F".equals(currentGender[0]), dp(48));
            female.setAlpha("F".equals(currentGender[0]) ? 1f : 0.50f);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(dp(55), dp(48));
            flp.rightMargin = dp(7);
            subRow.addView(female, flp);
            female.setOnClickListener(v -> {
                if (!"F".equals(currentGender[0])) {
                    currentGender[0] = "F";
                    currentType[0] = "hd";
                    currentFigure[0] = setFigurePart(currentFigure[0], "hd", figurePart(DEFAULT_VISUAL_FIGURE_FEMALE, "hd"));
                }
                if (refresh != null) refresh.run();
            });
        } else {
            for (String type : active.types) {
                View item = visualIconTab(categoryIconUrl(type, true), type.equals(currentType[0]), dp(48));
                item.setAlpha(type.equals(currentType[0]) ? 1f : 0.50f);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(55), dp(48));
                lp.rightMargin = dp(7);
                subRow.addView(item, lp);
                item.setOnClickListener(v -> {
                    currentType[0] = type;
                    if (refresh != null) refresh.run();
                });
            }
        }
    }

    private void renderVisualItems(LinearLayout area, LinearLayout colors, String[] currentFigure, String[] gender, String[] currentType, JSONObject data, Runnable updatePreview) {
        if (area == null) return;
        if (colors != null) colors.removeAllViews();

        String uiType = currentType[0];
        String itemType = getVisualItemTypeForUiCategory(uiType);
        JSONObject category = visualCategory(data, itemType);
        if (category == null) {
            if (colors != null) colors.setVisibility(View.GONE);
            area.removeAllViews();
            area.addView(visualFigureDataLoadingView(), new LinearLayout.LayoutParams(-1, dp(300)));
            loadVisualCategoryDataInto(data, itemType, () -> renderVisualItems(area, colors, currentFigure, gender, currentType, data, updatePreview), () -> {
                area.removeAllViews();
                area.addView(centerNote(t("cannot_load_visuals")));
            });
            return;
        }

        JSONArray items = category.optJSONArray("items");
        if (items == null || items.length() == 0) {
            if (colors != null) colors.setVisibility(View.GONE);
            area.removeAllViews();
            area.addView(centerNote(t("no_items_found")));
            return;
        }

        if (colors != null) colors.setVisibility(View.VISIBLE);
        area.removeAllViews();

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(0, 0, 0, 0);
        area.addView(container, new LinearLayout.LayoutParams(-1, -2));

        int perRow = 5;
        LinearLayout row = null;
        int shown = 0;
        int totalEligible = 0;
        String currentId = figurePartId(currentFigure[0], itemType);
        int cellSize = dp(54);
        int rowHeight = dp(62);
        int gap = dp(8);

        // Depois que o carregamento passou a usar o mesmo modelo do site, não limitamos mais
        // os itens por aparelho. Todas as peças elegíveis da categoria são exibidas de uma vez.
        int renderLimit = Integer.MAX_VALUE / 4;

        if (isVisualRemovableType(uiType)) {
            View remove = visualItemCell("", itemType, "0", currentFigure[0], true, currentId.isEmpty());
            remove.setOnClickListener(v -> {
                currentFigure[0] = removeFigurePart(currentFigure[0], itemType);
                if (updatePreview != null) updatePreview.run();
                renderVisualItems(area, colors, currentFigure, gender, currentType, data, updatePreview);
            });
            row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            container.addView(row, lp(-1, rowHeight, 0, dp(8), 0, 4));
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(cellSize, cellSize);
            rp.rightMargin = gap;
            row.addView(remove, rp);
            shown = 1;
        }

        for (int i=0; i<items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null || !item.optBoolean("selectable", true)) continue;
            String g = firstText(item, "gender", "sex");
            if (!visualGenderMatches(g, gender[0])) continue;
            totalEligible++;
            if (totalEligible > renderLimit) continue;

            if (shown % perRow == 0 || row == null) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER);
                container.addView(row, lp(-1, rowHeight, 0, shown == 0 ? dp(8) : 0, 0, 4));
            }
            final JSONObject finalItem = item;
            final String itemId = firstText(item, "id");
            String previewFigure = applyFigureItem(visualPreviewBaseFigure(gender[0], itemType), itemType, item, null);
            View cell = visualItemCell("", itemType, itemId, previewFigure, false, itemId.equals(currentId));
            cell.setOnClickListener(v -> {
                currentFigure[0] = applyFigureItem(currentFigure[0], itemType, finalItem, null);
                if ("hd".equals(itemType)) {
                    gender[0] = normalizeVisualGender(firstText(finalItem, "gender", "sex"), gender[0]);
                }
                markVisualItemSelected(container, cell);
                if (updatePreview != null) updatePreview.run();
                renderVisualColors(colors, currentFigure, uiType, finalItem, updatePreview, null);
            });
            attachVisualItemLongPress(cell, itemType, itemId, previewFigure);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(cellSize, cellSize);
            cp.rightMargin = gap;
            row.addView(cell, cp);
            shown++;
        }


        JSONObject selected = findVisualItemByFigure(category, currentFigure[0], itemType);
        if (selected != null) renderVisualColors(colors, currentFigure, uiType, selected, updatePreview, null);
    }

    private boolean isLowMemoryVisualDevice() {
        try {
            ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            if (am != null) {
                ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                am.getMemoryInfo(mi);
                // Limitações/paginação do Provador somente para celulares com 3 GB de RAM ou menos.
                // Uso uma margem pequena porque alguns aparelhos reportam um pouco abaixo/acima do valor nominal.
                if (Build.VERSION.SDK_INT >= 16 && mi.totalMem > 0) {
                    return mi.totalMem <= (long)(3.25d * 1024d * 1024d * 1024d);
                }
                if (Build.VERSION.SDK_INT >= 19 && am.isLowRamDevice()) return true;
                if (am.getMemoryClass() > 0 && am.getMemoryClass() <= 192) return true;
            }
        } catch(Exception ignored) {}
        return false;
    }

    private void syncVisualItemSelection(View root, String currentId, String currentFigure, String itemType) {
        if (root == null) return;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) syncVisualItemSelection(group.getChildAt(i), currentId, currentFigure, itemType);
        }
        if (root instanceof FrameLayout && root.getTag() instanceof String) {
            String tag = (String)root.getTag();
            if (tag.startsWith("visual_item_cell:" + itemType + ":")) {
                String id = tag.substring(("visual_item_cell:" + itemType + ":").length());
                boolean selected = (currentId == null ? "" : currentId).equals(id);
                applyVisualItemCellStyle(root, selected);
            }
        }
    }

    private void markVisualItemSelected(View root, View selectedCell) {
        if (root == null) return;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) markVisualItemSelected(group.getChildAt(i), selectedCell);
        }
        if (root instanceof FrameLayout && root.getTag() instanceof String && ((String)root.getTag()).startsWith("visual_item_cell:")) {
            applyVisualItemCellStyle(root, root == selectedCell);
        }
    }

    private void applyVisualItemCellStyle(View cell, boolean selected) {
        if (cell == null) return;
        int selectedStroke = Color.rgb(188, 74, 255);
        int normalStroke = Color.argb(lightTheme ? 24 : 20, 255, 255, 255);
        int fill = selected ? Color.argb(lightTheme ? 72 : 70, 168, 76, 255) : Color.argb(lightTheme ? 18 : 26, 255, 255, 255);
        cell.setBackground(round(fill, dp(12), selected ? selectedStroke : normalStroke, selected ? 2 : 1));
        if (Build.VERSION.SDK_INT >= 21) cell.setElevation(selected ? dp(4) : 0);
    }

    private void attachVisualItemLongPress(final View cell, final String type, final String itemId, final String previewFigure) {
        if (cell == null) return;
        final boolean[] triggered = {false};
        final float[] downX = {0f};
        final float[] downY = {0f};
        final Runnable[] pending = new Runnable[1];

        cell.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                triggered[0] = false;
                downX[0] = event.getX();
                downY[0] = event.getY();
                pending[0] = () -> {
                    triggered[0] = true;
                    try { v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS); } catch(Exception ignored) {}
                    showVisualItemInfoDialog(type, itemId, previewFigure);
                };
                uiHandler.postDelayed(pending[0], 500L);
                return false;
            }

            if (action == MotionEvent.ACTION_MOVE) {
                float dx = Math.abs(event.getX() - downX[0]);
                float dy = Math.abs(event.getY() - downY[0]);
                if (dx > dp(12) || dy > dp(12)) {
                    if (pending[0] != null) uiHandler.removeCallbacks(pending[0]);
                }
                return triggered[0];
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                if (pending[0] != null) uiHandler.removeCallbacks(pending[0]);
                return triggered[0];
            }

            return false;
        });
    }

    private void showVisualItemInfoDialog(final String type, final String itemId, final String previewFigure) {
        final Dialog dialog = new Dialog(this);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        tintScrollBar(scroll);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(18), dp(18), dp(18));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        scroll.addView(wrap, new ScrollView.LayoutParams(-1, -2));
        dialog.setContentView(scroll);

        ImageView avatarImage = new ImageView(this);
        avatarImage.setAdjustViewBounds(true);
        avatarImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatarImage.setBackground(round(lightTheme ? Color.rgb(248,248,248) : Color.argb(22,255,255,255), dp(18), lightTheme ? Color.rgb(220,220,220) : Color.argb(35,255,255,255), 1));
        wrap.addView(avatarImage, lp(-1, dp(150), 0, 0, 0, 12));
        Glide.with(MainActivity.this).load(avatarFull(previewFigure, 2)).diskCacheStrategy(DiskCacheStrategy.NONE).placeholder(R.drawable.pre_load).error(R.drawable.pre_load).into(avatarImage);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(info, lp(-1, -2, 0, 0, 0, 12));

        info.addView(visualPurpleLoader(""), lp(-1, dp(84), 0, 4, 0, 4));

        TextView close = dialogButton(t("close"));
        wrap.addView(close, lp(-1, dp(46), 0, 4, 0, 0));
        close.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = (int)(getResources().getDisplayMetrics().widthPixels * 0.90f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }

        executor.execute(() -> {
            JSONObject found = null;
            try {
                JSONObject payload = unwrap(getJson(habbodexFigureUrl(previewFigure)));
                ArrayList<JSONObject> clothes = normalizeClothingEntries(payload);
                for (JSONObject o : clothes) {
                    String slot = firstText(o, "_slot", "type", "partType", "category");
                    if (type.equals(slot)) { found = o; break; }
                }
                if (found == null && !clothes.isEmpty()) found = clothes.get(0);
            } catch(Exception ignored) {}

            final JSONObject itemInfo = found;
            runOnUiThread(() -> {
                info.removeAllViews();

                String code = firstText(itemInfo, "code", "classname", "className", "id");
                String name = clothingName(itemInfo, code.isEmpty() ? (type + "-" + itemId) : code);
                String collection = clothingLineName(itemInfo, "");
                String icon = firstText(itemInfo, "iconUrl", "imageUrl", "url", "thumbnail");
                if (icon.isEmpty() && !code.isEmpty()) icon = "https://habbodex.com/images/furni/" + enc(code) + "/" + enc(code) + "_icon.png";

                info.addView(visualItemInfoRow(t("item_name"), name.isEmpty() ? (type + "-" + itemId) : name));
                info.addView(visualItemInfoRow(t("collection"), collection.isEmpty() ? "-" : collection));
                info.addView(visualItemThumbnailBlock(t("thumbnail"), icon));
            });
        });
    }

    private LinearLayout visualItemThumbnailBlock(String label, String icon) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(14), lightTheme ? Color.rgb(222,222,222) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 8));

        TextView l = text(label, 12, themeMutedColor(), true);
        l.setGravity(Gravity.LEFT);
        row.addView(l, lp(-1, -2, 0, 0, 0, 8));

        ImageView img = new ImageView(this);
        img.setAdjustViewBounds(true);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setBackground(round(lightTheme ? Color.rgb(244,244,244) : Color.argb(18,255,255,255), dp(12), Color.TRANSPARENT, 0));
        row.addView(img, lp(-1, dp(54), 0, 0, 0, 0));
        if (icon != null && !icon.isEmpty()) Glide.with(MainActivity.this).load(icon).into(img);
        return row;
    }

    private LinearLayout visualItemInfoRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(12), dp(9), dp(12), dp(9));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(22,255,255,255), dp(14), lightTheme ? Color.rgb(222,222,222) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, -2, 0, 0, 0, 8));

        TextView l = text(label, 12, themeMutedColor(), true);
        l.setGravity(Gravity.LEFT);
        row.addView(l, lp(-1, -2, 0, 0, 0, 2));

        TextView v = text(value == null ? "" : value, 14, lightTheme ? Color.rgb(35,35,35) : Color.WHITE, false);
        v.setGravity(Gravity.LEFT);
        v.setMaxLines(3);
        v.setEllipsize(TextUtils.TruncateAt.END);
        row.addView(v, lp(-1, -2, 0, 0, 0, 0));
        return row;
    }

    private View visualItemCell(String label, String type, String id, String figure, boolean remove, boolean selected) {
        FrameLayout outer = new FrameLayout(this);
        outer.setPadding(dp(4), dp(4), dp(4), dp(4));
        outer.setTag("visual_item_cell:" + type + ":" + id);
        applyVisualItemCellStyle(outer, selected);

        FrameLayout box = new FrameLayout(this);
        box.setClipChildren(true);
        box.setClipToPadding(true);
        box.setBackground(round(Color.TRANSPARENT, dp(10), Color.TRANSPARENT, 0));
        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(dp(46), dp(46), Gravity.CENTER);
        outer.addView(box, bp);

        ImageView img = new ImageView(this);
        img.setAdjustViewBounds(false);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setPadding(0, 0, 0, 0);
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(dp(46), dp(72), Gravity.CENTER);
        box.addView(img, ip);

        if (remove) {
            Glide.with(MainActivity.this).load("https://lite.habbonews.net/ferramentas/visuais/removable.png").into(img);
            img.setScaleX(0.60f);
            img.setScaleY(0.60f);
        } else if (figure != null && !figure.isEmpty()) {
            img.setScaleX(visualItemScale(type));
            img.setScaleY(visualItemScale(type));
            img.setTranslationY(dp(visualItemOffsetDp(type)));
            Glide.with(MainActivity.this).load(avatarFull(figure, 2)).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).placeholder(R.drawable.pre_load).error(R.drawable.pre_load).into(img);
        }

        return outer;
    }

    private void renderVisualColors(LinearLayout colors, String[] currentFigure, String type, JSONObject item, Runnable updatePreview, Runnable refreshItems) {
        if (colors == null || item == null) return;
        colors.removeAllViews();

        JSONArray arr = item.optJSONArray("colors");
        final String itemType = getVisualItemTypeForUiCategory(type);
        if (!item.optBoolean("colorable", false) || arr == null || arr.length() == 0) {
            renderDisabledColorPlaceholder(colors);
            return;
        }

        colors.setAlpha(0.88f);
        colors.setEnabled(true);
        bindColorPanelTouchLock(colors);

        int count = Math.max(1, Math.min(2, item.optInt("colorCount", 1)));
        ArrayList<String> activeColors = figurePartColors(currentFigure[0], itemType);

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        horizontal.setFillViewport(true);
        final float[] colorAreaLastY = {0f};
        horizontal.setOnTouchListener((v, event) -> {
            ViewParent parent = v.getParent();
            while (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
                if (!(parent instanceof View)) break;
                parent = ((View) parent).getParent();
            }

            ScrollView target = findColorScrollTarget(horizontal, event.getX());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                colorAreaLastY[0] = event.getY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float y = event.getY();
                int dy = (int)(colorAreaLastY[0] - y);
                colorAreaLastY[0] = y;
                if (target != null) target.scrollBy(0, dy);
                return true;
            }
            return true;
        });
        LinearLayout columns = new LinearLayout(this);
        columns.setOrientation(LinearLayout.HORIZONTAL);
        columns.setGravity(Gravity.CENTER);
        horizontal.addView(columns, new HorizontalScrollView.LayoutParams(-1, -2));
        colors.addView(horizontal, lp(-1, dp(122), 0, 0, 0, 0));

        for (int slot=0; slot<count; slot++) {
            final int colorSlot = slot;
            ScrollView vertical = new ScrollView(this);
            vertical.setVerticalScrollBarEnabled(true);
            vertical.setScrollbarFadingEnabled(false);
            tintScrollBar(vertical);
            bindNestedScrollTouch(vertical);

            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setGravity(Gravity.LEFT);
            column.setPadding(0, 0, 0, 0);
            vertical.addView(column, new ScrollView.LayoutParams(-1, -2));

            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(count == 1 ? dp(304) : dp(156), dp(116));
            if (slot > 0) colLp.leftMargin = dp(10);
            columns.addView(vertical, colLp);

            int perRow = count == 1 ? 13 : 6;
            LinearLayout row = null;
            int shown = 0;
            String activeColor = activeColors.size() > slot ? activeColors.get(slot) : "";

            for (int i=0; i<arr.length(); i++) {
                JSONObject c = arr.optJSONObject(i);
                if (c == null || !c.optBoolean("selectable", true)) continue;
                if (shown % perRow == 0 || row == null) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setGravity(Gravity.LEFT);
                    column.addView(row, lp(-2, dp(25), 0, 0, 0, 1));
                }
                String colorId = firstText(c, "id");
                String hex = firstText(c, "hex");
                boolean club = c.optBoolean("isClub", false) || c.optBoolean("club", false) || "1".equals(firstText(c, "club")) || "2".equals(firstText(c, "club"));
                View sw = visualColorCell(hex, club, colorId.equals(activeColor));
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(20), dp(20));
                cp.rightMargin = dp(3);
                cp.topMargin = dp(2);
                row.addView(sw, cp);
                final float[] colorCellLastY = {0f};
                final boolean[] colorCellMoved = {false};
                sw.setOnTouchListener((view, event) -> {
                    ViewParent parent = view.getParent();
                    while (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                        if (!(parent instanceof View)) break;
                        parent = ((View) parent).getParent();
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        colorCellLastY[0] = event.getRawY();
                        colorCellMoved[0] = false;
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float y = event.getRawY();
                        int dy = (int)(colorCellLastY[0] - y);
                        if (Math.abs(dy) > dp(1)) colorCellMoved[0] = true;
                        colorCellLastY[0] = y;
                        vertical.scrollBy(0, dy);
                        return true;
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (!colorCellMoved[0]) {
                            currentFigure[0] = applyFigureItemColorSlot(currentFigure[0], itemType, item, colorId, colorSlot);
                            markVisualColorSelected(vertical, view);
                            if (updatePreview != null) updatePreview.run();
                        }
                        return true;
                    }
                    return true;
                });
                shown++;
            }
        }
    }

    private String normalizeVisualHex(String hex) {
        String h = hex == null ? "ffffff" : hex.replace("#", "").trim();
        if (!h.matches("(?i)[0-9a-f]{6}")) return "ffffff";
        return h.toLowerCase(Locale.ROOT);
    }

    private void markVisualColorSelected(View root, View selectedCell) {
        if (root == null) return;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) markVisualColorSelected(group.getChildAt(i), selectedCell);
        }
        if (root instanceof FrameLayout && root.getTag() instanceof String && ((String)root.getTag()).startsWith("visual_color:")) {
            applyVisualColorCellStyle((FrameLayout)root, root == selectedCell);
        }
    }

    private void applyVisualColorCellStyle(FrameLayout box, boolean active) {
        if (box == null) return;
        Object raw = box.getTag();
        String hex = "ffffff";
        if (raw instanceof String) {
            String s = (String)raw;
            int idx = s.indexOf(':');
            if (idx >= 0 && idx + 1 < s.length()) hex = s.substring(idx + 1);
        }
        int fill = colorFromHex(hex);
        int stroke = lightenColorForVisualBorder(fill, active ? 0.72f : 0.44f);
        box.setAlpha(1f);
        box.setBackground(round(fill, dp(5), stroke, 2));
        box.setTranslationY(active ? -dp(2) : 0);
        if (Build.VERSION.SDK_INT >= 21) box.setElevation(active ? dp(5) : 0);
    }

    private void restoreVisualColorScroll(LinearLayout colors, int slot, int scrollX, int scrollY) {
        if (colors == null) return;
        colors.postDelayed(() -> {
            try {
                if (colors.getChildCount() == 0) return;
                View h = colors.getChildAt(0);
                if (h instanceof HorizontalScrollView) {
                    ((HorizontalScrollView) h).setScrollX(scrollX);
                    if (((HorizontalScrollView) h).getChildCount() == 0) return;
                    View colsView = ((HorizontalScrollView) h).getChildAt(0);
                    if (colsView instanceof LinearLayout) {
                        LinearLayout cols = (LinearLayout) colsView;
                        if (slot >= 0 && slot < cols.getChildCount()) {
                            View v = cols.getChildAt(slot);
                            if (v instanceof ScrollView) ((ScrollView) v).setScrollY(scrollY);
                        }
                    }
                }
            } catch(Exception ignored) {}
        }, 60L);
    }

    private ScrollView findColorScrollTarget(HorizontalScrollView horizontal, float x) {
        try {
            if (horizontal == null || horizontal.getChildCount() == 0) return null;
            View child = horizontal.getChildAt(0);
            if (!(child instanceof LinearLayout)) return null;
            LinearLayout columns = (LinearLayout) child;
            if (columns.getChildCount() == 0) return null;

            float absoluteX = x + horizontal.getScrollX();
            for (int i = 0; i < columns.getChildCount(); i++) {
                View v = columns.getChildAt(i);
                if (v instanceof ScrollView && absoluteX >= v.getLeft() && absoluteX <= v.getRight()) {
                    return (ScrollView) v;
                }
            }
            for (int i = 0; i < columns.getChildCount(); i++) {
                View v = columns.getChildAt(i);
                if (v instanceof ScrollView) return (ScrollView) v;
            }
        } catch(Exception ignored) {}
        return null;
    }

    private void renderDisabledColorPlaceholder(LinearLayout colors) {
        if (colors == null) return;
        colors.setAlpha(0.24f);
        colors.setEnabled(false);

        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        colors.addView(column, lp(-1, dp(92), 0, 0, 0, 0));

        for (int r=0; r<3; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER);
            column.addView(row, lp(-1, dp(25), 0, 0, 0, 1));
            for (int c=0; c<13; c++) {
                View sw = visualColorCell("8b34d9", false, false);
                sw.setAlpha(0.55f);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(20), dp(20));
                cp.rightMargin = dp(3);
                cp.topMargin = dp(2);
                row.addView(sw, cp);
            }
        }
    }

    private View visualColorCell(String hex, boolean club, boolean active) {
        FrameLayout box = new FrameLayout(this);
        box.setTag("visual_color:" + normalizeVisualHex(hex));
        applyVisualColorCellStyle(box, active);
        if (club) {
            ImageView hc = new ImageView(this);
            hc.setImageResource(R.drawable.hcmini);
            hc.setAdjustViewBounds(true);
            hc.setScaleType(ImageView.ScaleType.FIT_CENTER);
            FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(dp(20), dp(10), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            hp.bottomMargin = 0;
            box.addView(hc, hp);
        }
        return box;
    }

    private JSONObject visualCategory(JSONObject data, String type) {
        if (data == null) return null;
        JSONObject cats = data.optJSONObject("categories");
        if (cats == null) return null;
        return cats.optJSONObject(type);
    }

    private JSONObject findVisualItemByFigure(JSONObject category, String figure, String type) {
        String id = figurePartId(figure, type);
        if (id.isEmpty()) return null;
        JSONArray items = category == null ? null : category.optJSONArray("items");
        if (items == null) return null;
        for (int i=0; i<items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item != null && id.equals(firstText(item, "id"))) return item;
        }
        return null;
    }

    private String applyFigureItem(String figure, String type, JSONObject item, String forcedColor) {
        if (item == null) return figure;
        String id = firstText(item, "id");
        if (id.isEmpty()) return figure;
        String old = figurePart(figure, type);
        ArrayList<String> colors = new ArrayList<>();
        if (old != null && !old.isEmpty()) {
            String[] bits = old.split("-");
            for (int i=2; i<bits.length; i++) if (!bits[i].trim().isEmpty()) colors.add(bits[i].trim());
        }
        int colorCount = Math.max(0, item.optInt("colorCount", item.optBoolean("colorable", false) ? 1 : 0));
        if (forcedColor != null && !forcedColor.trim().isEmpty()) {
            if (colors.isEmpty()) colors.add(forcedColor.trim());
            else colors.set(0, forcedColor.trim());
        }
        JSONArray itemColors = item.optJSONArray("colors");
        while (colors.size() < colorCount) {
            String first = firstSelectableColorId(itemColors);
            colors.add(first.isEmpty() ? "1" : first);
        }
        StringBuilder part = new StringBuilder(type + "-" + id);
        for (int i=0; i<Math.min(colorCount, colors.size()); i++) part.append("-").append(colors.get(i));
        return setFigurePart(figure, type, part.toString());
    }

    private String firstSelectableColorId(JSONArray colors) {
        if (colors == null) return "";
        for (int i=0; i<colors.length(); i++) {
            JSONObject c = colors.optJSONObject(i);
            if (c != null && c.optBoolean("selectable", true)) return firstText(c, "id");
        }
        return "";
    }

    private String figurePart(String figure, String type) {
        if (figure == null || type == null) return "";
        String[] parts = figure.split("\\.");
        for (String p : parts) if (p.startsWith(type + "-")) return p;
        return "";
    }

    private String figurePartId(String figure, String type) {
        String p = figurePart(figure, type);
        if (p.isEmpty()) return "";
        String[] bits = p.split("-");
        return bits.length > 1 ? bits[1] : "";
    }

    private String setFigurePart(String figure, String type, String part) {
        ArrayList<String> parts = new ArrayList<>();
        boolean replaced = false;
        if (figure != null && !figure.trim().isEmpty()) {
            for (String p : figure.split("\\.")) {
                if (p.trim().isEmpty()) continue;
                if (p.startsWith(type + "-")) {
                    if (part != null && !part.isEmpty()) parts.add(part);
                    replaced = true;
                } else {
                    parts.add(p);
                }
            }
        }
        if (!replaced && part != null && !part.isEmpty()) parts.add(part);
        return TextUtils.join(".", parts);
    }

    private String removeFigurePart(String figure, String type) {
        return setFigurePart(figure, type, "");
    }

    private boolean visualGenderMatches(String itemGender, String currentGender) {
        if (itemGender == null || itemGender.trim().isEmpty()) return true;
        String g = itemGender.trim().toUpperCase(Locale.ROOT);
        return "U".equals(g) || g.equals(normalizeVisualGender(currentGender, "M"));
    }

    private String normalizeVisualGender(String gender, String fallback) {
        if (gender == null) return fallback == null ? "M" : fallback;
        String g = gender.trim().toUpperCase(Locale.ROOT);
        if (g.startsWith("F")) return "F";
        if (g.startsWith("M")) return "M";
        return fallback == null ? "M" : fallback;
    }

    private String detectVisualGenderFromFigure(String figure, JSONObject figureData, String fallback) {
        String safeFallback = normalizeVisualGender(fallback, "M");
        String hdId = figurePartId(figure, "hd");
        if (hdId.isEmpty()) return safeFallback;
        try {
            JSONObject body = visualCategory(figureData, "hd");
            JSONArray items = body == null ? null : body.optJSONArray("items");
            if (items != null) {
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.optJSONObject(i);
                    if (item == null) continue;
                    String itemId = firstText(item, "id", "setid", "figureId");
                    if (hdId.equals(itemId)) {
                        String rawGender = firstText(item, "gender", "sex");
                        String gender = normalizeVisualGender(rawGender, safeFallback);
                        if ("M".equals(gender) || "F".equals(gender)) return gender;
                    }
                }
            }
        } catch(Exception ignored) {}
        if (hdId.equals(figurePartId(DEFAULT_VISUAL_FIGURE_FEMALE, "hd"))) return "F";
        if (hdId.equals(figurePartId(DEFAULT_VISUAL_FIGURE_MALE, "hd"))) return "M";
        return safeFallback;
    }

    private String visualPreviewBaseFigure(String gender, String itemType) {
        String base = "F".equalsIgnoreCase(gender) ? DEFAULT_VISUAL_FIGURE_FEMALE : DEFAULT_VISUAL_FIGURE_MALE;
        // A página usa a figure padrão para a miniatura dos itens, não o visual carregado por nick.
        return base;
    }

    private ArrayList<String> figurePartColors(String figure, String type) {
        ArrayList<String> out = new ArrayList<>();
        String old = figurePart(figure, type);
        if (old != null && !old.isEmpty()) {
            String[] bits = old.split("-");
            for (int i=2; i<bits.length; i++) if (!bits[i].trim().isEmpty()) out.add(bits[i].trim());
        }
        return out;
    }

    private String getVisualItemTypeForUiCategory(String type) {
        if ("ca".equals(type)) return "cp";
        if ("cp".equals(type)) return "ca";
        return type == null ? "" : type;
    }

    private boolean isVisualRemovableType(String type) {
        return "hr".equals(type) || "ha".equals(type) || "he".equals(type) || "ea".equals(type) || "fa".equals(type)
                || "ch".equals(type) || "ca".equals(type) || "cc".equals(type) || "cp".equals(type)
                || "sh".equals(type) || "wa".equals(type) || "pt".equals(type) || "mc".equals(type);
    }

    private static class VisualGroup {
        String id, icon;
        String[] types;
        boolean genderTabs;
        VisualGroup(String id, String icon, boolean genderTabs, String... types) {
            this.id = id;
            this.icon = icon;
            this.genderTabs = genderTabs;
            this.types = types == null ? new String[0] : types;
        }
    }

    private VisualGroup[] visualCategoryGroups(JSONObject data) {
        ArrayList<VisualGroup> out = new ArrayList<>();
        addVisualGroupIfAvailable(out, data, new VisualGroup("body", categoryIconUrl("hd", false), true, "hd"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("hair", categoryIconUrl("hr", false), false, "hr","ha","he","ea","fa"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("tops", categoryIconUrl("ch", false), false, "ch","ca","cc","cp"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("bottoms", categoryIconUrl("lg", false), false, "lg","sh","wa"));
        addVisualGroupIfAvailable(out, data, new VisualGroup("extras", categoryIconUrl("mc", false), false, "pt","mc"));
        return out.toArray(new VisualGroup[0]);
    }

    private void addVisualGroupIfAvailable(ArrayList<VisualGroup> out, JSONObject data, VisualGroup group) {
        // Mantém todas as categorias visíveis; os dados de cada subcategoria são carregados sob demanda.
        out.add(group);
    }

    private String visualActiveGroup(String type, VisualGroup[] groups) {
        for (VisualGroup g : groups) for (String t : g.types) if (t.equals(type)) return g.id;
        return groups.length > 0 ? groups[0].id : "body";
    }

    private VisualGroup visualFindGroup(String id, VisualGroup[] groups) {
        for (VisualGroup g : groups) if (g.id.equals(id)) return g;
        return groups.length > 0 ? groups[0] : null;
    }

    private String visualIconResource(String name) {
        return "android.resource://" + getPackageName() + "/drawable/" + name;
    }

    private String categoryIconUrl(String type) {
        return categoryIconUrl(type, false);
    }

    private String categoryIconUrl(String type, boolean sub) {
        if ("hd".equals(type)) return visualIconResource("body");
        if ("hr".equals(type)) return sub ? visualIconResource("hair_sn") : visualIconResource("hair");
        if ("ha".equals(type)) return visualIconResource("hats");
        if ("he".equals(type)) return visualIconResource("hair_accessories");
        if ("ea".equals(type)) return visualIconResource("glasses");
        if ("fa".equals(type)) return visualIconResource("moustaches");
        if ("ch".equals(type)) return sub ? visualIconResource("top") : visualIconResource("tops");
        if ("ca".equals(type)) return visualIconResource("chest");
        if ("cc".equals(type)) return visualIconResource("jackets");
        if ("cp".equals(type)) return visualIconResource("accessories");
        if ("pt".equals(type)) return visualIconResource("pets");
        if ("mc".equals(type)) return visualIconResource("misc");
        if ("lg".equals(type)) return sub ? visualIconResource("bottoms_sn") : visualIconResource("bottoms");
        if ("sh".equals(type)) return visualIconResource("shoes");
        if ("wa".equals(type)) return visualIconResource("belts");
        return visualIconResource("misc");
    }

    private View visualIconTab(String url, boolean active, int size) {
        FrameLayout box = new FrameLayout(this);
        box.setPadding(dp(4), dp(4), dp(4), dp(4));
        int fill = active ? Color.argb(lightTheme ? 96 : 92, 171, 77, 255) : Color.argb(lightTheme ? 24 : 34, 255, 255, 255);
        int stroke = active ? Color.rgb(190, 76, 255) : Color.argb(lightTheme ? 28 : 26, 255,255,255);
        box.setBackground(round(fill, dp(14), stroke, active ? 2 : 1));
        if (Build.VERSION.SDK_INT >= 21 && active) box.setElevation(dp(4));
        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int innerSize = visualTabIconInnerSize(url, size);
        FrameLayout.LayoutParams ip = new FrameLayout.LayoutParams(innerSize, innerSize, Gravity.CENTER);
        box.addView(img, ip);
        Glide.with(MainActivity.this).load(url).into(img);
        return box;
    }

    private int visualTabIconInnerSize(String url, int size) {
        int normal = size - dp(12);
        String u = url == null ? "" : url.toLowerCase(Locale.ROOT);
        if (u.contains("/misc") || u.endsWith("misc")
                || u.contains("/pets") || u.endsWith("pets")
                || u.contains("/jackets") || u.endsWith("jackets")
                || u.contains("/accessories") || u.endsWith("accessories")) {
            return Math.max(dp(26), size - dp(18));
        }
        return Math.max(dp(28), normal);
    }

    private float visualItemScale(String type) {
        if ("hd".equals(type)) return 1.02f;
        if ("hr".equals(type) || "ha".equals(type) || "he".equals(type) || "ea".equals(type)) return 1.58f;
        if ("fa".equals(type)) return 1.30f;
        if ("lg".equals(type)) return 1.48f;
        if ("sh".equals(type)) return 1.82f;
        if ("ch".equals(type) || "ca".equals(type) || "cc".equals(type) || "cp".equals(type)) return 1.34f;
        if ("wa".equals(type) || "pt".equals(type) || "mc".equals(type)) return 1.34f;
        return 1.15f;
    }

    private int visualItemOffsetDp(String type) {
        if ("hr".equals(type) || "ha".equals(type) || "he".equals(type) || "ea".equals(type)) return 18;
        if ("fa".equals(type)) return 4;
        if ("ch".equals(type) || "cp".equals(type) || "ca".equals(type)) return -10;
        if ("cc".equals(type)) return -14;
        if ("lg".equals(type)) return -22;
        if ("sh".equals(type)) return -38;
        if ("wa".equals(type)) return -16;
        if ("pt".equals(type)) return -20;
        if ("mc".equals(type)) return -14;
        return 0;
    }

    private String applyFigureItemColorSlot(String figure, String type, JSONObject item, String colorId, int slot) {
        if (item == null || colorId == null || colorId.trim().isEmpty()) return figure;
        String id = firstText(item, "id");
        if (id.isEmpty()) return figure;
        String old = figurePart(figure, type);
        ArrayList<String> colors = new ArrayList<>();
        if (old != null && !old.isEmpty()) {
            String[] bits = old.split("-");
            for (int i=2; i<bits.length; i++) if (!bits[i].trim().isEmpty()) colors.add(bits[i].trim());
        }
        int colorCount = Math.max(1, Math.min(2, item.optInt("colorCount", item.optBoolean("colorable", false) ? 1 : 0)));
        JSONArray itemColors = item.optJSONArray("colors");
        while (colors.size() < colorCount) {
            String first = firstSelectableColorId(itemColors);
            colors.add(first.isEmpty() ? "1" : first);
        }
        int s = Math.max(0, Math.min(colorCount - 1, slot));
        colors.set(s, colorId.trim());
        StringBuilder part = new StringBuilder(type + "-" + id);
        for (int i=0; i<colorCount; i++) part.append("-").append(colors.get(i));
        return setFigurePart(figure, type, part.toString());
    }

    private String visualCategoryName(String type) {
        if ("hr".equals(type)) return t("cat_hair");
        if ("hd".equals(type)) return t("cat_head");
        if ("ch".equals(type)) return t("cat_shirts");
        if ("lg".equals(type)) return t("cat_pants");
        if ("sh".equals(type)) return t("cat_shoes");
        if ("ha".equals(type)) return t("cat_hats");
        if ("he".equals(type)) return t("cat_accessories");
        if ("ea".equals(type)) return t("cat_face");
        if ("fa".equals(type)) return t("cat_face");
        if ("ca".equals(type)) return t("cat_coats");
        if ("cc".equals(type)) return t("cat_coats");
        if ("cp".equals(type)) return t("cat_prints");
        if ("wa".equals(type)) return t("cat_accessories");
        return type.toUpperCase(Locale.ROOT);
    }

    private int colorFromHex(String hex) {
        try {
            String h = hex == null ? "" : hex.trim();
            if (!h.startsWith("#")) h = "#" + h;
            return Color.parseColor(h);
        } catch(Exception e) {
            return Color.WHITE;
        }
    }

    private int lightenColorForVisualBorder(int color, float amount) {
        amount = Math.max(0f, Math.min(1f, amount));
        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        r = Math.min(255, Math.round(r + (255 - r) * amount));
        g = Math.min(255, Math.round(g + (255 - g) * amount));
        b = Math.min(255, Math.round(b + (255 - b) * amount));
        return Color.argb(Math.max(230, a), r, g, b);
    }

    private void showSettingsDialog() {
        final Dialog dialog = new Dialog(this);
        PullDispatchFrameLayout full = new PullDispatchFrameLayout(this);
        full.setBackground(makeBg());

        ScrollView dialogScroll = new ScrollView(this);
        dialogScroll.setFillViewport(true);
        dialogScroll.setVerticalScrollBarEnabled(false);

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(34), dp(18), dp(82));
        wrap.setBackgroundColor(Color.TRANSPARENT);
        dialogScroll.addView(wrap, new ScrollView.LayoutParams(-1, -1));
        full.addView(dialogScroll, new FrameLayout.LayoutParams(-1, -1));

        addBottomNavigation(full, 3, dialog);
        dialog.setContentView(full);

        TextView title = habboText(t("settings"), 24, true);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 18));

        LinearLayout favNotifyRow = new LinearLayout(this);
        favNotifyRow.setOrientation(LinearLayout.HORIZONTAL);
        favNotifyRow.setGravity(Gravity.CENTER_VERTICAL);
        favNotifyRow.setPadding(dp(12), dp(10), dp(12), dp(10));
        favNotifyRow.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(218,218,218) : Color.argb(28,255,255,255), 1));
        TextView favNotifyText = text(t("notify_favorite_online"), 14, lightTheme ? Color.rgb(33,33,33) : Color.WHITE, true);
        favNotifyText.setGravity(Gravity.CENTER_VERTICAL);
        favNotifyRow.addView(favNotifyText, new LinearLayout.LayoutParams(0, -2, 1));
        TextView favNotifyToggle = text("", 1, Color.TRANSPARENT, false);
        favNotifyToggle.setBackground(new AchievementSwitchDrawable(notifyFavoriteOnline));
        favNotifyRow.addView(favNotifyToggle, new LinearLayout.LayoutParams(dp(58), dp(34)));
        wrap.addView(favNotifyRow, lp(-1, -2, 0, 0, 0, 10));
        favNotifyRow.setOnClickListener(v -> {
            notifyFavoriteOnline = !notifyFavoriteOnline;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putBoolean(PREF_NOTIFY_FAVORITE_ONLINE, notifyFavoriteOnline).apply();
            favNotifyToggle.setBackground(new AchievementSwitchDrawable(notifyFavoriteOnline));
            startFavoriteOnlineWatcher();
            updateFavoriteOnlineAlarm();
        });


        TextView hotelTitle = text(t("search_hotel"), 13, themeMutedColor(), true);
        hotelTitle.setGravity(Gravity.CENTER);
        wrap.addView(hotelTitle, lp(-1, -2, 0, 0, 0, 8));

        LinearLayout hotelGrid = new LinearLayout(this);
        hotelGrid.setOrientation(LinearLayout.VERTICAL);
        addHotelButtonRow(hotelGrid, dialog, "br", "com", "es");
        addHotelButtonRow(hotelGrid, dialog, "de", "fr", "fi");
        addHotelButtonRow(hotelGrid, dialog, "it", "nl", "tr");
        wrap.addView(hotelGrid, lp(-1, -2, 0, 0, 0, 14));

        LinearLayout themeRow = new LinearLayout(this);
        themeRow.setOrientation(LinearLayout.HORIZONTAL);
        themeRow.setGravity(Gravity.CENTER);
        TextView lightBtn = text("", 1, Color.TRANSPARENT, false);
        TextView darkBtn = text("", 1, Color.TRANSPARENT, false);
        lightBtn.setGravity(Gravity.CENTER);
        darkBtn.setGravity(Gravity.CENTER);
        lightBtn.setBackground(new ThemeIconButtonDrawable(true, lightTheme));
        darkBtn.setBackground(new ThemeIconButtonDrawable(false, !lightTheme));
        LinearLayout.LayoutParams th1 = new LinearLayout.LayoutParams(dp(46), dp(46)); th1.rightMargin = dp(7);
        LinearLayout.LayoutParams th2 = new LinearLayout.LayoutParams(dp(46), dp(46)); th2.leftMargin = dp(7);
        themeRow.addView(lightBtn, th1);
        themeRow.addView(darkBtn, th2);
        wrap.addView(themeRow, lp(-1, dp(50), 0, 0, 0, 10));
        lightBtn.setOnClickListener(v -> {
            if (lightTheme) return;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("theme", "light").apply();
            lightTheme = true;
            openingSplashShownThisSession = true;
            applySystemBarsForTheme();
            rebuildUiPreservingProfile();
            showSettingsDialog();
            uiHandler.postDelayed(() -> {
                try { dialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        });
        darkBtn.setOnClickListener(v -> {
            if (!lightTheme) return;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString("theme", "dark").apply();
            lightTheme = false;
            openingSplashShownThisSession = true;
            applySystemBarsForTheme();
            rebuildUiPreservingProfile();
            showSettingsDialog();
            uiHandler.postDelayed(() -> {
                try { dialog.dismiss(); } catch (Exception ignored) {}
            }, 120L);
        });


        Space cacheBottomSpacer = new Space(this);
        wrap.addView(cacheBottomSpacer, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView info = text(t("app_cache") + ": ...", 13, muted, false);
        info.setGravity(Gravity.CENTER);
        info.setPadding(dp(10), dp(10), dp(10), dp(10));
        info.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(14), lightTheme ? Color.rgb(218,218,218) : Color.argb(28,255,255,255), 1));
        wrap.addView(info, lp(-1, -2, 0, 0, 0, 14));
        updateCacheStatsLabelAsync(info);

        TextView clear = dialogButton(t("clear_app_cache"));
        clear.setBackground(grad(dp(14), Color.rgb(120, 36, 46), Color.rgb(210, 54, 77)));
        wrap.addView(clear, lp(-1, dp(48), 0, 0, 0, 10));
        clear.setOnClickListener(v -> {
            clear.setEnabled(false);
            info.setText(t("app_cache") + ": ...");
            clearProfileCache(() -> {
                updateCacheStatsLabelAsync(info);
                clear.setEnabled(true);
                toast(t("app_cache_cleared"));
            });
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            w.setWindowAnimations(0);
            w.setAttributes(params);
        }
    }




    private void handleMainPullToRefreshDispatch(MotionEvent event) {
        if (mainScroll == null || activeRenderedProfile == null || searchInProgress) {
            if (pullDragging) resetMainPullIndicator();
            return;
        }

        int action = event.getActionMasked();
        int trigger = dp(220);
        int maxPull = dp(190);

        if (action == MotionEvent.ACTION_DOWN) {
            pullStartY = event.getRawY();
            pullStartedAtTop = mainScroll.getScrollY() <= 0;
            pullReadyToRefresh = false;
            pullDragging = false;
            return;
        }

        if (action == MotionEvent.ACTION_MOVE && pullStartedAtTop) {
            float dy = event.getRawY() - pullStartY;
            if (dy <= 0f) {
                pullReadyToRefresh = false;
                resetMainPullIndicator();
                return;
            }
            if (mainScroll.getScrollY() > 0 && !pullDragging) return;

            pullDragging = true;
            float progressValue = Math.max(0f, Math.min(1f, dy / Math.max(1, trigger)));
            float elastic = elasticPullDistance(dy, trigger, maxPull);
            updateMainPullIndicator(progressValue, elastic);
            pullReadyToRefresh = progressValue >= 1f;
            return;
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            boolean shouldRefresh = pullStartedAtTop && pullDragging && pullReadyToRefresh;
            pullStartedAtTop = false;
            pullReadyToRefresh = false;
            pullDragging = false;
            resetMainPullIndicator();
            if (shouldRefresh) refreshCurrentProfileWithCooldown(true);
        }
    }

    private float elasticPullDistance(float dy, int trigger, int maxPull) {
        return Math.min(maxPull, (float)(maxPull * (1d - (1d / (1d + (dy / Math.max(1, trigger)))))));
    }

    private void updateMainPullIndicator(float progressValue, float elastic) {
        if (pullRefreshChip == null) return;
        pullRefreshChip.setVisibility(View.VISIBLE);
        pullRefreshChip.animate().cancel();
        pullRefreshChip.setAlpha(Math.max(0.15f, progressValue));
        pullRefreshChip.setTranslationY(-dp(22) + (elastic * 0.40f));
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(progressValue);
        if (pullRefreshText != null) pullRefreshText.setText(progressValue >= 1f ? t("updating_profile") : t("updating_profile"));
        if (mainScroll != null) {
            mainScroll.animate().cancel();
            mainScroll.setTranslationY(elastic);
        }
    }

    private void resetMainPullIndicator() {
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(0);
        if (mainScroll != null) mainScroll.animate().translationY(0f).setDuration(190L).start();
        if (pullRefreshChip != null) {
            pullRefreshChip.animate().cancel();
            pullRefreshChip.animate().alpha(0f).translationY(-dp(44)).setDuration(180L).withEndAction(() -> pullRefreshChip.setVisibility(View.GONE)).start();
        }
    }

    private void refreshCurrentProfileWithCooldown(boolean fromPull) {
        if (activeRenderedProfile == null) return;
        String nick = activeRenderedProfile.name == null || activeRenderedProfile.name.trim().isEmpty() ? activeRenderedProfile.searchedNick : activeRenderedProfile.name;
        String uniqueId = activeRenderedProfile.uniqueId == null ? "" : activeRenderedProfile.uniqueId.trim();
        if ((nick == null || nick.trim().isEmpty()) && uniqueId.isEmpty()) return;

        String displayNick = nick == null || nick.trim().isEmpty() ? uniqueId : nick.trim();
        String refreshKey = normalizeNickKey(uniqueId.isEmpty() ? displayNick : uniqueId);
        if (!searchInProgress && activeRenderedProfile != null && refreshKey.equals(currentLoadedNick) && normalizeHotelKey(activeRenderedProfile.hotelKey).equals(currentHotelKey)) {
            long now = System.currentTimeMillis();
            long wait = PROFILE_REFRESH_COOLDOWN_MS - (now - lastSameNickRefreshAt);
            if (wait > 0) {
                hidePullRefreshIndicator();
                toast(tr("wait_refresh", Math.max(1, (int)Math.ceil(wait / 1000.0))));
                return;
            }
        }

        setSearchTextProgrammatically(displayNick);
        if (fromPull) showPullRefreshIndicator();
        if (!uniqueId.isEmpty()) searchByUniqueId(uniqueId, displayNick);
        else search();
    }

    private void showPullRefreshIndicator() {
        if (pullRefreshChip == null) return;
        if (pullRefreshText != null) pullRefreshText.setText(t("updating_profile"));
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(1f);
        pullRefreshChip.setVisibility(View.VISIBLE);
        pullRefreshChip.animate().cancel();
        pullRefreshChip.setAlpha(0f);
        pullRefreshChip.setTranslationY(-dp(40));
        pullRefreshChip.animate().alpha(1f).translationY(0).setDuration(180).start();
        if (mainScroll != null) {
            mainScroll.animate().cancel();
            mainScroll.animate().translationY(dp(34)).setDuration(150).withEndAction(() -> mainScroll.animate().translationY(0).setDuration(220).start()).start();
        }
    }

    private void hidePullRefreshIndicator() {
        if (pullRefreshSpinner != null) pullRefreshSpinner.setProgressPct(0);
        if (pullRefreshChip == null) return;
        pullRefreshChip.animate().cancel();
        pullRefreshChip.animate().alpha(0f).translationY(-dp(40)).setDuration(180).withEndAction(() -> pullRefreshChip.setVisibility(View.GONE)).start();
    }

    private String normalizeHotelKey(String hotel) {
        String h = hotel == null ? "" : hotel.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if ("us".equals(h)) h = "com";
        String[] allowed = {"br","com","es","de","fr","fi","it","nl","tr"};
        for (String a : allowed) if (a.equals(h)) return h;
        return "";
    }

    private String defaultHotelForDeviceLocale() {
        String lang = Locale.getDefault().getLanguage();
        String country = Locale.getDefault().getCountry();
        if ("pt".equals(lang) || "BR".equalsIgnoreCase(country)) return "br";
        if ("es".equals(lang)) return "es";
        if ("de".equals(lang)) return "de";
        if ("fr".equals(lang)) return "fr";
        if ("fi".equals(lang)) return "fi";
        if ("it".equals(lang)) return "it";
        if ("nl".equals(lang)) return "nl";
        if ("tr".equals(lang)) return "tr";
        return "com";
    }

    private String hotelDomain(String key) {
        String h = normalizeHotelKey(key);
        if ("com".equals(h)) return "www.habbo.com";
        if ("es".equals(h)) return "www.habbo.es";
        if ("de".equals(h)) return "www.habbo.de";
        if ("fr".equals(h)) return "www.habbo.fr";
        if ("fi".equals(h)) return "www.habbo.fi";
        if ("it".equals(h)) return "www.habbo.it";
        if ("nl".equals(h)) return "www.habbo.nl";
        if ("tr".equals(h)) return "www.habbo.com.tr";
        return "www.habbo.com.br";
    }

    private String habbodexHotelCode(String key) {
        String h = normalizeHotelKey(key);
        return "com".equals(h) ? "us" : (h.isEmpty() ? "br" : h);
    }

    private String hotelLabel(String key) {
        String h = normalizeHotelKey(key);
        if ("com".equals(h)) return ".COM";
        if ("tr".equals(h)) return ".COM.TR";
        if (h.isEmpty()) h = "br";
        return "." + h.toUpperCase(Locale.ROOT);
    }

    private String hotelName(String key) {
        return "Hotel " + hotelLabel(key).toLowerCase(Locale.ROOT).replace(".com.tr", ".com.tr");
    }

    private String hotelFlag(String key) {
        return hotelLabel(key);
    }

    private String currentLang() {
        String h = normalizeHotelKey(currentHotelKey);
        if ("com".equals(h)) return "en";
        if ("es".equals(h)) return "es";
        if ("de".equals(h)) return "de";
        if ("fr".equals(h)) return "fr";
        if ("fi".equals(h)) return "fi";
        if ("it".equals(h)) return "it";
        if ("nl".equals(h)) return "nl";
        if ("tr".equals(h)) return "tr";
        return "pt";
    }

    private String tr(String key, Object... args) {
        try {
            return String.format(Locale.ROOT, t(key), args);
        } catch (Exception ignored) {
            return t(key);
        }
    }

    private String t(String key) {
        String lang = currentLang();

        if ("en".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Pull to refresh favorites";
                case "favorites_release": return "Release to refresh favorites";
                case "favorites_updating": return "Updating favorites...";
                case "favorites_updated": return "Favorites updated.";
                case "searching": return "Searching —";
                case "search_button": return "Search";
                case "search_hint": return "Enter a nick...";
                case "ready_search": return "Ready to search";
                case "start_note": return "Enter a nickname from the selected hotel to view the information.";
                case "type_nick_toast": return "Enter a hotel nickname to search.";
                case "same_profile_loading": return "This profile is already being loaded.";
                case "wait_refresh": return "Wait %ss before refreshing this profile again.";
                case "updating_profile": return "Updating profile...";
                case "searching_profile": return "Searching profile...";
                case "not_found_body": return "I couldn't find a current account with the nickname %s in the selected hotel.";
                case "settings": return "Settings";
                case "search_hotel": return "Search hotel";
                case "hotel_changed": return "Hotel and language updated.";
                case "adfree_title": return "Ad-free access";
                case "buy_no_ads": return "Get the ad-free version!";
                case "premium_title": return "Become Premium";
                case "premium_remove_ads": return "Remove all ads";
                case "premium_pay_once": return "PAY ONCE";
                case "remove_ads_purchased": return "Ad-free version activated.";
                case "purchase_pending": return "Payment pending. The ad-free version will activate after confirmation.";
                case "purchase_loading": return "Preparing purchase. Try again in a few seconds.";
                case "purchase_error": return "Could not open the purchase right now.";
                case "adfree_msg_add": return "You still have %s without ads. Do you want to watch a video to add 30 more minutes? The maximum limit is 4 hours.";
                case "adfree_msg_new": return "Do you want to watch a video to unlock 30 minutes without ads while searching profiles?";
                case "time_left": return "Time left";
                case "cancel": return "Cancel";
                case "watch_video": return "Watch video";
                case "cannot_show_video": return "Couldn't show the video right now.";
                case "limit_24h": return "You already reached the 4-hour ad-free limit.";
                case "video_loading": return "The video is still loading. Try again in a few seconds.";
                case "adfree_granted": return "30 ad-free minutes unlocked.";
                case "visual_item_info": return "Item information";
                case "loading_item_info": return "Loading information...";
                case "item_name": return "Visual name";
                case "collection": return "Collection";
                case "image": return "Image";
                case "thumbnail": return "Thumbnail";
                case "disclaimer1": return "This application is not affiliated with, endorsed, sponsored, or specifically approved by Sulake Corporation Oy or its affiliates.";
                case "private": return "Private";
                case "banned": return "Banned";
                case "status": return "Status";
                case "online": return "Online";
                case "offline": return "Offline";
                case "last_login": return "Last login";
                case "creation": return "Created";
                case "friends": return "Friends";
                case "removed": return "Removed";
                case "rooms": return "Rooms";
                case "groups": return "Groups";
                case "photos": return "Photos";
                case "stars": return "Stars";
                case "level": return "Level";
                case "previous": return "Previous";
                case "previous_names": return "Previous names";
                case "previous_mottos": return "Previous mottos";
                case "previous_styles": return "Previous looks";
                case "user_photos": return "User photos";
                case "selected_badges": return "Selected badges";
                case "profile_history": return "Profile history";
                case "no_history": return "No profiles opened yet.";
                case "clear_history": return "Clear history";
                case "history_cleared": return "History cleared.";
                case "loading_details": return "Loading profile details...";
                case "loading_history": return "Loading history...";
                case "loading_styles_friends": return "Loading looks and friends...";
                case "loading_rooms_groups": return "Loading rooms and groups...";
                case "loading_clothes": return "Loading clothing...";
                case "cannot_load_clothes": return "Couldn't load the items.";
                case "no_clothes_found": return "No items found";
                case "current_look": return "Current look";
                case "looks": return "Looks";
                case "liked_by": return "Liked by";
                case "no_description": return "No description.";
                case "name": return "Name";
                case "description": return "Description";
                case "created": return "Created";
                case "code": return "Code";
                case "owner": return "Owner";
                case "room": return "Room";
                case "date": return "Date";
                case "likes": return "Likes";
                case "page_of": return "Page %s of %s";
                case "no_rooms_found": return "No rooms found.";
                case "close": return "Close";
                case "item": return "Item";
                case "tutorial_settings_title": return "Change hotel";
                case "tutorial_settings_body": return "Tap the menu icon at the bottom to choose the hotel and change the app language.";
                case "tutorial_search_title": return "Search profile";
                case "tutorial_search_body": return "Enter a nickname in the search bar and tap Search to look up public data.";
                case "tutorial_history_title": return "History";
                case "tutorial_history_body": return "Use the history button to quickly return to profiles you already opened.";
                case "tutorial_next": return "Next";
                case "tutorial_finish": return "Got it";
                case "error_search_profile": return "Failed to search profile.";
                case "no_profile_found": return "No profile found";
                case "generic_loading": return "Loading...";
                case "no_friend_found": return "No friends found.";
                case "no_removed_friend_found": return "No removed friends found.";
                case "light_theme": return "Light theme";
                case "dark_theme": return "Dark theme";
                case "clear_app_cache": return "Clear cache";
                case "app_cache_cleared": return "App cache cleared.";
                case "session_profiles": return "Profiles in session";
                case "app_cache": return "App cache";
                case "tap_to_continue": return "Tap anywhere to continue";
                case "profile": return "Profile";
                case "badges": return "Badges";
                case "show_achievements": return "Show achievements";
                case "hide_achievements": return "Hide achievements";
                case "no_badges_found": return "No badges found.";
                case "obtained": return "Obtained";
                case "achievements": return "Achievements";
                case "total_owners": return "Owners";
                case "suggestions": return "Suggestions";
                case "loading_suggestions": return "Loading suggestions...";
                case "old_nick": return "Old nick";
                case "changed_at": return "Changed on";
                case "old_nick_suggestions_title": return "This nickname seems to have been used before by:";
                case "no_old_nick_suggestions": return "I also couldn't find current accounts that used this nickname.";
                case "favorites": return "Favorites";
                case "no_favorites": return "No favorite profiles yet.";
                case "favorite_currently_online": return "Currently online!";
                case "notify_favorite_online": return "Notify when a favorite comes online";
                case "favorite_online_banner": return "%s just came online!";
                case "favorite_added": return "Added to favorites.";
                case "favorite_removed": return "Removed from favorites.";
                case "favorite_limit_reached": return "Favorite limit reached: %s.";
                case "open_full_profile": return "Open full profile";
                case "hide_badges": return "Hide achievements";
                case "time_ago": return "%s %s ago";
                case "ago_second": return "second";
                case "ago_seconds": return "seconds";
                case "ago_minute": return "minute";
                case "ago_minutes": return "minutes";
                case "ago_hour": return "hour";
                case "ago_hours": return "hours";
                case "ago_day": return "day";
                case "ago_days": return "days";
                case "ago_week": return "week";
                case "ago_weeks": return "weeks";
                case "ago_month": return "month";
                case "ago_months": return "months";
                case "ago_year": return "year";
                case "ago_years": return "years";
                case "visual_editor": return "Visual editor";
                case "saved_visuals": return "Saved looks";
                case "no_saved_visuals": return "No saved looks yet.";
                case "visual_saved": return "Look saved.";
                case "visual_already_saved": return "This look is already saved.";
                case "visual_saved_limit": return "You already have %s saved looks. Remove one before saving another.";
                case "visual_removed": return "Look removed.";
                case "visual_save_limit": return "Saved look limit reached: %s.";
                case "type_nick": return "Enter a nick...";
                case "male": return "Male";
                case "female": return "Female";
                case "loading_visuals": return "Loading visual editor...";
                case "cannot_load_visuals": return "Couldn't load the visual editor.";
                case "not_found_simple": return "Profile not found.";
                case "no_items_found": return "No items found.";
                case "remove_item": return "Remove";
                case "available_colors": return "Available colors";
                case "cat_hair": return "Hair";
                case "cat_head": return "Skin";
                case "cat_shirts": return "Shirts";
                case "cat_pants": return "Pants";
                case "cat_shoes": return "Shoes";
                case "cat_hats": return "Hats";
                case "cat_accessories": return "Accessories";
                case "cat_face": return "Face";
                case "cat_coats": return "Coats";
                case "cat_prints": return "Prints";
            }
        }
        else if ("es".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Tira para actualizar favoritos";
                case "favorites_release": return "Suelta para actualizar favoritos";
                case "favorites_updating": return "Actualizando favoritos...";
                case "favorites_updated": return "Favoritos actualizados.";
                case "private": return "Privado";
                case "banned": return "Baneado";
                case "status": return "Estado";
                case "online": return "En línea";
                case "offline": return "Desconectado";
                case "last_login": return "Último acceso";
                case "creation": return "Creación";
                case "friends": return "Amigos";
                case "removed": return "Eliminados";
                case "rooms": return "Salas";
                case "groups": return "Grupos";
                case "photos": return "Fotos";
                case "stars": return "Estrellas";
                case "level": return "Nivel";
                case "previous": return "Anteriores";
                case "previous_names": return "Nombres anteriores";
                case "previous_mottos": return "Misiones anteriores";
                case "previous_styles": return "Looks anteriores";
                case "user_photos": return "Fotos del usuario";
                case "selected_badges": return "Placas seleccionadas";
                case "profile_history": return "Historial de perfiles";
                case "no_history": return "Aún no hay perfiles abiertos.";
                case "clear_history": return "Limpiar historial";
                case "history_cleared": return "Historial limpiado.";
                case "loading_details": return "Cargando detalles del perfil...";
                case "loading_history": return "Cargando historial...";
                case "loading_styles_friends": return "Cargando looks y amigos...";
                case "loading_rooms_groups": return "Cargando salas y grupos...";
                case "loading_clothes": return "Cargando prendas...";
                case "cannot_load_clothes": return "No fue posible cargar las prendas.";
                case "no_clothes_found": return "No se encontraron prendas";
                case "current_look": return "Look actual";
                case "looks": return "Looks";
                case "liked_by": return "Me gusta de";
                case "no_description": return "Sin descripción.";
                case "name": return "Nombre";
                case "description": return "Descripción";
                case "created": return "Creado";
                case "code": return "Código";
                case "owner": return "Dueño";
                case "room": return "Sala";
                case "date": return "Fecha";
                case "likes": return "Me gusta";
                case "page_of": return "Página %s de %s";
                case "no_rooms_found": return "No se encontraron salas.";
                case "close": return "Cerrar";
                case "item": return "Prenda";
                case "tutorial_settings_title": return "Cambiar hotel";
                case "tutorial_settings_body": return "Toca el icono de menú inferior para elegir el hotel y cambiar el idioma de la app.";
                case "tutorial_search_title": return "Buscar perfil";
                case "tutorial_search_body": return "Escribe un nick en la barra y toca Buscar para consultar datos públicos.";
                case "tutorial_history_title": return "Historial";
                case "tutorial_history_body": return "Usa el botón de historial para volver rápidamente a perfiles abiertos.";
                case "tutorial_next": return "Siguiente";
                case "tutorial_finish": return "Entendido";
                case "searching": return "Buscando —";
                case "search_button": return "Buscar";
                case "search_hint": return "Escribe un nick...";
                case "ready_search": return "Listo para buscar";
                case "start_note": return "Escribe un nick del hotel seleccionado para consultar la información.";
                case "type_nick_toast": return "Escribe un nick del hotel para buscar.";
                case "same_profile_loading": return "Ese perfil ya se está cargando.";
                case "wait_refresh": return "Espera %ss para actualizar este perfil nuevamente.";
                case "updating_profile": return "Actualizando perfil...";
                case "searching_profile": return "Buscando perfil...";
                case "not_found_body": return "No encontré una cuenta actual con el nick %s en el hotel seleccionado.";
                case "settings": return "Configuración";
                case "search_hotel": return "Hotel de búsqueda";
                case "hotel_changed": return "Hotel e idioma actualizados.";
                case "adfree_title": return "Acceso sin anuncios";
                case "buy_no_ads": return "¡Adquiere la versión sin anuncios!";
                case "premium_title": return "Hazte Premium";
                case "premium_remove_ads": return "Eliminar todos los anuncios";
                case "premium_pay_once": return "PAGA UNA VEZ";
                case "remove_ads_purchased": return "Versión sin anuncios activada.";
                case "purchase_pending": return "Pago pendiente. La versión sin anuncios se activará tras la confirmación.";
                case "purchase_loading": return "Preparando la compra. Inténtalo de nuevo en unos segundos.";
                case "purchase_error": return "No se pudo abrir la compra ahora.";
                case "adfree_msg_add": return "Todavía tienes %s sin anuncios. ¿Quieres ver un vídeo para añadir 30 minutos más? El límite máximo es 4 horas.";
                case "adfree_msg_new": return "¿Quieres ver un vídeo para desbloquear 30 minutos sin anuncios al buscar perfiles?";
                case "time_left": return "Tiempo restante";
                case "cancel": return "Cancelar";
                case "watch_video": return "Ver vídeo";
                case "cannot_show_video": return "No se pudo mostrar el vídeo ahora.";
                case "limit_24h": return "Ya alcanzaste el límite de 4 horas sin anuncios.";
                case "video_loading": return "El vídeo todavía se está cargando. Inténtalo de nuevo en unos segundos.";
                case "adfree_granted": return "Se liberaron 30 minutos sin anuncios.";
                case "disclaimer1": return "Esta aplicación no está afiliada, respaldada, patrocinada ni específicamente aprobada por Sulake Corporation Oy o sus afiliadas.";
                case "error_search_profile": return "Error al buscar el perfil.";
                case "no_profile_found": return "No se encontró ningún perfil";
                case "generic_loading": return "Cargando...";
                case "no_friend_found": return "No se encontraron amigos.";
                case "no_removed_friend_found": return "No se encontraron amigos eliminados.";
                case "light_theme": return "Tema claro";
                case "dark_theme": return "Tema oscuro";
                case "clear_app_cache": return "Limpiar caché";
                case "app_cache_cleared": return "Caché de la app limpiada.";
                case "session_profiles": return "Perfiles en sesión";
                case "app_cache": return "Caché de la app";
                case "tap_to_continue": return "Toca cualquier lugar para continuar";
                case "profile": return "Perfil";
                case "badges": return "Placas";
                case "show_achievements": return "Mostrar logros";
                case "hide_achievements": return "Ocultar logros";
                case "no_badges_found": return "No se encontraron placas.";
                case "obtained": return "Obtenido";
                case "achievements": return "Logros";
                case "total_owners": return "Propietarios";
                case "suggestions": return "Sugerencias";
                case "loading_suggestions": return "Cargando sugerencias...";
                case "old_nick": return "Nick antiguo";
                case "changed_at": return "Cambiado el";
                case "old_nick_suggestions_title": return "Este nick parece haber sido usado antes por:";
                case "no_old_nick_suggestions": return "Tampoco encontré cuentas actuales que hayan usado este nick.";
                case "favorites": return "Favoritos";
                case "no_favorites": return "Aún no hay perfiles favoritos.";
                case "favorite_currently_online": return "¡Actualmente en línea!";
                case "notify_favorite_online": return "Notificar cuando un favorito se conecte";
                case "favorite_online_banner": return "¡%s acaba de conectarse!";
                case "favorite_added": return "Añadido a favoritos.";
                case "favorite_removed": return "Eliminado de favoritos.";
                case "favorite_limit_reached": return "Límite de favoritos alcanzado: %s.";
                case "open_full_profile": return "Abrir perfil completo";
                case "hide_badges": return "Ocultar logros";
                case "time_ago": return "hace %s %s";
                case "ago_second": return "segundo";
                case "ago_seconds": return "segundos";
                case "ago_minute": return "minuto";
                case "ago_minutes": return "minutos";
                case "ago_hour": return "hora";
                case "ago_hours": return "horas";
                case "ago_day": return "día";
                case "ago_days": return "días";
                case "ago_week": return "semana";
                case "ago_weeks": return "semanas";
                case "ago_month": return "mes";
                case "ago_months": return "meses";
                case "ago_year": return "año";
                case "ago_years": return "años";
                case "visual_editor": return "Probador de looks";
                case "saved_visuals": return "Looks guardados";
                case "no_saved_visuals": return "Aún no hay looks guardados.";
                case "visual_saved": return "Look guardado.";
                case "visual_already_saved": return "Este look ya está guardado.";
                case "visual_saved_limit": return "Ya tienes %s looks guardados. Elimina uno antes de guardar otro.";
                case "visual_removed": return "Look eliminado.";
                case "visual_save_limit": return "Límite de looks guardados: %s.";
                case "type_nick": return "Escribe un nick...";
                case "male": return "Masculino";
                case "female": return "Femenino";
                case "loading_visuals": return "Cargando probador...";
                case "cannot_load_visuals": return "No se pudo cargar el probador.";
                case "not_found_simple": return "Perfil no encontrado.";
                case "no_items_found": return "No se encontraron ítems.";
                case "remove_item": return "Quitar";
                case "available_colors": return "Colores disponibles";
                case "cat_hair": return "Pelo";
                case "cat_head": return "Piel";
                case "cat_shirts": return "Camisas";
                case "cat_pants": return "Pantalones";
                case "cat_shoes": return "Zapatos";
                case "cat_hats": return "Sombreros";
                case "cat_accessories": return "Accesorios";
                case "cat_face": return "Rostro";
                case "cat_coats": return "Abrigos";
                case "cat_prints": return "Estampados";
            }
        }
        else if ("de".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Ziehen, um Favoriten zu aktualisieren";
                case "favorites_release": return "Loslassen, um Favoriten zu aktualisieren";
                case "favorites_updating": return "Favoriten werden aktualisiert...";
                case "favorites_updated": return "Favoriten aktualisiert.";
                case "private": return "Privat";
                case "banned": return "Gesperrt";
                case "status": return "Status";
                case "online": return "Online";
                case "offline": return "Offline";
                case "last_login": return "Letzter Login";
                case "creation": return "Erstellt";
                case "friends": return "Freunde";
                case "removed": return "Entfernt";
                case "rooms": return "Räume";
                case "groups": return "Gruppen";
                case "photos": return "Fotos";
                case "stars": return "Sterne";
                case "level": return "Level";
                case "previous": return "Vorherige";
                case "previous_names": return "Frühere Namen";
                case "previous_mottos": return "Frühere Mottos";
                case "previous_styles": return "Frühere Looks";
                case "user_photos": return "Benutzerfotos";
                case "selected_badges": return "Ausgewählte Abzeichen";
                case "profile_history": return "Profilverlauf";
                case "no_history": return "Noch keine Profile geöffnet.";
                case "clear_history": return "Verlauf löschen";
                case "history_cleared": return "Verlauf gelöscht.";
                case "loading_details": return "Profildetails werden geladen...";
                case "loading_history": return "Verlauf wird geladen...";
                case "loading_styles_friends": return "Looks und Freunde werden geladen...";
                case "loading_rooms_groups": return "Räume und Gruppen werden geladen...";
                case "loading_clothes": return "Kleidung wird geladen...";
                case "cannot_load_clothes": return "Die Teile konnten nicht geladen werden.";
                case "no_clothes_found": return "Keine Teile gefunden";
                case "current_look": return "Aktueller Look";
                case "looks": return "Looks";
                case "liked_by": return "Gefällt von";
                case "no_description": return "Keine Beschreibung.";
                case "name": return "Name";
                case "description": return "Beschreibung";
                case "created": return "Erstellt";
                case "code": return "Code";
                case "owner": return "Besitzer";
                case "room": return "Raum";
                case "date": return "Datum";
                case "likes": return "Likes";
                case "page_of": return "Seite %s von %s";
                case "no_rooms_found": return "Keine Räume gefunden.";
                case "close": return "Schließen";
                case "item": return "Teil";
                case "tutorial_settings_title": return "Hotel wechseln";
                case "tutorial_settings_body": return "Tippe unten auf das Menü-Symbol, um Hotel und App-Sprache zu ändern.";
                case "tutorial_search_title": return "Profil suchen";
                case "tutorial_search_body": return "Gib einen Namen ein und tippe auf Suchen, um öffentliche Daten abzurufen.";
                case "tutorial_history_title": return "Verlauf";
                case "tutorial_history_body": return "Mit dem Verlauf kommst du schnell zu bereits geöffneten Profilen zurück.";
                case "tutorial_next": return "Weiter";
                case "tutorial_finish": return "Verstanden";
                case "searching": return "Suche —";
                case "search_button": return "Suchen";
                case "search_hint": return "Nick eingeben...";
                case "ready_search": return "Bereit zur Suche";
                case "start_note": return "Gib einen Nickname des ausgewählten Hotels ein, um die Informationen aufzurufen.";
                case "type_nick_toast": return "Gib einen Hotel-Nickname ein, um zu suchen.";
                case "same_profile_loading": return "Dieses Profil wird bereits geladen.";
                case "wait_refresh": return "Warte %ss, um dieses Profil erneut zu aktualisieren.";
                case "updating_profile": return "Profil wird aktualisiert...";
                case "searching_profile": return "Profil wird gesucht...";
                case "not_found_body": return "Ich konnte im ausgewählten Hotel kein aktuelles Konto mit dem Nickname %s finden.";
                case "settings": return "Einstellungen";
                case "search_hotel": return "Suchhotel";
                case "hotel_changed": return "Hotel und Sprache aktualisiert.";
                case "adfree_title": return "Werbefreier Zugriff";
                case "buy_no_ads": return "Hol dir die werbefreie Version!";
                case "premium_title": return "Premium werden";
                case "premium_remove_ads": return "Alle Werbung entfernen";
                case "premium_pay_once": return "EINMAL ZAHLEN";
                case "remove_ads_purchased": return "Werbefreie Version aktiviert.";
                case "purchase_pending": return "Zahlung ausstehend. Die werbefreie Version wird nach Bestätigung aktiviert.";
                case "purchase_loading": return "Kauf wird vorbereitet. Versuche es in einigen Sekunden erneut.";
                case "purchase_error": return "Der Kauf konnte jetzt nicht geöffnet werden.";
                case "adfree_msg_add": return "Du hast noch %s ohne Werbung. Möchtest du ein Video ansehen, um weitere 30 Minuten hinzuzufügen? Das Maximum beträgt 4 Stunden.";
                case "adfree_msg_new": return "Möchtest du ein Video ansehen, um 30 Minuten ohne Werbung bei der Profilsuche freizuschalten?";
                case "time_left": return "Verbleibende Zeit";
                case "cancel": return "Abbrechen";
                case "watch_video": return "Video ansehen";
                case "cannot_show_video": return "Das Video konnte jetzt nicht angezeigt werden.";
                case "limit_24h": return "Du hast bereits das werbefreie Limit von 4 Stunden erreicht.";
                case "video_loading": return "Das Video wird noch geladen. Versuche es in einigen Sekunden erneut.";
                case "adfree_granted": return "30 Minuten ohne Werbung freigeschaltet.";
                case "disclaimer1": return "Diese Anwendung ist weder mit Sulake Corporation Oy oder ihren verbundenen Unternehmen verbunden noch von ihnen unterstützt, gesponsert oder ausdrücklich genehmigt.";
                case "error_search_profile": return "Fehler bei der Profilsuche.";
                case "no_profile_found": return "Kein Profil gefunden";
                case "generic_loading": return "Wird geladen...";
                case "no_friend_found": return "Keine Freunde gefunden.";
                case "no_removed_friend_found": return "Keine entfernten Freunde gefunden.";
                case "light_theme": return "Helles Design";
                case "dark_theme": return "Dunkles Design";
                case "clear_app_cache": return "Cache löschen";
                case "app_cache_cleared": return "App-Cache gelöscht.";
                case "session_profiles": return "Profile in Sitzung";
                case "app_cache": return "App-Cache";
                case "tap_to_continue": return "Tippe irgendwo, um fortzufahren";
                case "profile": return "Profil";
                case "badges": return "Abzeichen";
                case "show_achievements": return "Erfolge anzeigen";
                case "hide_achievements": return "Erfolge ausblenden";
                case "no_badges_found": return "Keine Abzeichen gefunden.";
                case "obtained": return "Erhalten";
                case "achievements": return "Erfolge";
                case "total_owners": return "Besitzer";
                case "suggestions": return "Vorschläge";
                case "loading_suggestions": return "Vorschläge werden geladen...";
                case "old_nick": return "Alter Nick";
                case "changed_at": return "Geändert am";
                case "old_nick_suggestions_title": return "Dieser Nickname wurde offenbar früher verwendet von:";
                case "no_old_nick_suggestions": return "Ich konnte auch keine aktuellen Konten finden, die diesen Nickname verwendet haben.";
                case "favorites": return "Favoriten";
                case "no_favorites": return "Noch keine favorisierten Profile.";
                case "favorite_currently_online": return "Gerade online!";
                case "notify_favorite_online": return "Benachrichtigen, wenn ein Favorit online geht";
                case "favorite_online_banner": return "%s ist gerade online gegangen!";
                case "favorite_added": return "Zu Favoriten hinzugefügt.";
                case "favorite_removed": return "Aus Favoriten entfernt.";
                case "favorite_limit_reached": return "Favoritenlimit erreicht: %s.";
                case "open_full_profile": return "Vollständiges Profil öffnen";
                case "hide_badges": return "Erfolge ausblenden";
                case "time_ago": return "vor %s %s";
                case "ago_second": return "Sekunde";
                case "ago_seconds": return "Sekunden";
                case "ago_minute": return "Minute";
                case "ago_minutes": return "Minuten";
                case "ago_hour": return "Stunde";
                case "ago_hours": return "Stunden";
                case "ago_day": return "Tag";
                case "ago_days": return "Tagen";
                case "ago_week": return "Woche";
                case "ago_weeks": return "Wochen";
                case "ago_month": return "Monat";
                case "ago_months": return "Monaten";
                case "ago_year": return "Jahr";
                case "ago_years": return "Jahren";
                case "visual_editor": return "Outfit-Editor";
                case "saved_visuals": return "Gespeicherte Looks";
                case "no_saved_visuals": return "Noch keine Looks gespeichert.";
                case "visual_saved": return "Look gespeichert.";
                case "visual_already_saved": return "Dieser Look ist bereits gespeichert.";
                case "visual_saved_limit": return "Du hast bereits %s Looks gespeichert. Entferne einen, bevor du einen weiteren speicherst.";
                case "visual_removed": return "Look entfernt.";
                case "visual_save_limit": return "Limit gespeicherter Looks erreicht: %s.";
                case "type_nick": return "Nick eingeben...";
                case "male": return "Männlich";
                case "female": return "Weiblich";
                case "loading_visuals": return "Editor wird geladen...";
                case "cannot_load_visuals": return "Editor konnte nicht geladen werden.";
                case "not_found_simple": return "Profil nicht gefunden.";
                case "no_items_found": return "Keine Teile gefunden.";
                case "remove_item": return "Entfernen";
                case "available_colors": return "Verfügbare Farben";
                case "cat_hair": return "Haare";
                case "cat_head": return "Haut";
                case "cat_shirts": return "Oberteile";
                case "cat_pants": return "Hosen";
                case "cat_shoes": return "Schuhe";
                case "cat_hats": return "Hüte";
                case "cat_accessories": return "Accessoires";
                case "cat_face": return "Gesicht";
                case "cat_coats": return "Jacken";
                case "cat_prints": return "Muster";
            }
        }
        else if ("fr".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Tirez pour actualiser les favoris";
                case "favorites_release": return "Relâchez pour actualiser les favoris";
                case "favorites_updating": return "Actualisation des favoris...";
                case "favorites_updated": return "Favoris actualisés.";
                case "private": return "Privé";
                case "banned": return "Banni";
                case "status": return "Statut";
                case "online": return "En ligne";
                case "offline": return "Hors ligne";
                case "last_login": return "Dernière connexion";
                case "creation": return "Création";
                case "friends": return "Amis";
                case "removed": return "Supprimés";
                case "rooms": return "Apparts";
                case "groups": return "Groupes";
                case "photos": return "Photos";
                case "stars": return "Étoiles";
                case "level": return "Niveau";
                case "previous": return "Anciens";
                case "previous_names": return "Anciens noms";
                case "previous_mottos": return "Anciennes missions";
                case "previous_styles": return "Anciens looks";
                case "user_photos": return "Photos de l'utilisateur";
                case "selected_badges": return "Badges sélectionnés";
                case "profile_history": return "Historique des profils";
                case "no_history": return "Aucun profil ouvert pour le moment.";
                case "clear_history": return "Effacer l'historique";
                case "history_cleared": return "Historique effacé.";
                case "loading_details": return "Chargement des détails du profil...";
                case "loading_history": return "Chargement de l'historique...";
                case "loading_styles_friends": return "Chargement des looks et amis...";
                case "loading_rooms_groups": return "Chargement des apparts et groupes...";
                case "loading_clothes": return "Chargement des vêtements...";
                case "cannot_load_clothes": return "Impossible de charger les éléments.";
                case "no_clothes_found": return "Aucun élément trouvé";
                case "current_look": return "Look actuel";
                case "looks": return "Looks";
                case "liked_by": return "Aimé par";
                case "no_description": return "Aucune description.";
                case "name": return "Nom";
                case "description": return "Description";
                case "created": return "Créé";
                case "code": return "Code";
                case "owner": return "Propriétaire";
                case "room": return "Appart";
                case "date": return "Date";
                case "likes": return "J'aime";
                case "page_of": return "Page %s sur %s";
                case "no_rooms_found": return "Aucun appart trouvé.";
                case "close": return "Fermer";
                case "item": return "Élément";
                case "tutorial_settings_title": return "Changer d'hôtel";
                case "tutorial_settings_body": return "Touchez l'engrenage pour choisir l'hôtel et changer la langue de l'app.";
                case "tutorial_search_title": return "Rechercher un profil";
                case "tutorial_search_body": return "Saisissez un pseudo puis touchez Rechercher pour consulter les données publiques.";
                case "tutorial_history_title": return "Historique";
                case "tutorial_history_body": return "Utilisez le bouton d'historique pour revenir vite aux profils ouverts.";
                case "tutorial_next": return "Suivant";
                case "tutorial_finish": return "Compris";
                case "searching": return "Recherche —";
                case "search_button": return "Rechercher";
                case "search_hint": return "Saisissez un pseudo...";
                case "ready_search": return "Prêt à rechercher";
                case "start_note": return "Saisissez un pseudo de l'hôtel sélectionné pour consulter les informations.";
                case "type_nick_toast": return "Saisissez un pseudo de l'hôtel pour rechercher.";
                case "same_profile_loading": return "Ce profil est déjà en cours de chargement.";
                case "wait_refresh": return "Attendez %ss avant d'actualiser ce profil à nouveau.";
                case "updating_profile": return "Mise à jour du profil...";
                case "searching_profile": return "Recherche du profil...";
                case "not_found_body": return "Je n'ai trouvé aucun compte actuel avec le pseudo %s dans l'hôtel sélectionné.";
                case "settings": return "Paramètres";
                case "search_hotel": return "Hôtel de recherche";
                case "hotel_changed": return "Hôtel et langue mis à jour.";
                case "adfree_title": return "Accès sans publicité";
                case "buy_no_ads": return "Obtenez la version sans publicité !";
                case "premium_title": return "Devenez Premium";
                case "premium_remove_ads": return "Supprimer toutes les publicités";
                case "premium_pay_once": return "PAYEZ UNE FOIS";
                case "remove_ads_purchased": return "Version sans publicité activée.";
                case "purchase_pending": return "Paiement en attente. La version sans publicité sera activée après confirmation.";
                case "purchase_loading": return "Préparation de l’achat. Réessayez dans quelques secondes.";
                case "purchase_error": return "Impossible d’ouvrir l’achat maintenant.";
                case "adfree_msg_add": return "Il vous reste encore %s sans publicité. Voulez-vous regarder une vidéo pour ajouter 30 minutes supplémentaires ? La limite maximale est de 4 heures.";
                case "adfree_msg_new": return "Voulez-vous regarder une vidéo pour débloquer 30 minutes sans publicité lors de la recherche de profils ?";
                case "time_left": return "Temps restant";
                case "cancel": return "Annuler";
                case "watch_video": return "Voir la vidéo";
                case "cannot_show_video": return "Impossible d'afficher la vidéo pour le moment.";
                case "limit_24h": return "Vous avez déjà atteint la limite de 4 heures sans publicité.";
                case "video_loading": return "La vidéo est encore en cours de chargement. Réessayez dans quelques secondes.";
                case "adfree_granted": return "30 minutes sans publicité débloquées.";
                case "disclaimer1": return "Cette application n'est ni affiliée, ni approuvée, ni sponsorisée, ni spécifiquement autorisée par Sulake Corporation Oy ou ses sociétés affiliées.";
                case "error_search_profile": return "Échec de la recherche du profil.";
                case "no_profile_found": return "Aucun profil trouvé";
                case "generic_loading": return "Chargement...";
                case "no_friend_found": return "Aucun ami trouvé.";
                case "no_removed_friend_found": return "Aucun ami supprimé trouvé.";
                case "light_theme": return "Thème clair";
                case "dark_theme": return "Thème sombre";
                case "clear_app_cache": return "Vider le cache";
                case "app_cache_cleared": return "Cache de l’app vidé.";
                case "session_profiles": return "Profils dans la session";
                case "app_cache": return "Cache de l’app";
                case "tap_to_continue": return "Touchez n’importe où pour continuer";
                case "profile": return "Profil";
                case "badges": return "Badges";
                case "show_achievements": return "Afficher les succès";
                case "hide_achievements": return "Masquer les succès";
                case "no_badges_found": return "Aucun badge trouvé.";
                case "obtained": return "Obtenu";
                case "achievements": return "Succès";
                case "total_owners": return "Propriétaires";
                case "suggestions": return "Suggestions";
                case "loading_suggestions": return "Chargement des suggestions...";
                case "old_nick": return "Ancien pseudo";
                case "changed_at": return "Modifié le";
                case "old_nick_suggestions_title": return "Ce pseudo semble avoir été utilisé auparavant par :";
                case "no_old_nick_suggestions": return "Je n'ai pas non plus trouvé de comptes actuels ayant utilisé ce pseudo.";
                case "favorites": return "Favoris";
                case "no_favorites": return "Aucun profil favori pour le moment.";
                case "favorite_currently_online": return "Actuellement en ligne !";
                case "notify_favorite_online": return "Notifier quand un favori passe en ligne";
                case "favorite_online_banner": return "%s vient de se connecter !";
                case "favorite_added": return "Ajouté aux favoris.";
                case "favorite_removed": return "Retiré des favoris.";
                case "favorite_limit_reached": return "Limite de favoris atteinte : %s.";
                case "open_full_profile": return "Ouvrir le profil complet";
                case "hide_badges": return "Masquer les succès";
                case "time_ago": return "il y a %s %s";
                case "ago_second": return "seconde";
                case "ago_seconds": return "secondes";
                case "ago_minute": return "minute";
                case "ago_minutes": return "minutes";
                case "ago_hour": return "heure";
                case "ago_hours": return "heures";
                case "ago_day": return "jour";
                case "ago_days": return "jours";
                case "ago_week": return "semaine";
                case "ago_weeks": return "semaines";
                case "ago_month": return "mois";
                case "ago_months": return "mois";
                case "ago_year": return "an";
                case "ago_years": return "ans";
                case "visual_editor": return "Essayeur de looks";
                case "saved_visuals": return "Looks enregistrés";
                case "no_saved_visuals": return "Aucun look enregistré.";
                case "visual_saved": return "Look enregistré.";
                case "visual_already_saved": return "Ce look est déjà enregistré.";
                case "visual_saved_limit": return "Vous avez déjà %s looks enregistrés. Supprimez-en un avant d’en enregistrer un autre.";
                case "visual_removed": return "Look supprimé.";
                case "visual_save_limit": return "Limite de looks enregistrés : %s.";
                case "type_nick": return "Saisissez un pseudo...";
                case "male": return "Masculin";
                case "female": return "Féminin";
                case "loading_visuals": return "Chargement de l'essayeur...";
                case "cannot_load_visuals": return "Impossible de charger l'essayeur.";
                case "not_found_simple": return "Profil introuvable.";
                case "no_items_found": return "Aucun élément trouvé.";
                case "remove_item": return "Retirer";
                case "available_colors": return "Couleurs disponibles";
                case "cat_hair": return "Cheveux";
                case "cat_head": return "Peau";
                case "cat_shirts": return "Hauts";
                case "cat_pants": return "Pantalons";
                case "cat_shoes": return "Chaussures";
                case "cat_hats": return "Chapeaux";
                case "cat_accessories": return "Accessoires";
                case "cat_face": return "Visage";
                case "cat_coats": return "Vestes";
                case "cat_prints": return "Imprimés";
            }
        }
        else if ("fi".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Päivitä suosikit vetämällä";
                case "favorites_release": return "Vapauta päivittääksesi suosikit";
                case "favorites_updating": return "Päivitetään suosikkeja...";
                case "favorites_updated": return "Suosikit päivitetty.";
                case "private": return "Yksityinen";
                case "banned": return "Porttikielto";
                case "status": return "Tila";
                case "online": return "Paikalla";
                case "offline": return "Poissa";
                case "last_login": return "Viimeisin kirjautuminen";
                case "creation": return "Luotu";
                case "friends": return "Kaverit";
                case "removed": return "Poistetut";
                case "rooms": return "Huoneet";
                case "groups": return "Ryhmät";
                case "photos": return "Kuvat";
                case "stars": return "Tähdet";
                case "level": return "Taso";
                case "previous": return "Aiemmat";
                case "previous_names": return "Aiemmat nimet";
                case "previous_mottos": return "Aiemmat motot";
                case "previous_styles": return "Aiemmat asut";
                case "user_photos": return "Käyttäjän kuvat";
                case "selected_badges": return "Valitut merkit";
                case "profile_history": return "Profiilihistoria";
                case "no_history": return "Ei vielä avattuja profiileja.";
                case "clear_history": return "Tyhjennä historia";
                case "history_cleared": return "Historia tyhjennetty.";
                case "loading_details": return "Ladataan profiilin tietoja...";
                case "loading_history": return "Ladataan historiaa...";
                case "loading_styles_friends": return "Ladataan asuja ja kavereita...";
                case "loading_rooms_groups": return "Ladataan huoneita ja ryhmiä...";
                case "loading_clothes": return "Ladataan vaatteita...";
                case "cannot_load_clothes": return "Kohteita ei voitu ladata.";
                case "no_clothes_found": return "Kohteita ei löytynyt";
                case "current_look": return "Nykyinen asu";
                case "looks": return "Asut";
                case "liked_by": return "Tykkääjät";
                case "no_description": return "Ei kuvausta.";
                case "name": return "Nimi";
                case "description": return "Kuvaus";
                case "created": return "Luotu";
                case "code": return "Koodi";
                case "owner": return "Omistaja";
                case "room": return "Huone";
                case "date": return "Päiväys";
                case "likes": return "Tykkäykset";
                case "page_of": return "Sivu %s/%s";
                case "no_rooms_found": return "Huoneita ei löytynyt.";
                case "close": return "Sulje";
                case "item": return "Kohde";
                case "tutorial_settings_title": return "Vaihda hotelli";
                case "tutorial_settings_body": return "Napauta alareunan valikkokuvaketta valitaksesi hotellin ja sovelluksen kielen.";
                case "tutorial_search_title": return "Hae profiilia";
                case "tutorial_search_body": return "Kirjoita nimimerkki ja napauta Hae nähdäksesi julkiset tiedot.";
                case "tutorial_history_title": return "Historia";
                case "tutorial_history_body": return "Historiapainikkeella pääset nopeasti aiemmin avattuihin profiileihin.";
                case "tutorial_next": return "Seuraava";
                case "tutorial_finish": return "Selvä";
                case "searching": return "Haetaan —";
                case "search_button": return "Hae";
                case "search_hint": return "Kirjoita nick...";
                case "ready_search": return "Valmis hakuun";
                case "start_note": return "Kirjoita valitun hotellin nimimerkki tarkistaaksesi tiedot.";
                case "type_nick_toast": return "Kirjoita hotellin nimimerkki hakeaksesi.";
                case "same_profile_loading": return "Tätä profiilia ladataan jo.";
                case "wait_refresh": return "Odota %ss ennen kuin päivität tämän profiilin uudelleen.";
                case "updating_profile": return "Päivitetään profiilia...";
                case "searching_profile": return "Haetaan profiilia...";
                case "not_found_body": return "En löytänyt nykyistä tiliä nimimerkillä %s valitusta hotellista.";
                case "settings": return "Asetukset";
                case "search_hotel": return "Hakuhotelli";
                case "hotel_changed": return "Hotelli ja kieli päivitetty.";
                case "adfree_title": return "Mainokseton käyttö";
                case "buy_no_ads": return "Hanki mainokseton versio!";
                case "premium_title": return "Ryhdy Premiumiksi";
                case "premium_remove_ads": return "Poista kaikki mainokset";
                case "premium_pay_once": return "MAKSA KERRAN";
                case "remove_ads_purchased": return "Mainokseton versio aktivoitu.";
                case "purchase_pending": return "Maksu odottaa. Mainokseton versio aktivoituu vahvistuksen jälkeen.";
                case "purchase_loading": return "Ostoa valmistellaan. Yritä uudelleen muutaman sekunnin kuluttua.";
                case "purchase_error": return "Ostoa ei voitu avata juuri nyt.";
                case "adfree_msg_add": return "Sinulla on vielä %s ilman mainoksia. Haluatko katsoa videon lisätäksesi vielä 30 minuuttia? Enimmäisraja on 4 tuntia.";
                case "adfree_msg_new": return "Haluatko katsoa videon avataksesi 30 minuuttia ilman mainoksia profiileja haettaessa?";
                case "time_left": return "Aikaa jäljellä";
                case "cancel": return "Peruuta";
                case "watch_video": return "Katso video";
                case "cannot_show_video": return "Videota ei voitu näyttää juuri nyt.";
                case "limit_24h": return "Olet jo saavuttanut 4 tunnin mainoksettoman rajan.";
                case "video_loading": return "Video latautuu vielä. Yritä uudelleen muutaman sekunnin kuluttua.";
                case "adfree_granted": return "30 minuuttia ilman mainoksia avattu.";
                case "disclaimer1": return "Tämä sovellus ei ole Sulake Corporation Oy:n tai sen tytäryhtiöiden kanssa sidoksissa eikä niiden hyväksymä, sponsoroima tai erityisesti hyväksymä.";
                case "error_search_profile": return "Profiilin haku epäonnistui.";
                case "no_profile_found": return "Profiilia ei löytynyt";
                case "generic_loading": return "Ladataan...";
                case "no_friend_found": return "Kavereita ei löytynyt.";
                case "no_removed_friend_found": return "Poistettuja kavereita ei löytynyt.";
                case "light_theme": return "Vaalea teema";
                case "dark_theme": return "Tumma teema";
                case "clear_app_cache": return "Tyhjennä välimuisti";
                case "app_cache_cleared": return "Sovelluksen välimuisti tyhjennetty.";
                case "session_profiles": return "Profiileja istunnossa";
                case "app_cache": return "Sovelluksen välimuisti";
                case "tap_to_continue": return "Jatka napauttamalla mitä tahansa kohtaa";
                case "profile": return "Profiili";
                case "badges": return "Merkit";
                case "show_achievements": return "Näytä saavutukset";
                case "hide_achievements": return "Piilota saavutukset";
                case "no_badges_found": return "Merkkejä ei löytynyt.";
                case "obtained": return "Saatu";
                case "achievements": return "Saavutukset";
                case "total_owners": return "Omistajat";
                case "suggestions": return "Ehdotukset";
                case "loading_suggestions": return "Ladataan ehdotuksia...";
                case "old_nick": return "Vanha nimi";
                case "changed_at": return "Muutettu";
                case "old_nick_suggestions_title": return "Tätä nimimerkkiä näyttää käyttäneen aiemmin:";
                case "no_old_nick_suggestions": return "En myöskään löytänyt nykyisiä tilejä, jotka olisivat käyttäneet tätä nimimerkkiä.";
                case "favorites": return "Suosikit";
                case "no_favorites": return "Ei vielä suosikkiprofiileja.";
                case "favorite_currently_online": return "Tällä hetkellä paikalla!";
                case "notify_favorite_online": return "Ilmoita, kun suosikki tulee paikalle";
                case "favorite_online_banner": return "%s tuli juuri paikalle!";
                case "favorite_added": return "Lisätty suosikkeihin.";
                case "favorite_removed": return "Poistettu suosikeista.";
                case "favorite_limit_reached": return "Suosikkiraja täynnä: %s.";
                case "open_full_profile": return "Avaa koko profiili";
                case "hide_badges": return "Piilota saavutukset";
                case "time_ago": return "%s %s sitten";
                case "ago_second": return "sekunti";
                case "ago_seconds": return "sekuntia";
                case "ago_minute": return "minuutti";
                case "ago_minutes": return "minuuttia";
                case "ago_hour": return "tunti";
                case "ago_hours": return "tuntia";
                case "ago_day": return "päivä";
                case "ago_days": return "päivää";
                case "ago_week": return "viikko";
                case "ago_weeks": return "viikkoa";
                case "ago_month": return "kuukausi";
                case "ago_months": return "kuukautta";
                case "ago_year": return "vuosi";
                case "ago_years": return "vuotta";
                case "visual_editor": return "Asueditori";
                case "saved_visuals": return "Tallennetut asut";
                case "no_saved_visuals": return "Ei tallennettuja asuja.";
                case "visual_saved": return "Asu tallennettu.";
                case "visual_already_saved": return "Tämä asu on jo tallennettu.";
                case "visual_saved_limit": return "Sinulla on jo %s tallennettua asua. Poista yksi ennen uuden tallentamista.";
                case "visual_removed": return "Asu poistettu.";
                case "visual_save_limit": return "Tallennettujen asujen raja: %s.";
                case "type_nick": return "Kirjoita nick...";
                case "male": return "Mies";
                case "female": return "Nainen";
                case "loading_visuals": return "Ladataan editoria...";
                case "cannot_load_visuals": return "Editoria ei voitu ladata.";
                case "not_found_simple": return "Profiilia ei löytynyt.";
                case "no_items_found": return "Kohteita ei löytynyt.";
                case "remove_item": return "Poista";
                case "available_colors": return "Värit";
                case "cat_hair": return "Hiukset";
                case "cat_head": return "Iho";
                case "cat_shirts": return "Paidat";
                case "cat_pants": return "Housut";
                case "cat_shoes": return "Kengät";
                case "cat_hats": return "Hatut";
                case "cat_accessories": return "Asusteet";
                case "cat_face": return "Kasvot";
                case "cat_coats": return "Takit";
                case "cat_prints": return "Kuviot";
            }
        }
        else if ("it".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Trascina per aggiornare i preferiti";
                case "favorites_release": return "Rilascia per aggiornare i preferiti";
                case "favorites_updating": return "Aggiornamento preferiti...";
                case "favorites_updated": return "Preferiti aggiornati.";
                case "private": return "Privato";
                case "banned": return "Bannato";
                case "status": return "Stato";
                case "online": return "Online";
                case "offline": return "Offline";
                case "last_login": return "Ultimo accesso";
                case "creation": return "Creazione";
                case "friends": return "Amici";
                case "removed": return "Rimossi";
                case "rooms": return "Stanze";
                case "groups": return "Gruppi";
                case "photos": return "Foto";
                case "stars": return "Stelle";
                case "level": return "Livello";
                case "previous": return "Precedenti";
                case "previous_names": return "Nomi precedenti";
                case "previous_mottos": return "Missioni precedenti";
                case "previous_styles": return "Look precedenti";
                case "user_photos": return "Foto utente";
                case "selected_badges": return "Badge selezionati";
                case "profile_history": return "Cronologia profili";
                case "no_history": return "Nessun profilo aperto ancora.";
                case "clear_history": return "Cancella cronologia";
                case "history_cleared": return "Cronologia cancellata.";
                case "loading_details": return "Caricamento dettagli profilo...";
                case "loading_history": return "Caricamento cronologia...";
                case "loading_styles_friends": return "Caricamento look e amici...";
                case "loading_rooms_groups": return "Caricamento stanze e gruppi...";
                case "loading_clothes": return "Caricamento vestiti...";
                case "cannot_load_clothes": return "Impossibile caricare gli elementi.";
                case "no_clothes_found": return "Nessun elemento trovato";
                case "current_look": return "Look attuale";
                case "looks": return "Look";
                case "liked_by": return "Piaciuto a";
                case "no_description": return "Nessuna descrizione.";
                case "name": return "Nome";
                case "description": return "Descrizione";
                case "created": return "Creato";
                case "code": return "Codice";
                case "owner": return "Proprietario";
                case "room": return "Stanza";
                case "date": return "Data";
                case "likes": return "Mi piace";
                case "page_of": return "Pagina %s di %s";
                case "no_rooms_found": return "Nessuna stanza trovata.";
                case "close": return "Chiudi";
                case "item": return "Elemento";
                case "tutorial_settings_title": return "Cambia hotel";
                case "tutorial_settings_body": return "Tocca l'ingranaggio per scegliere l'hotel e cambiare la lingua dell'app.";
                case "tutorial_search_title": return "Cerca profilo";
                case "tutorial_search_body": return "Inserisci un nick e tocca Cerca per consultare dati pubblici.";
                case "tutorial_history_title": return "Cronologia";
                case "tutorial_history_body": return "Usa il pulsante cronologia per tornare rapidamente ai profili aperti.";
                case "tutorial_next": return "Avanti";
                case "tutorial_finish": return "Capito";
                case "searching": return "Ricerca —";
                case "search_button": return "Cerca";
                case "search_hint": return "Inserisci un nick...";
                case "ready_search": return "Pronto per cercare";
                case "start_note": return "Inserisci un nick dell'hotel selezionato per consultare le informazioni.";
                case "type_nick_toast": return "Inserisci un nick dell'hotel per cercare.";
                case "same_profile_loading": return "Questo profilo è già in caricamento.";
                case "wait_refresh": return "Attendi %ss prima di aggiornare di nuovo questo profilo.";
                case "updating_profile": return "Aggiornamento profilo...";
                case "searching_profile": return "Ricerca profilo...";
                case "not_found_body": return "Non ho trovato un account attuale con il nick %s nell'hotel selezionato.";
                case "settings": return "Impostazioni";
                case "search_hotel": return "Hotel di ricerca";
                case "hotel_changed": return "Hotel e lingua aggiornati.";
                case "adfree_title": return "Accesso senza annunci";
                case "buy_no_ads": return "Acquista la versione senza annunci!";
                case "premium_title": return "Diventa Premium";
                case "premium_remove_ads": return "Rimuovi tutti gli annunci";
                case "premium_pay_once": return "PAGA UNA VOLTA";
                case "remove_ads_purchased": return "Versione senza annunci attivata.";
                case "purchase_pending": return "Pagamento in sospeso. La versione senza annunci verrà attivata dopo la conferma.";
                case "purchase_loading": return "Preparazione acquisto. Riprova tra qualche secondo.";
                case "purchase_error": return "Impossibile aprire l’acquisto ora.";
                case "adfree_msg_add": return "Hai ancora %s senza annunci. Vuoi guardare un video per aggiungere altri 30 minuti? Il limite massimo è di 4 ore.";
                case "adfree_msg_new": return "Vuoi guardare un video per sbloccare 30 minuti senza annunci durante la ricerca dei profili?";
                case "time_left": return "Tempo restante";
                case "cancel": return "Annulla";
                case "watch_video": return "Guarda video";
                case "cannot_show_video": return "Non è stato possibile mostrare il video in questo momento.";
                case "limit_24h": return "Hai già raggiunto il limite di 4 ore senza annunci.";
                case "video_loading": return "Il video si sta ancora caricando. Riprova tra qualche secondo.";
                case "adfree_granted": return "30 minuti senza annunci sbloccati.";
                case "disclaimer1": return "Questa applicazione non è affiliata, approvata, sponsorizzata o specificamente approvata da Sulake Corporation Oy o dalle sue affiliate.";
                case "error_search_profile": return "Errore durante la ricerca del profilo.";
                case "no_profile_found": return "Nessun profilo trovato";
                case "generic_loading": return "Caricamento...";
                case "no_friend_found": return "Nessun amico trovato.";
                case "no_removed_friend_found": return "Nessun amico rimosso trovato.";
                case "light_theme": return "Tema chiaro";
                case "dark_theme": return "Tema scuro";
                case "clear_app_cache": return "Cancella cache";
                case "app_cache_cleared": return "Cache dell’app cancellata.";
                case "session_profiles": return "Profili nella sessione";
                case "app_cache": return "Cache dell’app";
                case "tap_to_continue": return "Tocca un punto qualsiasi per continuare";
                case "profile": return "Profilo";
                case "badges": return "Badge";
                case "show_achievements": return "Mostra risultati";
                case "hide_achievements": return "Nascondi risultati";
                case "no_badges_found": return "Nessun badge trovato.";
                case "obtained": return "Ottenuto";
                case "achievements": return "Risultati";
                case "total_owners": return "Proprietari";
                case "suggestions": return "Suggerimenti";
                case "loading_suggestions": return "Caricamento suggerimenti...";
                case "old_nick": return "Nick precedente";
                case "changed_at": return "Modificato il";
                case "old_nick_suggestions_title": return "Questo nick sembra essere stato usato prima da:";
                case "no_old_nick_suggestions": return "Non ho trovato altri account attuali che abbiano usato questo nick.";
                case "favorites": return "Preferiti";
                case "no_favorites": return "Nessun profilo preferito ancora.";
                case "favorite_currently_online": return "Attualmente online!";
                case "notify_favorite_online": return "Avvisa quando un preferito va online";
                case "favorite_online_banner": return "%s è appena andato online!";
                case "favorite_added": return "Aggiunto ai preferiti.";
                case "favorite_removed": return "Rimosso dai preferiti.";
                case "favorite_limit_reached": return "Limite preferiti raggiunto: %s.";
                case "open_full_profile": return "Apri profilo completo";
                case "hide_badges": return "Nascondi risultati";
                case "time_ago": return "%s %s fa";
                case "ago_second": return "secondo";
                case "ago_seconds": return "secondi";
                case "ago_minute": return "minuto";
                case "ago_minutes": return "minuti";
                case "ago_hour": return "ora";
                case "ago_hours": return "ore";
                case "ago_day": return "giorno";
                case "ago_days": return "giorni";
                case "ago_week": return "settimana";
                case "ago_weeks": return "settimane";
                case "ago_month": return "mese";
                case "ago_months": return "mesi";
                case "ago_year": return "anno";
                case "ago_years": return "anni";
                case "visual_editor": return "Prova look";
                case "saved_visuals": return "Look salvati";
                case "no_saved_visuals": return "Nessun look salvato.";
                case "visual_saved": return "Look salvato.";
                case "visual_already_saved": return "Questo look è già salvato.";
                case "visual_saved_limit": return "Hai già %s look salvati. Rimuovine uno prima di salvarne un altro.";
                case "visual_removed": return "Look rimosso.";
                case "visual_save_limit": return "Limite di look salvati: %s.";
                case "type_nick": return "Inserisci un nick...";
                case "male": return "Maschile";
                case "female": return "Femminile";
                case "loading_visuals": return "Caricamento prova look...";
                case "cannot_load_visuals": return "Impossibile caricare la prova look.";
                case "not_found_simple": return "Profilo non trovato.";
                case "no_items_found": return "Nessun item trovato.";
                case "remove_item": return "Rimuovi";
                case "available_colors": return "Colori disponibili";
                case "cat_hair": return "Capelli";
                case "cat_head": return "Pelle";
                case "cat_shirts": return "Maglie";
                case "cat_pants": return "Pantaloni";
                case "cat_shoes": return "Scarpe";
                case "cat_hats": return "Cappelli";
                case "cat_accessories": return "Accessori";
                case "cat_face": return "Viso";
                case "cat_coats": return "Giacche";
                case "cat_prints": return "Stampe";
            }
        }
        else if ("nl".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Trek om favorieten te vernieuwen";
                case "favorites_release": return "Laat los om favorieten te vernieuwen";
                case "favorites_updating": return "Favorieten bijwerken...";
                case "favorites_updated": return "Favorieten bijgewerkt.";
                case "private": return "Privé";
                case "banned": return "Verbannen";
                case "status": return "Status";
                case "online": return "Online";
                case "offline": return "Offline";
                case "last_login": return "Laatste login";
                case "creation": return "Aangemaakt";
                case "friends": return "Vrienden";
                case "removed": return "Verwijderd";
                case "rooms": return "Kamers";
                case "groups": return "Groepen";
                case "photos": return "Foto's";
                case "stars": return "Sterren";
                case "level": return "Level";
                case "previous": return "Vorige";
                case "previous_names": return "Vorige namen";
                case "previous_mottos": return "Vorige motto's";
                case "previous_styles": return "Vorige looks";
                case "user_photos": return "Gebruikersfoto's";
                case "selected_badges": return "Geselecteerde badges";
                case "profile_history": return "Profielgeschiedenis";
                case "no_history": return "Nog geen profielen geopend.";
                case "clear_history": return "Geschiedenis wissen";
                case "history_cleared": return "Geschiedenis gewist.";
                case "loading_details": return "Profielgegevens laden...";
                case "loading_history": return "Geschiedenis laden...";
                case "loading_styles_friends": return "Looks en vrienden laden...";
                case "loading_rooms_groups": return "Kamers en groepen laden...";
                case "loading_clothes": return "Kleding laden...";
                case "cannot_load_clothes": return "Items konden niet worden geladen.";
                case "no_clothes_found": return "Geen items gevonden";
                case "current_look": return "Huidige look";
                case "looks": return "Looks";
                case "liked_by": return "Geliked door";
                case "no_description": return "Geen beschrijving.";
                case "name": return "Naam";
                case "description": return "Beschrijving";
                case "created": return "Aangemaakt";
                case "code": return "Code";
                case "owner": return "Eigenaar";
                case "room": return "Kamer";
                case "date": return "Datum";
                case "likes": return "Likes";
                case "page_of": return "Pagina %s van %s";
                case "no_rooms_found": return "Geen kamers gevonden.";
                case "close": return "Sluiten";
                case "item": return "Item";
                case "tutorial_settings_title": return "Hotel wijzigen";
                case "tutorial_settings_body": return "Tik onderaan op het menu-icoon om het hotel en de app-taal te wijzigen.";
                case "tutorial_search_title": return "Profiel zoeken";
                case "tutorial_search_body": return "Voer een naam in en tik op Zoeken om openbare gegevens te bekijken.";
                case "tutorial_history_title": return "Geschiedenis";
                case "tutorial_history_body": return "Gebruik geschiedenis om snel terug te keren naar geopende profielen.";
                case "tutorial_next": return "Volgende";
                case "tutorial_finish": return "Begrepen";
                case "searching": return "Zoeken —";
                case "search_button": return "Zoeken";
                case "search_hint": return "Voer een nick in...";
                case "ready_search": return "Klaar om te zoeken";
                case "start_note": return "Voer een bijnaam van het geselecteerde hotel in om de informatie te bekijken.";
                case "type_nick_toast": return "Voer een hotelbijnaam in om te zoeken.";
                case "same_profile_loading": return "Dit profiel wordt al geladen.";
                case "wait_refresh": return "Wacht %ss voordat je dit profiel opnieuw ververst.";
                case "updating_profile": return "Profiel bijwerken...";
                case "searching_profile": return "Profiel zoeken...";
                case "not_found_body": return "Ik kon geen actueel account vinden met de bijnaam %s in het geselecteerde hotel.";
                case "settings": return "Instellingen";
                case "search_hotel": return "Zoekhotel";
                case "hotel_changed": return "Hotel en taal bijgewerkt.";
                case "adfree_title": return "Advertentievrije toegang";
                case "buy_no_ads": return "Koop de advertentievrije versie!";
                case "premium_title": return "Word Premium";
                case "premium_remove_ads": return "Verwijder alle advertenties";
                case "premium_pay_once": return "BETAAL ÉÉN KEER";
                case "remove_ads_purchased": return "Advertentievrije versie geactiveerd.";
                case "purchase_pending": return "Betaling in behandeling. De advertentievrije versie wordt na bevestiging geactiveerd.";
                case "purchase_loading": return "Aankoop voorbereiden. Probeer het over een paar seconden opnieuw.";
                case "purchase_error": return "Kon de aankoop nu niet openen.";
                case "adfree_msg_add": return "Je hebt nog %s zonder advertenties. Wil je een video bekijken om nog 30 minuten toe te voegen? De maximale limiet is 4 uur.";
                case "adfree_msg_new": return "Wil je een video bekijken om 30 minuten zonder advertenties vrij te schakelen tijdens het zoeken naar profielen?";
                case "time_left": return "Resterende tijd";
                case "cancel": return "Annuleren";
                case "watch_video": return "Video bekijken";
                case "cannot_show_video": return "De video kon nu niet worden weergegeven.";
                case "limit_24h": return "Je hebt de advertentievrije limiet van 4 uur al bereikt.";
                case "video_loading": return "De video wordt nog geladen. Probeer het over een paar seconden opnieuw.";
                case "adfree_granted": return "30 minuten zonder advertenties ontgrendeld.";
                case "disclaimer1": return "Deze applicatie is niet verbonden met, onderschreven door, gesponsord door of specifiek goedgekeurd door Sulake Corporation Oy of haar gelieerde ondernemingen.";
                case "error_search_profile": return "Profiel zoeken mislukt.";
                case "no_profile_found": return "Geen profiel gevonden";
                case "generic_loading": return "Laden...";
                case "no_friend_found": return "Geen vrienden gevonden.";
                case "no_removed_friend_found": return "Geen verwijderde vrienden gevonden.";
                case "light_theme": return "Licht thema";
                case "dark_theme": return "Donker thema";
                case "clear_app_cache": return "Cache wissen";
                case "app_cache_cleared": return "App-cache gewist.";
                case "session_profiles": return "Profielen in sessie";
                case "app_cache": return "App-cache";
                case "tap_to_continue": return "Tik ergens om door te gaan";
                case "profile": return "Profiel";
                case "badges": return "Badges";
                case "show_achievements": return "Prestaties tonen";
                case "hide_achievements": return "Prestaties verbergen";
                case "no_badges_found": return "Geen badges gevonden.";
                case "obtained": return "Verkregen";
                case "achievements": return "Prestaties";
                case "total_owners": return "Eigenaren";
                case "suggestions": return "Suggesties";
                case "loading_suggestions": return "Suggesties laden...";
                case "old_nick": return "Oude nick";
                case "changed_at": return "Gewijzigd op";
                case "old_nick_suggestions_title": return "Deze nick lijkt eerder gebruikt te zijn door:";
                case "no_old_nick_suggestions": return "Ik kon ook geen huidige accounts vinden die deze nick hebben gebruikt.";
                case "favorites": return "Favorieten";
                case "no_favorites": return "Nog geen favoriete profielen.";
                case "favorite_currently_online": return "Momenteel online!";
                case "notify_favorite_online": return "Melden wanneer een favoriet online komt";
                case "favorite_online_banner": return "%s is net online gekomen!";
                case "favorite_added": return "Toegevoegd aan favorieten.";
                case "favorite_removed": return "Verwijderd uit favorieten.";
                case "open_full_profile": return "Volledig profiel openen";
                case "hide_badges": return "Prestaties verbergen";
                case "time_ago": return "%s %s geleden";
                case "ago_second": return "seconde";
                case "ago_seconds": return "seconden";
                case "ago_minute": return "minuut";
                case "ago_minutes": return "minuten";
                case "ago_hour": return "uur";
                case "ago_hours": return "uur";
                case "ago_day": return "dag";
                case "ago_days": return "dagen";
                case "ago_week": return "week";
                case "ago_weeks": return "weken";
                case "ago_month": return "maand";
                case "ago_months": return "maanden";
                case "ago_year": return "jaar";
                case "ago_years": return "jaar";
                case "visual_editor": return "Look-passer";
                case "saved_visuals": return "Opgeslagen looks";
                case "no_saved_visuals": return "Nog geen opgeslagen looks.";
                case "visual_saved": return "Look opgeslagen.";
                case "visual_already_saved": return "Deze look is al opgeslagen.";
                case "visual_saved_limit": return "Je hebt al %s opgeslagen looks. Verwijder er een voordat je een andere opslaat.";
                case "visual_removed": return "Look verwijderd.";
                case "visual_save_limit": return "Limiet opgeslagen looks bereikt: %s.";
                case "type_nick": return "Voer een nick in...";
                case "male": return "Man";
                case "female": return "Vrouw";
                case "loading_visuals": return "Look-passer laden...";
                case "cannot_load_visuals": return "Kon de look-passer niet laden.";
                case "not_found_simple": return "Profiel niet gevonden.";
                case "no_items_found": return "Geen items gevonden.";
                case "remove_item": return "Verwijder";
                case "available_colors": return "Beschikbare kleuren";
                case "cat_hair": return "Haar";
                case "cat_head": return "Huid";
                case "cat_shirts": return "Shirts";
                case "cat_pants": return "Broeken";
                case "cat_shoes": return "Schoenen";
                case "cat_hats": return "Hoeden";
                case "cat_accessories": return "Accessoires";
                case "cat_face": return "Gezicht";
                case "cat_coats": return "Jassen";
                case "cat_prints": return "Prints";
            }
        }
        else if ("tr".equals(lang)) {
            switch (key) {
                case "favorites_pull": return "Favorileri yenilemek için çek";
                case "favorites_release": return "Favorileri yenilemek için bırak";
                case "favorites_updating": return "Favoriler güncelleniyor...";
                case "favorites_updated": return "Favoriler güncellendi.";
                case "private": return "Gizli";
                case "banned": return "Banlı";
                case "status": return "Durum";
                case "online": return "Çevrimiçi";
                case "offline": return "Çevrimdışı";
                case "last_login": return "Son giriş";
                case "creation": return "Oluşturulma";
                case "friends": return "Arkadaşlar";
                case "removed": return "Kaldırılanlar";
                case "rooms": return "Odalar";
                case "groups": return "Gruplar";
                case "photos": return "Fotoğraflar";
                case "stars": return "Yıldızlar";
                case "level": return "Seviye";
                case "previous": return "Öncekiler";
                case "previous_names": return "Önceki adlar";
                case "previous_mottos": return "Önceki mottolar";
                case "previous_styles": return "Önceki görünümler";
                case "user_photos": return "Kullanıcı fotoğrafları";
                case "selected_badges": return "Seçili rozetler";
                case "profile_history": return "Profil geçmişi";
                case "no_history": return "Henüz açılmış profil yok.";
                case "clear_history": return "Geçmişi temizle";
                case "history_cleared": return "Geçmiş temizlendi.";
                case "loading_details": return "Profil detayları yükleniyor...";
                case "loading_history": return "Geçmiş yükleniyor...";
                case "loading_styles_friends": return "Görünümler ve arkadaşlar yükleniyor...";
                case "loading_rooms_groups": return "Odalar ve gruplar yükleniyor...";
                case "loading_clothes": return "Kıyafetler yükleniyor...";
                case "cannot_load_clothes": return "Öğeler yüklenemedi.";
                case "no_clothes_found": return "Öğe bulunamadı";
                case "current_look": return "Mevcut görünüm";
                case "looks": return "Görünümler";
                case "liked_by": return "Beğenenler";
                case "no_description": return "Açıklama yok.";
                case "name": return "Ad";
                case "description": return "Açıklama";
                case "created": return "Oluşturuldu";
                case "code": return "Kod";
                case "owner": return "Sahip";
                case "room": return "Oda";
                case "date": return "Tarih";
                case "likes": return "Beğeniler";
                case "page_of": return "Sayfa %s / %s";
                case "no_rooms_found": return "Oda bulunamadı.";
                case "close": return "Kapat";
                case "item": return "Öğe";
                case "tutorial_settings_title": return "Otel değiştir";
                case "tutorial_settings_body": return "Otel ve uygulama dilini değiştirmek için alttaki menü simgesine dokunun.";
                case "tutorial_search_title": return "Profil ara";
                case "tutorial_search_body": return "Bir nick yazıp Ara'ya dokunarak herkese açık verileri görüntüleyin.";
                case "tutorial_history_title": return "Geçmiş";
                case "tutorial_history_body": return "Açılmış profillere hızlı dönmek için geçmiş düğmesini kullanın.";
                case "tutorial_next": return "Sonraki";
                case "tutorial_finish": return "Anladım";
                case "searching": return "Aranıyor —";
                case "search_button": return "Ara";
                case "search_hint": return "Bir nick yaz...";
                case "ready_search": return "Aramaya hazır";
                case "start_note": return "Bilgileri görüntülemek için seçili otelin takma adını girin.";
                case "type_nick_toast": return "Aramak için bir otel takma adı girin.";
                case "same_profile_loading": return "Bu profil zaten yükleniyor.";
                case "wait_refresh": return "Bu profili tekrar yenilemek için %ss bekleyin.";
                case "updating_profile": return "Profil güncelleniyor...";
                case "searching_profile": return "Profil aranıyor...";
                case "not_found_body": return "Seçili otelde %s takma adına sahip güncel bir hesap bulamadım.";
                case "settings": return "Ayarlar";
                case "search_hotel": return "Arama oteli";
                case "hotel_changed": return "Otel ve dil güncellendi.";
                case "adfree_title": return "Reklamsız erişim";
                case "buy_no_ads": return "Reklamsız sürümü edinin!";
                case "premium_title": return "Premium ol";
                case "premium_remove_ads": return "Tüm reklamları kaldır";
                case "premium_pay_once": return "BİR KEZ ÖDE";
                case "remove_ads_purchased": return "Reklamsız sürüm etkinleştirildi.";
                case "purchase_pending": return "Ödeme beklemede. Reklamsız sürüm onaydan sonra etkinleşecek.";
                case "purchase_loading": return "Satın alma hazırlanıyor. Birkaç saniye sonra tekrar deneyin.";
                case "purchase_error": return "Satın alma şu anda açılamadı.";
                case "adfree_msg_add": return "Hâlâ reklamsız %s süreniz var. 30 dakika daha eklemek için bir video izlemek ister misiniz? Maksimum sınır 4 saattir.";
                case "adfree_msg_new": return "Profil ararken 30 dakika reklamsız kullanım açmak için bir video izlemek ister misiniz?";
                case "time_left": return "Kalan süre";
                case "cancel": return "İptal";
                case "watch_video": return "Videoyu izle";
                case "cannot_show_video": return "Video şu anda gösterilemedi.";
                case "limit_24h": return "4 saatlik reklamsız sınırına zaten ulaştınız.";
                case "video_loading": return "Video hâlâ yükleniyor. Birkaç saniye sonra tekrar deneyin.";
                case "adfree_granted": return "30 dakika reklamsız kullanım açıldı.";
                case "disclaimer1": return "Bu uygulama Sulake Corporation Oy veya bağlı kuruluşlarıyla ilişkili değildir; onlar tarafından onaylanmaz, desteklenmez veya özellikle onaylanmış değildir.";
                case "error_search_profile": return "Profil aranamadı.";
                case "no_profile_found": return "Profil bulunamadı";
                case "generic_loading": return "Yükleniyor...";
                case "no_friend_found": return "Arkadaş bulunamadı.";
                case "no_removed_friend_found": return "Kaldırılan arkadaş bulunamadı.";
                case "light_theme": return "Açık tema";
                case "dark_theme": return "Koyu tema";
                case "clear_app_cache": return "Önbelleği temizle";
                case "app_cache_cleared": return "Uygulama önbelleği temizlendi.";
                case "session_profiles": return "Oturumdaki profiller";
                case "app_cache": return "Uygulama önbelleği";
                case "tap_to_continue": return "Devam etmek için herhangi bir yere dokunun";
                case "profile": return "Profil";
                case "badges": return "Rozetler";
                case "show_achievements": return "Başarıları göster";
                case "hide_achievements": return "Başarıları gizle";
                case "no_badges_found": return "Rozet bulunamadı.";
                case "obtained": return "Alındı";
                case "achievements": return "Başarılar";
                case "total_owners": return "Sahip olanlar";
                case "suggestions": return "Öneriler";
                case "loading_suggestions": return "Öneriler yükleniyor...";
                case "old_nick": return "Eski nick";
                case "changed_at": return "Değiştirilme";
                case "old_nick_suggestions_title": return "Bu nick daha önce şu kişi tarafından kullanılmış görünüyor:";
                case "no_old_nick_suggestions": return "Bu nicki kullanmış güncel hesap da bulamadım.";
                case "favorites": return "Favoriler";
                case "no_favorites": return "Henüz favori profil yok.";
                case "favorite_currently_online": return "Şu anda çevrimiçi!";
                case "notify_favorite_online": return "Bir favori çevrimiçi olunca bildir";
                case "favorite_online_banner": return "%s az önce çevrimiçi oldu!";
                case "favorite_added": return "Favorilere eklendi.";
                case "favorite_removed": return "Favorilerden kaldırıldı.";
                case "favorite_limit_reached": return "Favori sınırına ulaşıldı: %s.";
                case "open_full_profile": return "Tam profili aç";
                case "hide_badges": return "Başarıları gizle";
                case "time_ago": return "%s %s önce";
                case "ago_second": return "saniye";
                case "ago_seconds": return "saniye";
                case "ago_minute": return "dakika";
                case "ago_minutes": return "dakika";
                case "ago_hour": return "saat";
                case "ago_hours": return "saat";
                case "ago_day": return "gün";
                case "ago_days": return "gün";
                case "ago_week": return "hafta";
                case "ago_weeks": return "hafta";
                case "ago_month": return "ay";
                case "ago_months": return "ay";
                case "ago_year": return "yıl";
                case "ago_years": return "yıl";
                case "visual_editor": return "Görünüm deneme";
                case "saved_visuals": return "Kayıtlı görünümler";
                case "no_saved_visuals": return "Henüz kayıtlı görünüm yok.";
                case "visual_saved": return "Görünüm kaydedildi.";
                case "visual_already_saved": return "Bu görünüm zaten kayıtlı.";
                case "visual_saved_limit": return "Zaten %s kayıtlı görünümünüz var. Başka bir tane kaydetmeden önce birini kaldırın.";
                case "visual_removed": return "Görünüm kaldırıldı.";
                case "visual_save_limit": return "Kayıtlı görünüm sınırı: %s.";
                case "type_nick": return "Bir nick yaz...";
                case "male": return "Erkek";
                case "female": return "Kadın";
                case "loading_visuals": return "Görünüm yükleniyor...";
                case "cannot_load_visuals": return "Görünüm deneme yüklenemedi.";
                case "not_found_simple": return "Profil bulunamadı.";
                case "no_items_found": return "Öğe bulunamadı.";
                case "remove_item": return "Kaldır";
                case "available_colors": return "Renkler";
                case "cat_hair": return "Saç";
                case "cat_head": return "Ten";
                case "cat_shirts": return "Üstler";
                case "cat_pants": return "Pantolon";
                case "cat_shoes": return "Ayakkabı";
                case "cat_hats": return "Şapkalar";
                case "cat_accessories": return "Aksesuarlar";
                case "cat_face": return "Yüz";
                case "cat_coats": return "Ceketler";
                case "cat_prints": return "Desenler";
            }
        }
        switch (key) {
                case "favorites_pull": return "Puxe para atualizar favoritos";
                case "favorites_release": return "Solte para atualizar favoritos";
                case "favorites_updating": return "Atualizando favoritos...";
                case "favorites_updated": return "Favoritos atualizados.";
            case "searching": return "Buscando —";
            case "search_button": return "Pesquisar";
            case "search_hint": return "Digite um nick...";
            case "ready_search": return "Pronto para buscar";
            case "start_note": return "Digite um nick do hotel selecionado para consultar as informações.";
            case "type_nick_toast": return "Digite um nick do hotel para consultar perfil...";
            case "same_profile_loading": return "Esse perfil já está sendo carregado.";
            case "wait_refresh": return "Aguarde %ss para atualizar este perfil novamente.";
            case "updating_profile": return "Atualizando perfil...";
            case "searching_profile": return "Pesquisando perfil...";
            case "not_found_body": return "Não encontrei uma conta atual com o nick %s no hotel selecionado.";
            case "settings": return "Configurações";
            case "search_hotel": return "Hotel de busca";
            case "hotel_changed": return "Hotel e idioma atualizados.";
            case "adfree_title": return "Acesso sem anúncios";
            case "buy_no_ads": return "Adquira a versão sem anúncios!";
                case "premium_title": return "Torne-se Premium";
                case "premium_remove_ads": return "Remover todos os anúncios";
                case "premium_pay_once": return "PAGUE UMA VEZ";
            case "remove_ads_purchased": return "Versão sem anúncios ativada.";
            case "purchase_pending": return "Pagamento pendente. A versão sem anúncios será ativada após a confirmação.";
            case "purchase_loading": return "Preparando compra. Tente novamente em alguns segundos.";
            case "purchase_error": return "Não foi possível abrir a compra agora.";
            case "adfree_msg_add": return "Você ainda tem %s sem anúncios. Deseja assistir um vídeo para adicionar mais 30 minutos? O limite máximo é 4 horas.";
            case "adfree_msg_new": return "Deseja assistir um vídeo para liberar 30 minutos sem anúncios ao pesquisar perfis?";
            case "time_left": return "Tempo restante";
            case "cancel": return "Cancelar";
            case "watch_video": return "Assistir vídeo";
            case "cannot_show_video": return "Não foi possível exibir o vídeo agora.";
            case "limit_24h": return "Você já atingiu o limite de 4 horas sem anúncios.";
            case "video_loading": return "O vídeo ainda está carregando. Tente novamente em alguns segundos.";
            case "adfree_granted": return "30 minutos sem anúncios liberados.";
            case "disclaimer1": return "Este aplicativo não é afiliado, endossado, patrocinado ou especificamente aprovado pela Sulake Corporation Oy ou suas afiliadas.";
            case "private": return "Privado";
            case "banned": return "Banido";
            case "status": return "Status";
            case "online": return "Online";
            case "offline": return "Offline";
            case "last_login": return "Último login";
            case "creation": return "Criação";
            case "friends": return "Amigos";
            case "removed": return "Removidos";
            case "rooms": return "Quartos";
            case "groups": return "Grupos";
            case "photos": return "Fotos";
            case "stars": return "Estrelas";
            case "level": return "Level";
            case "previous": return "Anteriores";
            case "previous_names": return "Nomes anteriores";
            case "previous_mottos": return "Missões anteriores";
            case "previous_styles": return "Visuais anteriores";
            case "user_photos": return "Fotos do usuário";
            case "selected_badges": return "Emblemas selecionados";
            case "profile_history": return "Histórico de perfis";
            case "no_history": return "Nenhum perfil aberto ainda.";
            case "clear_history": return "Limpar histórico";
            case "history_cleared": return "Histórico limpo.";
            case "loading_details": return "Carregando detalhes do perfil...";
            case "loading_history": return "Carregando histórico...";
            case "loading_styles_friends": return "Carregando visuais e amigos...";
            case "loading_rooms_groups": return "Carregando quartos e grupos...";
            case "loading_clothes": return "Carregando roupas...";
            case "cannot_load_clothes": return "Não foi possível carregar as peças.";
            case "no_clothes_found": return "Nenhuma peça encontrada";
            case "current_look": return "Visual atual";
            case "looks": return "Visuais";
            case "liked_by": return "Quem curtiu";
            case "no_description": return "Sem descrição.";
            case "name": return "Nome";
            case "description": return "Descrição";
            case "created": return "Criado";
            case "code": return "Código";
            case "owner": return "Dono";
            case "room": return "Quarto";
            case "date": return "Data";
            case "likes": return "Curtidas";
            case "page_of": return "Página %s de %s";
            case "no_rooms_found": return "Nenhum quarto encontrado.";
            case "close": return "Fechar";
            case "item": return "Peça";
            case "tutorial_settings_title": return "Trocar hotel";
            case "tutorial_settings_body": return "Toque no ícone de menu na barra inferior para escolher o hotel e alterar o idioma do app.";
            case "tutorial_search_title": return "Pesquisar perfil";
            case "tutorial_search_body": return "Digite um nick na barra de pesquisa e toque em Pesquisar para consultar os dados públicos.";
            case "tutorial_history_title": return "Histórico";
            case "tutorial_history_body": return "Use o botão de histórico para voltar rapidamente aos perfis já abertos.";
            case "tutorial_next": return "Próximo";
            case "tutorial_finish": return "Entendi";
            case "error_search_profile": return "Falha ao buscar perfil.";
            case "no_profile_found": return "Nenhum perfil encontrado";
            case "generic_loading": return "Carregando...";
            case "no_friend_found": return "Nenhum amigo encontrado.";
            case "no_removed_friend_found": return "Nenhum amigo removido encontrado.";
            case "light_theme": return "Tema claro";
            case "dark_theme": return "Tema escuro";
            case "clear_app_cache": return "Limpar cache";
            case "app_cache_cleared": return "Cache do app limpo.";
            case "session_profiles": return "Perfis na sessão";
            case "app_cache": return "Cache do app";
            case "tap_to_continue": return "Toque em qualquer lugar para continuar";
            case "profile": return "Perfil";
            case "badges": return "Emblemas";
            case "show_achievements": return "Mostrar conquistas";
            case "hide_achievements": return "Ocultar conquistas";
            case "no_badges_found": return "Nenhum emblema encontrado.";
            case "obtained": return "Obtido";
            case "achievements": return "Conquistas";
            case "total_owners": return "Possuem";
            case "suggestions": return "Sugestões";
            case "loading_suggestions": return "Carregando sugestões...";
            case "old_nick": return "Nick antigo";
            case "changed_at": return "Alterado em";
            case "old_nick_suggestions_title": return "Esse nick parece ter sido usado antes por:";
            case "no_old_nick_suggestions": return "Também não encontrei contas atuais que já usaram esse nick.";
            case "favorites": return "Favoritos";
            case "no_favorites": return "Nenhum perfil favoritado ainda.";
            case "favorite_currently_online": return "Atualmente online!";
            case "notify_favorite_online": return "Notificar quando um favorito ficar online";
            case "favorite_online_banner": return "%s acabou de ficar online!";
            case "favorite_added": return "Adicionado aos favoritos.";
            case "favorite_removed": return "Removido dos favoritos.";
            case "favorite_limit_reached": return "Limite de favoritos atingido: %s.";
            case "open_full_profile": return "Abrir perfil completo";
            case "hide_badges": return "Ocultar conquistas";
            case "time_ago": return "há %s %s";
            case "ago_second": return "segundo";
            case "ago_seconds": return "segundos";
            case "ago_minute": return "minuto";
            case "ago_minutes": return "minutos";
            case "ago_hour": return "hora";
            case "ago_hours": return "horas";
            case "ago_day": return "dia";
            case "ago_days": return "dias";
            case "ago_week": return "semana";
            case "ago_weeks": return "semanas";
            case "ago_month": return "mês";
            case "ago_months": return "meses";
            case "ago_year": return "ano";
            case "ago_years": return "anos";
            case "visual_editor": return "Provador de Visuais";
            case "saved_visuals": return "Visuais salvos";
            case "no_saved_visuals": return "Nenhum visual salvo ainda.";
            case "visual_saved": return "Visual salvo.";
            case "visual_already_saved": return "Este visual já está salvo.";
            case "visual_saved_limit": return "Você já tem %s visuais salvos. Remova um visual salvo para conseguir salvar outro.";
            case "visual_removed": return "Visual removido.";
            case "visual_save_limit": return "Limite de visuais salvos atingido: %s.";
            case "type_nick": return "Digite um nick...";
            case "male": return "Masculino";
            case "female": return "Feminino";
            case "loading_visuals": return "Carregando provador...";
            case "cannot_load_visuals": return "Não foi possível carregar o provador.";
            case "not_found_simple": return "Perfil não encontrado.";
            case "no_items_found": return "Nenhum item encontrado.";
            case "remove_item": return "Remover";
            case "available_colors": return "Cores disponíveis";
            case "cat_hair": return "Cabelo";
            case "cat_head": return "Pele";
            case "cat_shirts": return "Camisas";
            case "cat_pants": return "Calças";
            case "cat_shoes": return "Sapatos";
            case "cat_hats": return "Chapéus";
            case "cat_accessories": return "Acessórios";
            case "cat_face": return "Rosto";
            case "cat_coats": return "Casacos";
            case "cat_prints": return "Estampas";

            case "visual_item_info": return "Informações do item";
            case "loading_item_info": return "Carregando informações...";
            case "item_name": return "Nome do visual";
            case "collection": return "Coleção";
            case "image": return "Imagem";
            case "thumbnail": return "Miniatura";
        }
        return key;
    }

    private String habboApiUrl(String path) {
        if (path == null) path = "";
        if (!path.startsWith("/")) path = "/" + path;
        return "https://" + hotelDomain(currentHotelKey) + path;
    }

    private String habboImagingUrl(String path) {
        if (path == null) path = "";
        if (!path.startsWith("/")) path = "/" + path;
        return "https://" + hotelDomain(currentHotelKey) + path;
    }

    private String badgeImageUrl(String code) {
        return "https://images.habbo.com/c_images/album1584/" + enc(code) + ".png";
    }

    private void addHotelButtonRow(LinearLayout grid, Dialog dialog, String a, String b, String c) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        grid.addView(row, lp(-1, dp(46), 0, 0, 0, 8));
        addHotelButton(row, dialog, a, 0);
        addHotelButton(row, dialog, b, 1);
        addHotelButton(row, dialog, c, 2);
    }

    private void addHotelButton(LinearLayout row, Dialog dialog, String hotelKey, int pos) {
        boolean active = hotelKey.equals(currentHotelKey);
        LinearLayout btn = new LinearLayout(this);
        btn.setOrientation(LinearLayout.HORIZONTAL);
        btn.setGravity(Gravity.CENTER);
        btn.setPadding(dp(6), 0, dp(6), 0);
        btn.setBackground(active ? grad(dp(12), purple2, purple) : round(lightTheme ? Color.rgb(250,250,250) : Color.argb(18,255,255,255), dp(12), lightTheme ? Color.rgb(218,218,218) : Color.argb(28,255,255,255), 1));

        ImageView flag = new ImageView(this);
        flag.setImageDrawable(new HotelFlagDrawable(hotelKey));
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(dp(30), dp(20));
        fp.rightMargin = 0;
        btn.addView(flag, fp);

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(42), 1);
        if (pos > 0) bp.leftMargin = dp(6);
        row.addView(btn, bp);
        btn.setOnClickListener(v -> {
            currentHotelKey = normalizeHotelKey(hotelKey);
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
            dialog.dismiss();
            activeSearchToken++;
            searchInProgress = false;
            currentLoadedNick = "";
            activeRenderedProfile = null;
            resultWrap.removeAllViews();
            rebuildUiPreservingProfile();
            toast(t("hotel_changed"));
        });
    }

    private void rememberOpenedProfile(ProfileResult r) {
        if (r == null || r.name == null || r.name.trim().isEmpty()) return;
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        String key = profileIdentityKey(hotel, r.uniqueId, r.name);
        for (int i = openedProfilesHistory.size() - 1; i >= 0; i--) {
            ProfileHistoryItem item = openedProfilesHistory.get(i);
            if (profileIdentityKey(item.hotelKey, item.uniqueId, item.nick).equals(key)) openedProfilesHistory.remove(i);
        }
        openedProfilesHistory.add(0, new ProfileHistoryItem(r.name, r.figure, hotel, r.uniqueId));
        while (openedProfilesHistory.size() > 50) openedProfilesHistory.remove(openedProfilesHistory.size() - 1);
        saveOpenedProfilesHistory();
    }

    private void showOpenedProfilesHistoryDialog() {
        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(18), dp(18), dp(18), dp(18));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);

        TextView title = habboText(t("profile_history"), 22, true);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 12));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new ScrollView.LayoutParams(-1, -2));
        wrap.addView(sv, lp(-1, dp(360), 0, 0, 0, 14));

        if (openedProfilesHistory.isEmpty()) {
            list.addView(centerNote(t("no_history")));
        } else {
            for (ProfileHistoryItem item : new ArrayList<>(openedProfilesHistory)) {
                list.addView(openedProfileHistoryRow(item, dialog));
            }
        }

        TextView clear = dialogButton(t("clear_history"));
        clear.setBackground(grad(dp(14), Color.rgb(120, 36, 46), Color.rgb(210, 54, 77)));
        wrap.addView(clear, lp(-1, dp(48), 0, 0, 0, 0));
        clear.setOnClickListener(v -> {
            openedProfilesHistory.clear();
            saveOpenedProfilesHistory();
            dialog.dismiss();
            toast(t("history_cleared"));
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }
    }

    private LinearLayout openedProfileHistoryRow(ProfileHistoryItem item, Dialog dialog) {
        LinearLayout row = profileListRowBase(item, false);

        TextView remove = text("", 18, Color.WHITE, true);
        remove.setGravity(Gravity.CENTER);
        remove.setBackground(new RemoveXDrawable());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(38), dp(38));
        rp.leftMargin = dp(6);
        row.addView(remove, rp);
        remove.setOnClickListener(v -> {
            for (int i = openedProfilesHistory.size() - 1; i >= 0; i--) {
                ProfileHistoryItem h = openedProfilesHistory.get(i);
                if (favoriteKey(h).equals(favoriteKey(item))) openedProfilesHistory.remove(i);
            }
            saveOpenedProfilesHistory();
            if (dialog != null) {
                dialog.dismiss();
                showOpenedProfilesHistoryDialog();
            }
        });

        bindProfileCardOpenAndHold(row, item.nick, item.hotelKey, item.figure, item.uniqueId, () -> openProfileListItem(item, dialog));
        return row;
    }

    private void showBadgeDialog(JSONObject badge) {
        if (badge == null) return;
        String code = firstText(badge, "code", "badgeCode");
        if (code.isEmpty()) return;
        String name = firstText(badge, "name", "title");
        if (name.isEmpty()) name = code;
        String desc = firstText(badge, "description", "desc");
        if (desc.isEmpty()) desc = t("no_description");
        String created = firstText(badge, "creationTime", "createdAt", "date");
        String owners = firstText(badge, "totalOwners", "owners", "ownerCount", "count");

        final Dialog dialog = new Dialog(this);
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(14), dp(14), dp(14), dp(14));
        wrap.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(wrap);

        ImageView img = new ImageView(this);
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setPadding(dp(30), dp(24), dp(30), dp(24));
        img.setBackground(round(lightTheme ? Color.rgb(245,245,245) : Color.argb(20,255,255,255), dp(16), lightTheme ? Color.rgb(224,224,224) : Color.argb(28,255,255,255), 1));
        wrap.addView(img, lp(-1, dp(170), 0,0,0,12));
        loadImage(img, badgeImageUrl(code));

        LinearLayout infoGrid = new LinearLayout(this);
        infoGrid.setOrientation(LinearLayout.VERTICAL);
        wrap.addView(infoGrid, lp(-1, -2, 0, 0, 0, 0));
        infoGrid.addView(photoInfoCard(t("name"), name, "", ""));
        infoGrid.addView(photoInfoCard(t("description"), desc, "", ""));
        infoGrid.addView(photoInfoCard(t("obtained"), created.isEmpty() ? "—" : niceDateOnly(created), "", ""));
        if (!owners.isEmpty()) infoGrid.addView(photoInfoCard(t("total_owners"), owners, "", ""));
        infoGrid.addView(photoInfoCard(t("code"), code, "", ""));

        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(shownWindow.getAttributes());
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92f);
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            shownWindow.setAttributes(params);
        }
    }

    private boolean isSameProfileObject(JSONObject a, JSONObject b) {
        if (a == null || b == null) return false;
        if (a == b) return true;
        String aId = normalizeNickKey(firstText(a, "uniqueId", "id", "habboId"));
        String bId = normalizeNickKey(firstText(b, "uniqueId", "id", "habboId"));
        return !aId.isEmpty() && aId.equals(bId);
    }

    private boolean isSameProfileId(String expectedUniqueId, JSONObject obj) {
        String expected = normalizeNickKey(expectedUniqueId);
        if (expected.isEmpty() || obj == null) return false;
        String actual = normalizeNickKey(firstText(obj, "uniqueId", "id", "habboId"));
        return !actual.isEmpty() && expected.equals(actual);
    }

    private void pushCurrentProfileToHistory(String nextNickKey) {
        if (activeRenderedProfile == null || activeRenderedProfile.name == null || activeRenderedProfile.name.trim().isEmpty()) return;
        String currentId = normalizeNickKey(activeRenderedProfile.uniqueId);
        String currentName = normalizeNickKey(activeRenderedProfile.name);
        if (normalizeHotelKey(activeRenderedProfile.hotelKey).equals(currentHotelKey) && ((!currentId.isEmpty() && currentId.equals(nextNickKey)) || (!currentName.isEmpty() && currentName.equals(nextNickKey)))) return;
        if (!profileHistory.isEmpty()) {
            ProfileResult last = profileHistory.peekLast();
            if (sameProfile(last, activeRenderedProfile)) return;
        }
        profileHistory.addLast(copyProfileResult(activeRenderedProfile));
        while (profileHistory.size() > PROFILE_HISTORY_LIMIT) profileHistory.removeFirst();
    }

    private boolean sameProfile(ProfileResult a, ProfileResult b) {
        if (a == null || b == null) return false;
        String aId = normalizeNickKey(a.uniqueId);
        String bId = normalizeNickKey(b.uniqueId);
        if (!aId.isEmpty() && !bId.isEmpty()) return aId.equals(bId);
        return normalizeNickKey(a.name).equals(normalizeNickKey(b.name));
    }

    private ProfileResult copyProfileResult(ProfileResult src) {
        ProfileResult c = new ProfileResult();
        if (src == null) return c;
        c.searchedNick = src.searchedNick; c.uniqueId = src.uniqueId; c.name = src.name; c.motto = src.motto; c.figure = src.figure; c.memberSince = src.memberSince; c.lastAccess = src.lastAccess; c.level = src.level; c.starGems = src.starGems; c.hotelKey = src.hotelKey;
        c.online = src.online; c.privateProfile = src.privateProfile; c.banned = src.banned;
        c.habboPublic = src.habboPublic; c.dex = src.dex; c.suggest = src.suggest; c.dexProfile = src.dexProfile; c.officialProfile = src.officialProfile;
        c.previousNames = new ArrayList<>(src.previousNames); c.previousMottos = new ArrayList<>(src.previousMottos); c.previousStyles = new ArrayList<>(src.previousStyles); c.photos = new ArrayList<>(src.photos); c.friends = new ArrayList<>(src.friends); c.oldFriends = new ArrayList<>(src.oldFriends); c.rooms = new ArrayList<>(src.rooms); c.oldRooms = new ArrayList<>(src.oldRooms); c.groups = new ArrayList<>(src.groups); c.badges = new ArrayList<>(src.badges); c.badgesWithAchievements = new ArrayList<>(src.badgesWithAchievements); c.totalBadges = src.totalBadges; c.selectedBadges = new ArrayList<>(src.selectedBadges);
        c.photosNextPage = src.photosNextPage; c.stylesNextPage = src.stylesNextPage; c.photosTotal = src.photosTotal; c.stylesTotal = src.stylesTotal;
        c.photosHasMore = src.photosHasMore; c.stylesHasMore = src.stylesHasMore; c.photosLoading = false; c.stylesLoading = false;
        return c;
    }

    @Override public void onBackPressed() {
        if (searchInput != null && searchInput.hasFocus()) {
            clearSearchFocus();
            return;
        }
        if (!profileHistory.isEmpty()) {
            activeSearchToken++;
            searchInProgress = false;
            activeSearchNick = "";
            inlineProgressPct = 0;
            inlineProgressMessage = "";
            ProfileResult previous = profileHistory.removeLast();
            String previousHotel = normalizeHotelKey(previous.hotelKey);
            if (!previousHotel.isEmpty()) {
                currentHotelKey = previousHotel;
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
            }
            activeRenderedProfile = previous;
            currentLoadedNick = normalizeNickKey(previous.name);
            setSearchTextProgrammatically(previous.name == null ? "" : previous.name);
            clearSearchFocus();
            statusText.setText("");
            renderProfile(previous);
            return;
        }
        super.onBackPressed();
    }

    private ProfileResult mergeFreshIntoCachedSafely(ProfileResult cached, ProfileResult fresh) {
        if (fresh == null) return cached;
        if (cached == null) return fresh;

        String freshId = normalizeNickKey(fresh.uniqueId);
        String cachedId = normalizeNickKey(cached.uniqueId);
        if (!freshId.isEmpty() && !cachedId.isEmpty() && !freshId.equals(cachedId)) {
            return fresh;
        }

        ProfileResult merged = mergeFreshIntoCached(cached, fresh);

        // Fotos e visuais antigos são carregados por página. Não reaproveite estes blocos
        // do cache, para evitar mostrar histórico antigo antes da primeira página atual.
        merged.photos.clear();
        merged.previousStyles.clear();
        merged.photosNextPage = 0;
        merged.stylesNextPage = 0;
        merged.photosTotal = 0;
        merged.stylesTotal = 0;
        merged.photosHasMore = false;
        merged.stylesHasMore = false;
        merged.photosLoading = false;
        merged.stylesLoading = false;
        return merged;
    }

    private ProfileResult mergeFreshIntoCached(ProfileResult cached, ProfileResult fresh) {
        if (cached == null) return fresh;
        if (fresh == null) return cached;

        cached.searchedNick = pickText(fresh.searchedNick, cached.searchedNick);
        cached.uniqueId = pickText(fresh.uniqueId, cached.uniqueId);
        cached.name = pickText(fresh.name, cached.name);
        cached.motto = pickText(fresh.motto, cached.motto);
        cached.figure = pickText(fresh.figure, cached.figure);
        cached.memberSince = pickText(fresh.memberSince, cached.memberSince);
        cached.lastAccess = pickText(fresh.lastAccess, cached.lastAccess);
        cached.level = pickText(fresh.level, cached.level);
        cached.starGems = pickText(fresh.starGems, cached.starGems);
        cached.hotelKey = pickText(fresh.hotelKey, cached.hotelKey);
        cached.online = fresh.online;
        cached.privateProfile = fresh.privateProfile;
        cached.banned = fresh.banned;

        if (fresh.habboPublic != null) cached.habboPublic = fresh.habboPublic;
        if (fresh.dex != null) cached.dex = fresh.dex;
        if (fresh.suggest != null) cached.suggest = fresh.suggest;
        if (fresh.dexProfile != null) cached.dexProfile = fresh.dexProfile;
        if (fresh.officialProfile != null) cached.officialProfile = fresh.officialProfile;

        cached.previousNames = mergeLists(fresh.previousNames, cached.previousNames);
        cached.previousMottos = mergeLists(fresh.previousMottos, cached.previousMottos);
        cached.previousStyles = mergeLists(fresh.previousStyles, cached.previousStyles);
        cached.photos = mergeLists(fresh.photos, cached.photos);
        cached.friends = mergeLists(fresh.friends, cached.friends);
        cached.oldFriends = mergeLists(fresh.oldFriends, cached.oldFriends);
        cached.rooms = mergeLists(fresh.rooms, cached.rooms);
        cached.oldRooms = mergeLists(fresh.oldRooms, cached.oldRooms);
        cached.groups = mergeLists(fresh.groups, cached.groups); cached.badges = mergeLists(fresh.badges, cached.badges); cached.badgesWithAchievements = mergeLists(fresh.badgesWithAchievements, cached.badgesWithAchievements); if (!fresh.totalBadges.isEmpty()) cached.totalBadges = fresh.totalBadges;
        cached.selectedBadges = mergeLists(fresh.selectedBadges, cached.selectedBadges);
        return cached;
    }

    private String pickText(String fresh, String old) {
        if (fresh != null && !fresh.trim().isEmpty() && !"null".equalsIgnoreCase(fresh.trim())) return fresh;
        return old == null ? "" : old;
    }

    private String readFile(File file) throws IOException {
        if (file == null || !file.isFile()) return "";
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        try {
            String line;
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
        } finally {
            try { br.close(); } catch(Exception ignored) {}
        }
        return sb.toString();
    }

    private ProfileResult profileFromJson(JSONObject json) {
        // Cache em disco está desativado nesta versão; este parser mínimo existe apenas para compatibilidade de compilação.
        return null;
    }

    private TextView dialogButton(String label) {
        TextView v = habboText(label, 15, true);
        v.setGravity(Gravity.CENTER);
        v.setTextColor(Color.WHITE);
        v.setPadding(dp(12), 0, dp(12), 0);
        v.setBackground(grad(dp(14), purple2, purple));
        return v;
    }





    private void updateFavoriteOnlineAlarm() {
        try {
            if (notifyFavoriteOnline) scheduleFavoriteOnlineAlarm();
            else cancelFavoriteOnlineAlarm();
        } catch(Exception ignored) {}
    }

    private PendingIntent favoriteOnlineAlarmIntent(int flags) {
        Intent intent = new Intent(this, FavoriteOnlineReceiver.class);
        intent.setAction("com.toxic.search.FAVORITE_ONLINE_CHECK");
        return PendingIntent.getBroadcast(this, 2607, intent, flags | (Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0));
    }

    private void scheduleFavoriteOnlineAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = favoriteOnlineAlarmIntent(PendingIntent.FLAG_UPDATE_CURRENT);
        long first = System.currentTimeMillis() + 60_000L;
        am.cancel(pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP, first, 60_000L, pi);
    }

    private void cancelFavoriteOnlineAlarm() {
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = favoriteOnlineAlarmIntent(PendingIntent.FLAG_NO_CREATE);
        if (pi != null) am.cancel(pi);
    }


    private void loadFavoriteOnlineStatesFromPrefs() {
        try {
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            JSONObject obj = new JSONObject(raw == null || raw.trim().isEmpty() ? "{}" : raw);
            favoriteOnlineStates.clear();
            for (ProfileHistoryItem item : favoriteProfiles) {
                if (item == null) continue;
                String key = favoriteKey(item);
                if (obj.has(key)) favoriteOnlineStates.put(key, obj.optBoolean(key, false));
            }
        } catch(Exception ignored) {}
    }

    private int favoriteOnlineCount() {
        int count = 0;
        for (ProfileHistoryItem item : favoriteProfiles) {
            if (item == null) continue;
            if (Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)))) count++;
        }
        return Math.max(0, Math.min(MAX_FAVORITES, count));
    }

    private void updateFavoriteOnlineBadgeText() {
        int count = favoriteOnlineCount();
        for (int i = favoriteOnlineBadgeViews.size() - 1; i >= 0; i--) {
            TextView badge = favoriteOnlineBadgeViews.get(i);
            if (badge == null || badge.getParent() == null) {
                favoriteOnlineBadgeViews.remove(i);
                continue;
            }
            int bw = count >= 10 ? dp(24) : dp(18);
            ViewGroup.LayoutParams rawLp = badge.getLayoutParams();
            if (rawLp != null && rawLp.width != bw) {
                rawLp.width = bw;
                badge.setLayoutParams(rawLp);
            }
            if (count <= 0) {
                badge.setVisibility(View.GONE);
            } else {
                badge.setTextColor(Color.WHITE);
                badge.setText(String.valueOf(Math.min(MAX_FAVORITES, count)));
                badge.setVisibility(View.VISIBLE);
            }
        }
    }

    private long favoriteOnlineWatcherIntervalMs() {
        return appInForeground ? FAVORITE_ONLINE_FOREGROUND_INTERVAL_MS : FAVORITE_ONLINE_BACKGROUND_INTERVAL_MS;
    }

    private void startFavoriteOnlineWatcher() {
        if (favoriteOnlineWatcher != null) uiHandler.removeCallbacks(favoriteOnlineWatcher);
        favoriteOnlineWatcher = () -> {
            checkFavoriteOnlineNotifications();
            uiHandler.postDelayed(favoriteOnlineWatcher, favoriteOnlineWatcherIntervalMs());
        };
        uiHandler.postDelayed(favoriteOnlineWatcher, appInForeground ? 500L : favoriteOnlineWatcherIntervalMs());
    }

    private void checkFavoriteOnlineNotifications() {
        if (favoriteProfiles.isEmpty()) {
            runOnUiThread(() -> updateFavoriteOnlineBadgeText());
            return;
        }
        ArrayList<ProfileHistoryItem> snapshot = new ArrayList<>(favoriteProfiles);
        executor.execute(() -> {
            boolean changedAny = false;
            for (ProfileHistoryItem item : snapshot) {
                if (item == null) continue;
                String key = favoriteKey(item);
                FavoriteStatus st = fetchFavoriteStatus(item);
                if (st == null) continue;

                String newKey = profileIdentityKey(st.hotelKey, st.uniqueId, st.nick);
                Boolean oldStored = getStoredFavoriteOnlineState(key);
                Boolean oldMemory = favoriteOnlineStates.get(key);
                boolean hadPrevious = oldStored != null || oldMemory != null;
                boolean wasOnline = oldStored != null ? oldStored.booleanValue() : Boolean.TRUE.equals(oldMemory);

                Boolean old = favoriteOnlineStates.put(key, st.online);
                favoriteStatusCache.put(key, st);
                if (!newKey.isEmpty()) {
                    favoriteOnlineStates.put(newKey, st.online);
                    favoriteStatusCache.put(newKey, st);
                }
                setStoredFavoriteOnlineState(key, st.online);
                if (!newKey.isEmpty()) setStoredFavoriteOnlineState(newKey, st.online);
                cacheFavoriteHeadAsync(st);
                if (old == null || old.booleanValue() != st.online) changedAny = true;

                if (notifyFavoriteOnline && hadPrevious && !wasOnline && st.online && isFavoriteRecentlyOnline(st)) {
                    runOnUiThread(() -> showFavoriteOnlineSystemNotification(st));
                }
            }
            if (changedAny || !snapshot.isEmpty()) runOnUiThread(() -> updateFavoriteOnlineBadgeText());
        });
    }


    private boolean isFavoriteRecentlyOnline(FavoriteStatus st) {
        if (st == null || st.lastAccess == null || st.lastAccess.trim().isEmpty()) return false;
        long access = parseHabboTimestampMs(st.lastAccess);
        if (access <= 0L) return false;
        long age = System.currentTimeMillis() - access;
        return age >= -30_000L && age <= 3L * 60L * 1000L;
    }

    private long parseHabboTimestampMs(String raw) {
        return parseHabboTimestampMsStatic(raw);
    }

    private Boolean getStoredFavoriteOnlineState(String key) {
        if (key == null || key.isEmpty()) return null;
        try {
            String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            JSONObject obj = new JSONObject(raw == null || raw.trim().isEmpty() ? "{}" : raw);
            if (!obj.has(key)) return null;
            return obj.optBoolean(key, false);
        } catch(Exception ignored) { return null; }
    }

    private void setStoredFavoriteOnlineState(String key, boolean online) {
        if (key == null || key.isEmpty()) return;
        try {
            SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
            String raw = sp.getString(PREF_FAVORITE_ONLINE_STATES, "{}");
            JSONObject obj = new JSONObject(raw == null || raw.trim().isEmpty() ? "{}" : raw);
            obj.put(key, online);
            sp.edit().putString(PREF_FAVORITE_ONLINE_STATES, obj.toString()).apply();
        } catch(Exception ignored) {}
    }

    private FavoriteStatus fetchFavoriteStatus(ProfileHistoryItem item) {
        if (item == null) return null;
        try {
            String hotel = normalizeHotelKey(item.hotelKey);
            if (hotel.isEmpty()) hotel = "br";
            JSONObject obj = null;
            String storedId = item.uniqueId == null ? "" : item.uniqueId.trim();
            if (!storedId.isEmpty()) {
                JSONObject profile = tryJson("https://" + hotelDomain(hotel) + "/api/public/users/" + enc(storedId) + "/profile");
                obj = profile == null ? null : profile.optJSONObject("user");
                if (obj == null) obj = validProfileObject(profile);
            }
            if (obj == null && item.nick != null && !item.nick.trim().isEmpty()) {
                obj = validProfileObject(tryJson("https://" + hotelDomain(hotel) + "/api/public/users?name=" + enc(item.nick)));
            }
            if (obj == null) return null;
            FavoriteStatus st = new FavoriteStatus();
            st.nick = firstText(obj, "name", "username", "habboName");
            if (st.nick.isEmpty()) st.nick = item.nick;
            st.uniqueId = firstText(obj, "uniqueId", "id", "habboId");
            if (st.uniqueId.isEmpty()) st.uniqueId = storedId;
            st.figure = firstText(obj, "figureString", "figure", "figure_string");
            if (st.figure.isEmpty()) st.figure = item.figure;
            st.hotelKey = hotel;
            st.online = obj.optBoolean("online", optBoolAny(obj, false, "isOnline"));
            st.privateProfile = !optBoolAny(obj, true, "profileVisible", "isProfileVisible", "visible");
            st.lastAccess = firstText(obj, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
            return st;
        } catch(Exception ignored) {
            return null;
        }
    }

    private File favoriteHeadCacheDir() {
        File dir = new File(getCacheDir(), "favorite_heads");
        try { dir.mkdirs(); } catch(Exception ignored) {}
        return dir;
    }

    private File favoriteHeadCacheFile(String hotelKey, String nick) {
        return favoriteHeadCacheFile(hotelKey, nick, "");
    }

    private File favoriteHeadCacheFile(String hotelKey, String nick, String uniqueId) {
        String key = profileIdentityKey(hotelKey, uniqueId, nick);
        if (key == null || key.trim().isEmpty()) key = "unknown";
        return new File(favoriteHeadCacheDir(), Math.abs(key.hashCode()) + ".png");
    }

    private void deleteFavoriteHeadCache(String hotelKey, String nick) {
        try {
            File f = favoriteHeadCacheFile(hotelKey, nick);
            if (f.exists()) f.delete();
        } catch(Exception ignored) {}
    }

    private void cacheFavoriteHeadAsync(FavoriteStatus st) {
        if (st == null || st.nick == null || st.nick.trim().isEmpty()) return;
        executor.execute(() -> {
            try {
                Bitmap b = downloadFavoriteHeadBitmap(st);
                if (b != null) saveFavoriteHeadBitmap(st, b);
            } catch(Exception ignored) {}
        });
    }

    private Bitmap downloadFavoriteHeadBitmap(FavoriteStatus st) {
        HttpURLConnection c = null;
        try {
            if (st == null) return null;
            String url = "";
            if (st.nick != null && !st.nick.trim().isEmpty()) {
                url = avatarHeadByNameForHotel(st.nick, st.hotelKey);
            } else if (st.figure != null && !st.figure.trim().isEmpty()) {
                url = "https://" + hotelDomain(st.hotelKey) + "/habbo-imaging/avatarimage?figure=" + enc(st.figure) + "&size=m&direction=2&head_direction=2&headonly=1";
            }
            if (url.isEmpty()) return null;
            c = (HttpURLConnection)new URL(url).openConnection();
            c.setConnectTimeout(10000);
            c.setReadTimeout(15000);
            return BitmapFactory.decodeStream(c.getInputStream());
        } catch(Exception ignored) {
            return null;
        } finally {
            try { if (c != null) c.disconnect(); } catch(Exception ignored) {}
        }
    }

    private void saveFavoriteHeadBitmap(FavoriteStatus st, Bitmap bitmap) {
        if (st == null || bitmap == null) return;
        try {
            File f = favoriteHeadCacheFile(st.hotelKey, st.nick, st.uniqueId);
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch(Exception ignored) {}
    }

    private Bitmap loadFavoriteHeadFromCache(FavoriteStatus st) {
        try {
            if (st == null) return null;
            File f = favoriteHeadCacheFile(st.hotelKey, st.nick, st.uniqueId);
            if (f.exists()) return BitmapFactory.decodeFile(f.getAbsolutePath());
        } catch(Exception ignored) {}
        return null;
    }

    private Bitmap loadNotificationHeadBitmap(FavoriteStatus st) {
        try {
            if (st == null) return BitmapFactory.decodeResource(getResources(), R.drawable.pre_load_head);
            Bitmap fresh = downloadFavoriteHeadBitmap(st);
            if (fresh != null) {
                saveFavoriteHeadBitmap(st, fresh);
                return fresh;
            }
            Bitmap cached = loadFavoriteHeadFromCache(st);
            if (cached != null) return cached;
            return BitmapFactory.decodeResource(getResources(), R.drawable.pre_load_head);
        } catch(Exception ignored) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.pre_load_head);
        }
    }

    private void showFavoriteOnlineSystemNotification(FavoriteStatus st) {
        try {
            if (st == null) return;
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            if (nm == null) return;
            String channelId = "favorite_online";
            if (Build.VERSION.SDK_INT >= 26) {
                NotificationChannel ch = new NotificationChannel(channelId, t("favorites"), NotificationManager.IMPORTANCE_HIGH);
                nm.createNotificationChannel(ch);
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(this, 1207, intent, Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
            Bitmap largeIcon = loadNotificationHeadBitmap(st);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, channelId) : new Notification.Builder(this);
            b.setSmallIcon(R.drawable.notification_image)
             .setContentTitle(t("favorites"))
             .setContentText(tr("favorite_online_banner", st.nick))
             .setWhen(System.currentTimeMillis())
             .setShowWhen(true)
             .setPriority(Notification.PRIORITY_HIGH)
             .setContentIntent(pi)
             .setAutoCancel(true)
             .setStyle(new Notification.BigTextStyle().bigText(tr("favorite_online_banner", st.nick)));
            if (largeIcon != null) b.setLargeIcon(largeIcon);
            nm.notify(Math.abs(profileIdentityKey(st.hotelKey, st.uniqueId, st.nick).hashCode()), b.build());
        } catch(Exception ignored) {}
    }

    private void showFavoriteOnlineBanner(FavoriteStatus st) {
        if (screen == null || st == null) return;
        final LinearLayout banner = new LinearLayout(this);
        banner.setOrientation(LinearLayout.HORIZONTAL);
        banner.setGravity(Gravity.CENTER_VERTICAL);
        banner.setPadding(dp(12), dp(10), dp(12), dp(10));
        banner.setBackground(round(Color.argb(lightTheme ? 235 : 235, 24, 138, 85), dp(18), Color.argb(150, 120, 255, 190), 1));
        if (Build.VERSION.SDK_INT >= 21) banner.setElevation(dp(30));

        ImageView head = new ImageView(this);
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        banner.addView(head, new LinearLayout.LayoutParams(dp(46), dp(46)));
        if (st.nick != null && !st.nick.isEmpty()) loadHeadImage(head, avatarHeadByNameForHotel(st.nick, st.hotelKey));
        else if (st.figure != null && !st.figure.isEmpty()) loadHeadImage(head, avatarHead(st.figure));

        TextView msg = habboText(tr("favorite_online_banner", st.nick), 14, true);
        msg.setTextColor(Color.WHITE);
        msg.setMaxLines(2);
        msg.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.leftMargin = dp(10);
        banner.addView(msg, mp);

        FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(-1, -2, Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        bp.setMargins(dp(14), dp(18), dp(14), 0);
        banner.setTranslationY(-dp(90));
        banner.setAlpha(0f);
        screen.addView(banner, bp);
        banner.bringToFront();
        banner.animate().translationY(0).alpha(1f).setDuration(220).start();
        uiHandler.postDelayed(() -> {
            try {
                banner.animate().translationY(-dp(90)).alpha(0f).setDuration(220).withEndAction(() -> {
                    try { screen.removeView(banner); } catch(Exception ignored) {}
                }).start();
            } catch(Exception ignored) {}
        }, 4200L);
    }

    private void loadFavoriteProfiles() {
        favoriteProfiles.clear();
        String raw = getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_FAVORITES, "");
        if (raw == null || raw.trim().isEmpty()) return;
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length() && favoriteProfiles.size() < MAX_FAVORITES; i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                String nick = o.optString("nick", "").trim();
                if (nick.isEmpty()) continue;
                String hotel = normalizeHotelKey(o.optString("hotel", "br"));
                if (hotel.isEmpty()) hotel = "br";
                favoriteProfiles.add(new ProfileHistoryItem(nick, o.optString("figure", ""), hotel, o.optString("uniqueId", o.optString("id", ""))));
            }
        } catch(Exception ignored) {}
    }

    private void saveFavoriteProfiles() {
        JSONArray arr = new JSONArray();
        try {
            for (ProfileHistoryItem item : favoriteProfiles) {
                JSONObject o = new JSONObject();
                o.put("nick", item.nick);
                o.put("figure", item.figure);
                o.put("uniqueId", item.uniqueId);
                String hotel = normalizeHotelKey(item.hotelKey);
                o.put("hotel", hotel.isEmpty() ? "br" : hotel);
                arr.put(o);
            }
        } catch(Exception ignored) {}
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_FAVORITES, arr.toString()).apply();
    }

    private String favoriteKey(String hotelKey, String nick) { return normalizeHotelKey(hotelKey) + ":" + normalizeNickKey(nick); }

    private String profileIdentityKey(String hotelKey, String uniqueId, String nick) {
        String hotel = normalizeHotelKey(hotelKey);
        String id = normalizeNickKey(uniqueId);
        if (!id.isEmpty()) return hotel + ":id:" + id;
        return hotel + ":nick:" + normalizeNickKey(nick);
    }

    private String favoriteKey(ProfileHistoryItem item) {
        if (item == null) return "";
        return profileIdentityKey(item.hotelKey, item.uniqueId, item.nick);
    }

    private String favoriteKey(ProfileResult r) {
        if (r == null) return "";
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        String nick = r.name == null || r.name.trim().isEmpty() ? r.searchedNick : r.name;
        return profileIdentityKey(hotel, r.uniqueId, nick);
    }

    private void applyFavoriteRowVisualState(LinearLayout row, ProfileHistoryItem item) {
        if (row == null || item == null) return;

        FavoriteStatus st = favoriteStatusCache.get(favoriteKey(item));
        boolean online = Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)));
        boolean privateProfile = false;

        if (st != null) {
            online = st.online;
            privateProfile = st.privateProfile;
        }

        int bgColor;
        int strokeColor;
        if (privateProfile) {
            bgColor = Color.rgb(10, 10, 14);
            strokeColor = lightTheme ? Color.rgb(54, 54, 64) : Color.argb(155, 110, 110, 125);
        } else if (online) {
            bgColor = lightTheme ? Color.rgb(241, 232, 252) : Color.argb(74, 139, 52, 217);
            strokeColor = lightTheme ? Color.rgb(139, 52, 217) : Color.argb(190, 171, 77, 255);
        } else {
            bgColor = lightTheme ? Color.rgb(232, 232, 236) : Color.rgb(42, 42, 50);
            strokeColor = lightTheme ? Color.rgb(192, 192, 198) : Color.argb(82, 180, 180, 190);
        }

        row.setBackground(round(bgColor, dp(16), strokeColor, 1));
        applyFavoriteRowTextColor(row, privateProfile);

        View head = row.findViewWithTag("favorite_head");
        if (head != null) {
            head.setAlpha((privateProfile || !online) ? 0.50f : 1.0f);
        }
    }

    private void applyFavoriteRowTextColor(View view, boolean privateProfile) {
        if (view == null) return;
        if (view instanceof TextView && privateProfile) {
            ((TextView)view).setTextColor(Color.WHITE);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)view;
            for (int i = 0; i < group.getChildCount(); i++) applyFavoriteRowTextColor(group.getChildAt(i), privateProfile);
        }
    }

    private int favoriteSortRank(ProfileHistoryItem item) {
        if (item == null) return 3;
        FavoriteStatus st = favoriteStatusCache.get(favoriteKey(item));
        if (st != null && st.privateProfile) return 2;
        boolean online = st != null ? st.online : Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)));
        return online ? 0 : 1;
    }

    private boolean isFavoriteProfile(ProfileResult r) {
        String key = favoriteKey(r);
        if (key.isEmpty()) return false;
        for (ProfileHistoryItem item : favoriteProfiles) if (favoriteKey(item).equals(key)) return true;
        return false;
    }

    private void toggleFavoriteProfile(ProfileResult r) {
        if (r == null) return;
        String hotel = normalizeHotelKey(r.hotelKey);
        if (hotel.isEmpty()) hotel = currentHotelKey;
        String nick = r.name == null || r.name.trim().isEmpty() ? r.searchedNick : r.name;
        if (nick == null || nick.trim().isEmpty()) return;
        String key = favoriteKey(r);
        for (int i = favoriteProfiles.size() - 1; i >= 0; i--) {
            ProfileHistoryItem item = favoriteProfiles.get(i);
            if (favoriteKey(item).equals(key)) {
                deleteFavoriteHeadCache(item.hotelKey, item.nick);
                favoriteOnlineStates.remove(key);
                favoriteProfiles.remove(i);
                saveFavoriteProfiles();
                updateFavoriteOnlineBadgeText();
                toast(t("favorite_removed"));
                return;
            }
        }
        if (favoriteProfiles.size() >= MAX_FAVORITES) {
            toast(tr("favorite_limit_reached", MAX_FAVORITES));
            return;
        }
        favoriteProfiles.add(0, new ProfileHistoryItem(nick.trim(), r.figure, hotel, r.uniqueId));
        while (favoriteProfiles.size() > MAX_FAVORITES) favoriteProfiles.remove(favoriteProfiles.size() - 1);
        saveFavoriteProfiles();
        updateFavoriteOnlineBadgeText();
        toast(t("favorite_added"));
    }

    private void showFavoriteProfilesDialog() {
        final Dialog dialog = new Dialog(this);
        PullDispatchFrameLayout full = new PullDispatchFrameLayout(this);
        full.setBackground(makeBg());

        LinearLayout favoritesPullIndicator = favoritesPullRefreshIndicator();
        full.addView(favoritesPullIndicator, favoritesPullIndicatorLayoutParams());

        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(dp(16), dp(34), dp(16), dp(82));
        wrap.setBackgroundColor(Color.TRANSPARENT);
        full.addView(wrap, new FrameLayout.LayoutParams(-1, -1));

        addBottomNavigation(full, 2, dialog);
        dialog.setContentView(full);

        TextView title = habboText(t("favorites"), 24, true);
        title.setGravity(Gravity.CENTER);
        wrap.addView(title, lp(-1, -2, 0, 0, 0, 18));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(true);
        sv.setScrollbarFadingEnabled(false);
        tintScrollBar(sv);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        sv.addView(list, new ScrollView.LayoutParams(-1, -2));
        LinearLayout.LayoutParams svLp = new LinearLayout.LayoutParams(-1, 0, 1);
        wrap.addView(sv, svLp);

        Runnable[] render = new Runnable[1];
        render[0] = () -> {
            list.removeAllViews();
            if (favoriteProfiles.isEmpty()) {
                list.addView(centerNote(t("no_favorites")));
                return;
            }
            ArrayList<ProfileHistoryItem> sortedFavorites = new ArrayList<>(favoriteProfiles);
            Collections.sort(sortedFavorites, (a, b) -> {
                int ra = favoriteSortRank(a);
                int rb = favoriteSortRank(b);
                if (ra != rb) return Integer.compare(ra, rb);
                return String.valueOf(a.nick).compareToIgnoreCase(String.valueOf(b.nick));
            });
            for (ProfileHistoryItem item : sortedFavorites) list.addView(favoriteProfileRow(item, dialog, render[0]));
        };
        render[0].run();
        bindFavoritesPullRefresh(full, sv, wrap, favoritesPullIndicator, render[0]);

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            w.setWindowAnimations(0);
            w.setAttributes(params);
        }
    }

    private LinearLayout favoritesPullRefreshIndicator() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(16), dp(8), dp(16), dp(8));
        box.setAlpha(0f);
        box.setVisibility(View.GONE);
        box.setTranslationY(-dp(44));
        box.setBackground(round(lightTheme ? Color.WHITE : Color.rgb(34, 21, 54), dp(18), lightTheme ? Color.rgb(220,220,226) : Color.argb(58,255,255,255), 1));

        CircularPullProgressView progress = new CircularPullProgressView(this);
        progress.setProgressPct(0);
        progress.setTag("progress");
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(dp(36), dp(36));
        pp.gravity = Gravity.CENTER_HORIZONTAL;
        box.addView(progress, pp);

        TextView label = text(t("favorites_pull"), 12, lightTheme ? Color.rgb(45,45,50) : Color.WHITE, true);
        label.setGravity(Gravity.CENTER);
        label.setTag("label");
        LinearLayout.LayoutParams lpLabel = new LinearLayout.LayoutParams(-2, -2);
        lpLabel.topMargin = dp(6);
        box.addView(label, lpLabel);
        return box;
    }

    private FrameLayout.LayoutParams favoritesPullIndicatorLayoutParams() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-2, dp(82), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        params.topMargin = dp(18);
        return params;
    }

    private void bindFavoritesPullRefresh(final PullDispatchFrameLayout touchHost, final ScrollView scrollView, final View elasticView, final LinearLayout indicator, final Runnable refreshRender) {
        if (touchHost == null || scrollView == null || elasticView == null || indicator == null) return;
        final float[] startY = {0f};
        final boolean[] tracking = {false};
        final boolean[] dragging = {false};
        final boolean[] ready = {false};
        final int trigger = dp(220);
        final int maxPull = dp(190);
        final CircularPullProgressView progress = indicator.findViewWithTag("progress");
        final TextView label = indicator.findViewWithTag("label");

        touchHost.setPullTouchListener(event -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                startY[0] = event.getRawY();
                tracking[0] = scrollView.getScrollY() <= 0;
                dragging[0] = false;
                ready[0] = false;
                return;
            }

            if (action == MotionEvent.ACTION_MOVE && tracking[0]) {
                float dy = event.getRawY() - startY[0];
                if (dy <= 0f) {
                    resetFavoritesPullIndicator(elasticView, indicator, progress, label);
                    dragging[0] = false;
                    ready[0] = false;
                    return;
                }
                if (scrollView.getScrollY() > 0 && !dragging[0]) return;

                dragging[0] = true;
                float progressValue = Math.max(0f, Math.min(1f, dy / Math.max(1, trigger)));
                float elastic = elasticPullDistance(dy, trigger, maxPull);
                elasticView.setTranslationY(elastic);
                indicator.setVisibility(View.VISIBLE);
                indicator.animate().cancel();
                indicator.setAlpha(Math.max(0.15f, progressValue));
                indicator.setTranslationY(-dp(22) + (elastic * 0.40f));
                if (progress != null) progress.setProgressPct(progressValue);
                if (label != null) label.setText(progressValue >= 1f ? t("favorites_release") : t("favorites_pull"));
                ready[0] = progressValue >= 1f;
                return;
            }

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                boolean shouldRefresh = tracking[0] && dragging[0] && ready[0];
                tracking[0] = false;
                dragging[0] = false;
                ready[0] = false;
                resetFavoritesPullIndicator(elasticView, indicator, progress, label);
                if (shouldRefresh) refreshFavoritesFromPull(refreshRender);
            }
        });
    }

    private void resetFavoritesPullIndicator(View elasticView, LinearLayout indicator, CircularPullProgressView progress, TextView label) {
        if (progress != null) progress.setProgressPct(0);
        if (label != null) label.setText(t("favorites_pull"));
        if (elasticView != null) elasticView.animate().translationY(0f).setDuration(190L).start();
        if (indicator != null) indicator.animate().alpha(0f).translationY(-dp(44)).setDuration(180L).withEndAction(() -> indicator.setVisibility(View.GONE)).start();
    }

    private void refreshFavoritesFromPull(Runnable refreshRender) {
        long now = System.currentTimeMillis();
        long wait = FAVORITES_REFRESH_COOLDOWN_MS - (now - lastFavoritesPullRefreshAt);
        if (wait > 0) {
            long seconds = Math.max(1L, (wait + 999L) / 1000L);
            toast(tr("wait_refresh", seconds));
            return;
        }
        lastFavoritesPullRefreshAt = now;
        toast(t("favorites_updating"));
        executor.execute(() -> {
            ArrayList<ProfileHistoryItem> snapshot = new ArrayList<>(favoriteProfiles);
            for (ProfileHistoryItem item : snapshot) {
                try {
                    FavoriteStatus st = fetchFavoriteStatus(item);
                    if (st == null) continue;
                    String oldKey = favoriteKey(item);
                    String newKey = profileIdentityKey(st.hotelKey, st.uniqueId, st.nick);
                    cacheFavoriteHeadAsync(st);
                    favoriteOnlineStates.put(oldKey, st.online);
                    favoriteStatusCache.put(oldKey, st);
                    favoriteOnlineStates.put(newKey, st.online);
                    favoriteStatusCache.put(newKey, st);
                    setStoredFavoriteOnlineState(oldKey, st.online);
                    setStoredFavoriteOnlineState(newKey, st.online);
                } catch(Exception ignored) {}
            }
            runOnUiThread(() -> {
                if (refreshRender != null) refreshRender.run();
                updateFavoriteOnlineBadgeText();
                toast(t("favorites_updated"));
            });
        });
    }

    private LinearLayout favoriteProfileRow(ProfileHistoryItem item, Dialog dialog, Runnable refresh) {
        LinearLayout row = profileListRowBase(item, true);
        applyFavoriteRowVisualState(row, item);

        TextView remove = text("", 18, Color.WHITE, true);
        remove.setGravity(Gravity.CENTER);
        remove.setBackground(new RemoveXDrawable());
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(dp(38), dp(38));
        rp.leftMargin = dp(6);
        row.addView(remove, rp);
        remove.setOnClickListener(v -> {
            for (int i = favoriteProfiles.size() - 1; i >= 0; i--) {
                ProfileHistoryItem f = favoriteProfiles.get(i);
                if (favoriteKey(f).equals(favoriteKey(item))) {
                    deleteFavoriteHeadCache(f.hotelKey, f.nick);
                    favoriteOnlineStates.remove(favoriteKey(f));
                    favoriteStatusCache.remove(favoriteKey(f));
                    favoriteProfiles.remove(i);
                }
            }
            saveFavoriteProfiles();
            updateFavoriteOnlineBadgeText();
            if (refresh != null) refresh.run();
        });
        bindProfileCardOpenAndHold(row, item.nick, item.hotelKey, item.figure, item.uniqueId, () -> {
            if (!isCurrentProfileListItem(item)) openProfileListItem(item, dialog);
        });

        updateFavoriteOnlineRowAsync(item, refresh);
        return row;
    }

    private void updateFavoriteOnlineRowAsync(ProfileHistoryItem item, Runnable refresh) {
        if (item == null) return;
        final String key = favoriteKey(item);
        executor.execute(() -> {
            FavoriteStatus st = fetchFavoriteStatus(item);
            if (st == null) return;
            cacheFavoriteHeadAsync(st);
            FavoriteStatus oldStatus = favoriteStatusCache.put(key, st);
            Boolean old = favoriteOnlineStates.put(key, st.online);
            setStoredFavoriteOnlineState(key, st.online);
            boolean changed = old == null || old.booleanValue() != st.online || oldStatus == null || oldStatus.privateProfile != st.privateProfile;
            if (changed) {
                runOnUiThread(() -> { if (refresh != null) refresh.run(); });
            }
        });
    }

    private boolean isCurrentProfileListItem(ProfileHistoryItem item) {
        if (item == null) return false;
        String itemHotel = normalizeHotelKey(item.hotelKey);
        String currentHotel = activeRenderedProfile != null ? normalizeHotelKey(activeRenderedProfile.hotelKey) : normalizeHotelKey(currentHotelKey);
        if (!itemHotel.equals(currentHotel)) return false;

        String currentId = activeRenderedProfile != null ? normalizeNickKey(activeRenderedProfile.uniqueId) : "";
        String itemId = normalizeNickKey(item.uniqueId);
        if (!currentId.isEmpty() && !itemId.isEmpty()) return currentId.equals(itemId);

        String currentNick = activeRenderedProfile != null && activeRenderedProfile.name != null && !activeRenderedProfile.name.trim().isEmpty() ? activeRenderedProfile.name : currentLoadedNick;
        return !currentNick.isEmpty() && normalizeNickKey(currentNick).equals(normalizeNickKey(item.nick));
    }

    private void openProfileListItem(ProfileHistoryItem item, Dialog dialog) {
        if (item == null) return;
        if (dialog != null) dialog.dismiss();
        currentHotelKey = normalizeHotelKey(item.hotelKey);
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        currentLoadedNick = "";
        activeSearchToken++;
        searchInProgress = false;
        rebuildUiPreservingProfile();
        openProfileReference(item.nick, item.uniqueId, item.figure, currentHotelKey);
    }

    private LinearLayout profileListRowBase(ProfileHistoryItem item) {
        return profileListRowBase(item, true);
    }

    private LinearLayout profileListRowBase(ProfileHistoryItem item, boolean showOnlineState) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(8), dp(10), dp(8));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(16), lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255), 1));
        row.setLayoutParams(lp(-1, dp(72), 0, 0, 0, 8));

        ImageView head = new ImageView(this);
        head.setTag("favorite_head");
        head.setScaleType(ImageView.ScaleType.FIT_CENTER);
        row.addView(head, new LinearLayout.LayoutParams(dp(54), dp(56)));
        if (showOnlineState) loadHeadImage(head, avatarHeadByNameForHotel(item.nick, item.hotelKey));
        else loadHeadImageForKnownProfile(head, item.figure, item.uniqueId, item.nick, item.hotelKey);


        LinearLayout mid = new LinearLayout(this);
        mid.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(0, -2, 1);
        mp.leftMargin = dp(10);
        row.addView(mid, mp);
        TextView name = habboText(item.nick, 16, true);
        name.setMaxLines(1);
        name.setEllipsize(TextUtils.TruncateAt.END);

        LinearLayout nameRow = new LinearLayout(this);
        nameRow.setOrientation(LinearLayout.HORIZONTAL);
        nameRow.setGravity(Gravity.CENTER_VERTICAL);
        nameRow.addView(name, new LinearLayout.LayoutParams(-2, -2));
        if (showOnlineState && Boolean.TRUE.equals(favoriteOnlineStates.get(favoriteKey(item)))) {
            TextView online = text(t("favorite_currently_online"), 12, purple, true);
            online.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams op = new LinearLayout.LayoutParams(-2, -2);
            op.leftMargin = dp(8);
            nameRow.addView(online, op);
        }
        mid.addView(nameRow);

        LinearLayout hotelLine = new LinearLayout(this);
        hotelLine.setGravity(Gravity.CENTER_VERTICAL);
        ImageView flag = new ImageView(this);
        flag.setImageDrawable(new HotelFlagDrawable(item.hotelKey));
        hotelLine.addView(flag, new LinearLayout.LayoutParams(dp(24), dp(16)));
        mid.addView(hotelLine, new LinearLayout.LayoutParams(-1, -2));
        return row;
    }


    private void bindProfileHeadPreviewHold(final View target, final String nick, final String hotelKey, final String fallbackFigure) {
        bindProfileCardOpenAndHold(target, nick, hotelKey, fallbackFigure, "", null);
    }

    private void bindProfileCardOpenAndHold(final View target, final String nick, final String hotelKey, final String fallbackFigure, final Runnable openAction) {
        bindProfileCardOpenAndHold(target, nick, hotelKey, fallbackFigure, "", openAction);
    }

    private void bindProfileCardOpenAndHold(final View target, final String nick, final String hotelKey, final String fallbackFigure, final String uniqueId, final Runnable openAction) {
        if (target == null) return;
        target.setClickable(true);
        final Runnable[] holdTask = new Runnable[1];
        final boolean[] fired = {false};
        final boolean[] movedBeyondClick = {false};
        final float[] downX = {0f};
        final float[] downY = {0f};
        target.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN) {
                downX[0] = event.getX();
                downY[0] = event.getY();
                fired[0] = false;
                movedBeyondClick[0] = false;
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                holdTask[0] = () -> {
                    fired[0] = true;
                    showMiniProfilePreviewDialog(nick, hotelKey, fallbackFigure, uniqueId);
                };
                uiHandler.postDelayed(holdTask[0], 500L);
                return true;
            }
            if (action == MotionEvent.ACTION_MOVE) {
                float dx = Math.abs(event.getX() - downX[0]);
                float dy = Math.abs(event.getY() - downY[0]);
                if (dx > dp(12) || dy > dp(12)) {
                    movedBeyondClick[0] = true;
                    if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                }
                return true;
            }
            if (action == MotionEvent.ACTION_UP) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                if (!fired[0] && !movedBeyondClick[0] && openAction != null) openAction.run();
                return true;
            }
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
                if (holdTask[0] != null) uiHandler.removeCallbacks(holdTask[0]);
                return true;
            }
            return true;
        });
    }

    private void showMiniProfilePreviewDialog(final String nick, final String hotelKey, final String fallbackFigure, final String uniqueId) {
        final Dialog dialog = new Dialog(this);
        LinearLayout rootDialog = new LinearLayout(this);
        rootDialog.setOrientation(LinearLayout.VERTICAL);
        rootDialog.setPadding(dp(18), dp(18), dp(18), dp(18));
        rootDialog.setBackground(round(dialogFillColor(), dp(22), dialogStrokeColor(), 1));
        dialog.setContentView(rootDialog);

        FrameLayout avatarWrap = new FrameLayout(this);
        avatarWrap.setPadding(dp(8), dp(2), dp(8), dp(2));
        rootDialog.addView(avatarWrap, lp(-1, dp(190), 0, 0, 0, 10));

        ImageView avatar = new ImageView(this);
        avatar.setAdjustViewBounds(true);
        avatar.setScaleType(ImageView.ScaleType.FIT_CENTER);
        avatar.setPadding(dp(44), 0, dp(44), 0);
        avatarWrap.addView(avatar, new FrameLayout.LayoutParams(-1, -1));
        String initialFigure = fallbackFigure == null ? "" : fallbackFigure.trim();
        if (!initialFigure.isEmpty()) loadAvatarImage(avatar, avatarFull(initialFigure, 2));
        else loadAvatarImage(avatar, avatarHeadByNameForHotel(nick, hotelKey));

        TextView favoriteBtn = text("", 22, Color.WHITE, true);
        favoriteBtn.setGravity(Gravity.CENTER);
        favoriteBtn.setPadding(0, 0, 0, 0);
        ProfileResult initialProfile = miniProfileResult(nick, initialFigure, hotelKey, uniqueId);
        favoriteBtn.setBackground(new FavoriteStarDrawable(isFavoriteProfile(initialProfile)));
        FrameLayout.LayoutParams favLp = new FrameLayout.LayoutParams(dp(42), dp(42), Gravity.TOP | Gravity.RIGHT);
        favLp.topMargin = dp(8);
        favLp.rightMargin = dp(8);
        avatarWrap.addView(favoriteBtn, favLp);

        TextView name = habboText(nick == null || nick.trim().isEmpty() ? t("profile") : nick.trim(), 24, true);
        name.setGravity(Gravity.CENTER);
        rootDialog.addView(name, lp(-1, -2, 0, 0, 0, 8));

        ProgressBar miniLoader = new ProgressBar(this);
        miniLoader.setIndeterminate(true);
        if (Build.VERSION.SDK_INT >= 21) miniLoader.setIndeterminateTintList(ColorStateList.valueOf(purple));
        LinearLayout loaderLine = new LinearLayout(this);
        loaderLine.setGravity(Gravity.CENTER);
        loaderLine.addView(miniLoader, new LinearLayout.LayoutParams(dp(30), dp(30)));
        rootDialog.addView(loaderLine, lp(-1, dp(38), 0, 0, 0, 10));

        TextView motto = habboText("", 15, false);
        motto.setGravity(Gravity.CENTER);
        motto.setTextColor(lightTheme ? Color.rgb(70,70,70) : Color.argb(220,255,255,255));
        motto.setMaxLines(3);
        motto.setEllipsize(TextUtils.TruncateAt.END);
        motto.setVisibility(View.GONE);
        rootDialog.addView(motto, lp(-1, -2, 0, 0, 0, 10));

        LinearLayout badges = new LinearLayout(this);
        badges.setGravity(Gravity.CENTER);
        badges.setOrientation(LinearLayout.HORIZONTAL);
        rootDialog.addView(badges, lp(-1, -2, 0, 0, 0, 10));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);
        rootDialog.addView(stats, lp(-1, -2, 0, 0, 0, 12));

        stats.addView(miniStatRow("status_offline", t("status"), "—", "", false));
        stats.addView(miniStatRow("clock", t("last_login"), "—", "", false));
        stats.addView(miniStatRow("calendar", t("creation"), "—", "", false));

        final MiniProfilePreview[] loaded = new MiniProfilePreview[1];

        TextView openFull = dialogButton(t("open_full_profile"));
        openFull.setBackground(grad(dp(14), purple2, purple));
        rootDialog.addView(openFull, lp(-1, dp(48), 0, 0, 0, 0));
        openFull.setOnClickListener(v -> {
            dialog.dismiss();
            MiniProfilePreview data = loaded[0];
            String openNick = data != null && data.nick != null && !data.nick.trim().isEmpty() ? data.nick : nick;
            String openId = data != null && data.uniqueId != null && !data.uniqueId.trim().isEmpty() ? data.uniqueId : uniqueId;
            String openFig = data != null && data.figure != null && !data.figure.trim().isEmpty() ? data.figure : fallbackFigure;
            openMiniProfileFull(openNick, hotelKey, openId, openFig);
        });

        favoriteBtn.setOnClickListener(v -> {
            MiniProfilePreview data = loaded[0];
            String favNick = data != null && data.nick != null && !data.nick.trim().isEmpty() ? data.nick : nick;
            String favFig = data != null && data.figure != null && !data.figure.trim().isEmpty() ? data.figure : fallbackFigure;
            String favHotel = data != null && data.hotelKey != null && !data.hotelKey.trim().isEmpty() ? data.hotelKey : hotelKey;
            String favId = data != null && data.uniqueId != null && !data.uniqueId.trim().isEmpty() ? data.uniqueId : uniqueId;
            ProfileResult pr = miniProfileResult(favNick, favFig, favHotel, favId);
            toggleFavoriteProfile(pr);
            favoriteBtn.setBackground(new FavoriteStarDrawable(isFavoriteProfile(pr)));
        });

        dialog.show();
        Window w = dialog.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.copyFrom(w.getAttributes());
            params.width = Math.min(getResources().getDisplayMetrics().widthPixels - dp(28), dp(430));
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            w.setAttributes(params);
        }

        executor.execute(() -> {
            MiniProfilePreview data = fetchMiniProfilePreview(nick, hotelKey, fallbackFigure, uniqueId);
            runOnUiThread(() -> {
                try {
                    if (!dialog.isShowing() || data == null) return;
                    loaded[0] = data;
                    if (data.figure != null && !data.figure.trim().isEmpty()) loadAvatarImage(avatar, avatarFull(data.figure, 2));
                    loaderLine.setVisibility(View.GONE);
                    name.setText(data.nick == null || data.nick.trim().isEmpty() ? nick : data.nick);
                    String mission = data.motto == null ? "" : data.motto.trim();
                    if (mission.isEmpty()) {
                        motto.setText("");
                        motto.setVisibility(View.GONE);
                    } else {
                        motto.setText(mission);
                        motto.setVisibility(View.VISIBLE);
                    }

                    ProfileResult pr = miniProfileResult(data.nick, data.figure, data.hotelKey, data.uniqueId);
                    favoriteBtn.setBackground(new FavoriteStarDrawable(isFavoriteProfile(pr)));

                    badges.removeAllViews();
                    if (data.privateProfile) badges.addView(profileBadge(t("private"), "lock", red));
                    if (data.banned) {
                        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, -2);
                        bp.leftMargin = dp(8);
                        badges.addView(profileBadge(t("banned"), "banned", red), bp);
                    }

                    boolean redBorder = data.privateProfile || data.banned;
                    stats.removeAllViews();
                    stats.addView(miniStatRow(data.online ? "status_online" : "status_offline", t("status"), data.online ? t("online") : t("offline"), "", redBorder));
                    stats.addView(miniStatRow("clock", t("last_login"), niceDate(data.lastAccess), timeAgoText(data.lastAccess), redBorder));
                    stats.addView(miniStatRow("calendar", t("creation"), niceDateOnly(data.memberSince), timeAgoText(data.memberSince), redBorder));
                } catch(Exception ignored) {}
            });
        });
    }

    private LinearLayout miniStatRow(String icon, String label, String value, String tooltip, boolean redBorder) {
        LinearLayout row = card(dp(18));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(10), dp(7), dp(10), dp(7));
        row.setLayoutParams(lp(-1, dp(54), 0, 0, 0, 7));
        row.setBackground(round(lightTheme ? Color.rgb(250,250,250) : Color.argb(20,255,255,255), dp(18), redBorder ? Color.argb(130, 255, 64, 64) : (lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255)), 1));

        if ("status".equals(icon) || "status_online".equals(icon) || "status_offline".equals(icon)) {
            ImageView iv = new ImageView(this);
            iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            row.addView(iv, new LinearLayout.LayoutParams(dp(20), dp(20)));
            boolean onlineStatusIcon = "status_online".equals(icon) || (value != null && value.trim().equalsIgnoreCase(t("online")));
            Glide.with(this).asGif().load(onlineStatusIcon ? R.drawable.online : R.drawable.offline).into(iv);
        } else {
            IconView iv = new IconView(this, icon);
            row.addView(iv, new LinearLayout.LayoutParams(dp(18), dp(18)));
        }

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0, -2, 1);
        tp.leftMargin = dp(9);
        row.addView(texts, tp);
        texts.addView(text(label, 11, Color.argb(190,255,255,255), false));
        texts.addView(text(value == null || value.isEmpty() || "null".equalsIgnoreCase(value) ? "—" : value, 14, Color.WHITE, true));

        if (tooltip != null && !tooltip.trim().isEmpty() && !"—".equals(tooltip.trim())) {
            row.setOnClickListener(v -> toast(tooltip));
        }
        return row;
    }

    private ProfileResult miniProfileResult(String nick, String figure, String hotelKey) {
        return miniProfileResult(nick, figure, hotelKey, "");
    }

    private ProfileResult miniProfileResult(String nick, String figure, String hotelKey, String uniqueId) {
        ProfileResult pr = new ProfileResult();
        pr.name = nick == null ? "" : nick.trim();
        pr.figure = figure == null ? "" : figure.trim();
        pr.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        pr.hotelKey = normalizeHotelKey(hotelKey);
        if (pr.hotelKey.isEmpty()) pr.hotelKey = currentHotelKey;
        return pr;
    }

    private void openMiniProfileFull(String nick, String hotelKey, String uniqueId, String figure) {
        currentHotelKey = normalizeHotelKey(hotelKey);
        if (currentHotelKey.isEmpty()) currentHotelKey = defaultHotelForDeviceLocale();
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(PREF_HOTEL, currentHotelKey).apply();
        currentLoadedNick = "";
        activeSearchToken++;
        searchInProgress = false;
        rebuildUiPreservingProfile();
        openProfileReference(nick == null ? "" : nick.trim(), uniqueId, figure, currentHotelKey);
    }

    private MiniProfilePreview fetchMiniProfilePreview(String nick, String hotelKey, String fallbackFigure, String uniqueId) {
        MiniProfilePreview out = new MiniProfilePreview();
        out.nick = nick == null ? "" : nick.trim();
        out.figure = fallbackFigure == null ? "" : fallbackFigure.trim();
        out.uniqueId = uniqueId == null ? "" : uniqueId.trim();
        out.hotelKey = normalizeHotelKey(hotelKey);
        if (out.hotelKey.isEmpty()) out.hotelKey = currentHotelKey;
        try {
            JSONObject dexById = out.uniqueId.isEmpty() ? null : unwrap(tryJson(habbodexProfileByUniqueUrlForHotel(out.uniqueId, out.hotelKey)));
            JSONObject publicObj = tryJson("https://" + hotelDomain(out.hotelKey) + "/api/public/users?name=" + enc(out.nick));
            publicObj = validProfileObject(publicObj);
            JSONObject dexObj = unwrap(tryJson(habbodexProfileByNameUrl(out.nick)));
            JSONObject base = firstObject(validProfileObject(dexById), validProfileObject(publicObj), validProfileObject(dexObj));
            if (base == null) return out;

            String realId = firstText(base, "uniqueId", "id", "habboId");
            if (!realId.isEmpty()) out.uniqueId = realId;

            String realNick = firstText(base, "name", "username", "habboName");
            if (!realNick.isEmpty()) out.nick = realNick;

            String fig = firstText(base, "figureString", "figure", "figure_string");
            if (fig.isEmpty() && publicObj != null) fig = firstText(publicObj, "figureString", "figure", "figure_string");
            if (!fig.isEmpty()) out.figure = fig;

            out.motto = firstText(base, "motto", "mission");
            if (out.motto.isEmpty() && publicObj != null) out.motto = firstText(publicObj, "motto", "mission");

            out.online = optBoolAny(base, false, "online", "isOnline");
            if (publicObj != null && publicObj.has("online")) out.online = publicObj.optBoolean("online", out.online);

            out.privateProfile = !optBoolAny(base, true, "profileVisible", "isProfileVisible", "visible");
            if (publicObj != null && publicObj.has("profileVisible")) out.privateProfile = !publicObj.optBoolean("profileVisible", true);

            out.banned = publicObj != null && isSameProfileObject(base, publicObj) ? false : optBoolTrue(base, "isBanned", "banned", "ban", "is_banned");

            out.memberSince = firstText(base, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");
            if (out.memberSince.isEmpty() && publicObj != null) out.memberSince = firstText(publicObj, "memberSince", "creationTime", "createdAt", "registeredAt", "created_at", "registerDate", "registrationDate");

            out.lastAccess = firstText(base, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
            if (out.lastAccess.isEmpty() && publicObj != null) out.lastAccess = firstText(publicObj, "lastAccessTime", "lastLoginTime", "lastOnline", "lastVisit");
        } catch(Exception ignored) {}
        return out;
    }


    private static long parseHabboTimestampMsStatic(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) return 0L;
        ArrayList<String> candidates = new ArrayList<>();
        candidates.add(value);
        if (value.endsWith("Z")) candidates.add(value.substring(0, value.length() - 1) + "+0000");
        if (value.matches(".*[+-]\\d{2}:\\d{2}$")) {
            candidates.add(value.substring(0, value.length() - 3) + value.substring(value.length() - 2));
        }
        String[] patterns = new String[] {
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String candidate : candidates) {
            for (String pattern : patterns) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                    sdf.setLenient(false);
                    if (!pattern.endsWith("Z")) sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date d = sdf.parse(candidate);
                    if (d != null) return d.getTime();
                } catch(Exception ignored) {}
            }
        }
        return 0L;
    }

    private static boolean isFavoriteRecentlyOnlineStatic(FavoriteStatus st) {
        if (st == null || st.lastAccess == null || st.lastAccess.trim().isEmpty()) return false;
        long access = parseHabboTimestampMsStatic(st.lastAccess);
        if (access <= 0L) return false;
        long age = System.currentTimeMillis() - access;
        return age >= -30_000L && age <= 3L * 60L * 1000L;
    }

    private static class MiniProfilePreview {
        String nick = "", figure = "", uniqueId = "", hotelKey = "br", motto = "", lastAccess = "", memberSince = "";
        boolean online = false, privateProfile = false, banned = false;
    }

    private static class FavoriteStatus {
        String nick = "", figure = "", hotelKey = "br", uniqueId = "", lastAccess = "";
        boolean online = false, privateProfile = false;
    }

    private static class ProfileHistoryItem {
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

    private static class ProfileResult {
        String searchedNick = "", uniqueId = "", name = "", motto = "", figure = "", memberSince = "", lastAccess = "", level = "", starGems = "", totalBadges = "", hotelKey = "br";
        boolean online = false, privateProfile = false, banned = false;
        JSONObject habboPublic, dex, suggest, dexProfile, officialProfile;
        ArrayList<JSONObject> previousNames = new ArrayList<>(), previousMottos = new ArrayList<>(), previousStyles = new ArrayList<>(), photos = new ArrayList<>(), friends = new ArrayList<>(), oldFriends = new ArrayList<>(), rooms = new ArrayList<>(), oldRooms = new ArrayList<>(), groups = new ArrayList<>(), selectedBadges = new ArrayList<>(), badges = new ArrayList<>(), badgesWithAchievements = new ArrayList<>();
        int photosNextPage = 0, stylesNextPage = 0, photosTotal = 0, stylesTotal = 0;
        boolean photosHasMore = false, stylesHasMore = false, photosLoading = false, stylesLoading = false;
    }

    private static class PageResult {
        ArrayList<JSONObject> items = new ArrayList<>();
        int page = 1, nextPage = 0, total = 0;
        boolean hasMore = false;
    }




    public static class FavoriteOnlineReceiver extends BroadcastReceiver {
        @Override public void onReceive(Context context, Intent intent) {
            final PendingResult pending = goAsync();
            new Thread(() -> {
                try {
                    checkFavoritesInBackground(context);
                } catch(Exception ignored) {
                } finally {
                    try { pending.finish(); } catch(Exception ignored) {}
                }
            }).start();
        }

        private static void checkFavoritesInBackground(Context context) {
            if (context == null) return;
            SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            if (!sp.getBoolean(PREF_NOTIFY_FAVORITE_ONLINE, false)) return;

            String rawFavorites = sp.getString(PREF_FAVORITES, "");
            if (rawFavorites == null || rawFavorites.trim().isEmpty()) return;

            JSONObject states;
            try {
                String rawStates = sp.getString(PREF_FAVORITE_ONLINE_STATES, "{}");
                states = new JSONObject(rawStates == null || rawStates.trim().isEmpty() ? "{}" : rawStates);
            } catch(Exception e) {
                states = new JSONObject();
            }

            try {
                JSONArray arr = new JSONArray(rawFavorites);
                for (int i=0; i<arr.length(); i++) {
                    JSONObject fav = arr.optJSONObject(i);
                    if (fav == null) continue;
                    String nick = fav.optString("nick", "").trim();
                    if (nick.isEmpty()) continue;
                    String hotel = normalizeHotelKeyStatic(fav.optString("hotel", "br"));
                    if (hotel.isEmpty()) hotel = "br";
                    String uniqueId = fav.optString("uniqueId", fav.optString("id", "")).trim();
                    String key = profileIdentityKeyStatic(hotel, uniqueId, nick);

                    FavoriteStatus st = fetchFavoriteStatusStatic(nick, fav.optString("figure", ""), hotel, uniqueId);
                    if (st == null) continue;

                    boolean hadPrevious = states.has(key);
                    boolean wasOnline = states.optBoolean(key, false);
                    states.put(key, st.online);

                    if (hadPrevious && !wasOnline && st.online && isFavoriteRecentlyOnlineStatic(st)) {
                        showFavoriteOnlineSystemNotificationStatic(context, st);
                    }
                }
                sp.edit().putString(PREF_FAVORITE_ONLINE_STATES, states.toString()).apply();
            } catch(Exception ignored) {}
        }

        private static FavoriteStatus fetchFavoriteStatusStatic(String nick, String fallbackFigure, String hotel, String uniqueId) {
            HttpURLConnection c = null;
            try {
                String safeId = uniqueId == null ? "" : uniqueId.trim();
                URL u;
                if (!safeId.isEmpty()) u = new URL("https://" + hotelDomainStatic(hotel) + "/api/public/users/" + URLEncoder.encode(safeId, "UTF-8") + "/profile");
                else u = new URL("https://" + hotelDomainStatic(hotel) + "/api/public/users?name=" + URLEncoder.encode(nick, "UTF-8"));
                c = (HttpURLConnection)u.openConnection();
                c.setConnectTimeout(12000);
                c.setReadTimeout(18000);
                c.setRequestProperty("Accept", "application/json, text/plain, */*");
                c.setRequestProperty("User-Agent", "ToxicSearchTool/1.0 Android");
                int code = c.getResponseCode();
                InputStream is = code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream();
                String body = readAllStatic(is);
                if (body == null || body.trim().isEmpty() || body.trim().startsWith("[")) return null;
                JSONObject root = new JSONObject(body);
                JSONObject obj = root.optJSONObject("user");
                if (obj == null) obj = root;
                if (obj.has("ok") && !obj.optBoolean("ok", true) && !obj.has("uniqueId")) return null;

                FavoriteStatus st = new FavoriteStatus();
                st.nick = obj.optString("name", nick);
                if (st.nick == null || st.nick.trim().isEmpty()) st.nick = nick;
                st.uniqueId = obj.optString("uniqueId", obj.optString("id", safeId));
                st.figure = obj.optString("figureString", obj.optString("figure", fallbackFigure == null ? "" : fallbackFigure));
                st.hotelKey = hotel;
                st.online = obj.optBoolean("online", obj.optBoolean("isOnline", false));
                st.privateProfile = !obj.optBoolean("profileVisible", obj.optBoolean("isProfileVisible", obj.optBoolean("visible", true)));
                st.lastAccess = obj.optString("lastAccessTime", obj.optString("lastLoginTime", obj.optString("lastOnline", obj.optString("lastVisit", ""))));
                return st;
            } catch(Exception ignored) {
                return null;
            } finally {
                try { if (c != null) c.disconnect(); } catch(Exception ignored) {}
            }
        }


        private static File favoriteHeadCacheDirStatic(Context context) {
            File dir = new File(context.getCacheDir(), "favorite_heads");
            try { dir.mkdirs(); } catch(Exception ignored) {}
            return dir;
        }

        private static File favoriteHeadCacheFileStatic(Context context, String hotelKey, String nick) {
            return favoriteHeadCacheFileStatic(context, hotelKey, nick, "");
        }

        private static File favoriteHeadCacheFileStatic(Context context, String hotelKey, String nick, String uniqueId) {
            String key = profileIdentityKeyStatic(hotelKey, uniqueId, nick);
            return new File(favoriteHeadCacheDirStatic(context), Math.abs(key.hashCode()) + ".png");
        }

        private static void saveFavoriteHeadBitmapStatic(Context context, FavoriteStatus st, Bitmap bitmap) {
            if (context == null || st == null || bitmap == null) return;
            try {
                FileOutputStream out = new FileOutputStream(favoriteHeadCacheFileStatic(context, st.hotelKey, st.nick, st.uniqueId));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            } catch(Exception ignored) {}
        }

        private static Bitmap loadFavoriteHeadFromCacheStatic(Context context, FavoriteStatus st) {
            try {
                if (context == null || st == null) return null;
                File f = favoriteHeadCacheFileStatic(context, st.hotelKey, st.nick, st.uniqueId);
                if (f.exists()) return BitmapFactory.decodeFile(f.getAbsolutePath());
            } catch(Exception ignored) {}
            return null;
        }

        private static Bitmap loadNotificationHeadBitmapStatic(Context context, FavoriteStatus st) {
            HttpURLConnection c = null;
            try {
                if (context == null || st == null) return null;
                String url;
                if (st.nick != null && !st.nick.trim().isEmpty()) {
                    url = "https://" + hotelDomainStatic(st.hotelKey) + "/habbo-imaging/avatarimage?user=" + URLEncoder.encode(st.nick, "UTF-8") + "&size=m&direction=2&head_direction=2&headonly=1";
                } else if (st.figure != null && !st.figure.trim().isEmpty()) {
                    url = "https://" + hotelDomainStatic(st.hotelKey) + "/habbo-imaging/avatarimage?figure=" + URLEncoder.encode(st.figure, "UTF-8") + "&size=m&direction=2&head_direction=2&headonly=1";
                } else {
                    Bitmap cached = loadFavoriteHeadFromCacheStatic(context, st);
                    return cached != null ? cached : BitmapFactory.decodeResource(context.getResources(), R.drawable.pre_load_head);
                }
                c = (HttpURLConnection)new URL(url).openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(15000);
                Bitmap b = BitmapFactory.decodeStream(c.getInputStream());
                if (b != null) {
                    saveFavoriteHeadBitmapStatic(context, st, b);
                    return b;
                }
                Bitmap cached = loadFavoriteHeadFromCacheStatic(context, st);
                return cached != null ? cached : BitmapFactory.decodeResource(context.getResources(), R.drawable.pre_load_head);
            } catch(Exception ignored) {
                Bitmap cached = loadFavoriteHeadFromCacheStatic(context, st);
                return cached != null ? cached : (context == null ? null : BitmapFactory.decodeResource(context.getResources(), R.drawable.pre_load_head));
            } finally {
                try { if (c != null) c.disconnect(); } catch(Exception ignored) {}
            }
        }

        private static void showFavoriteOnlineSystemNotificationStatic(Context context, FavoriteStatus st) {
            try {
                NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (nm == null || st == null) return;
                String channelId = "favorite_online";
                if (Build.VERSION.SDK_INT >= 26) {
                    NotificationChannel ch = new NotificationChannel(channelId, "Favoritos", NotificationManager.IMPORTANCE_HIGH);
                    nm.createNotificationChannel(ch);
                }
                Intent open = new Intent(context, MainActivity.class);
                open.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pi = PendingIntent.getActivity(context, 1207, open, Build.VERSION.SDK_INT >= 23 ? PendingIntent.FLAG_IMMUTABLE : 0);
                String msg = (st.nick == null ? "" : st.nick) + " acabou de ficar online!";
                Bitmap largeIcon = loadNotificationHeadBitmapStatic(context, st);
                Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(context, channelId) : new Notification.Builder(context);
                b.setSmallIcon(R.drawable.notification_image)
                 .setContentTitle("Favoritos")
                 .setContentText(msg)
                 .setWhen(System.currentTimeMillis())
                 .setShowWhen(true)
                 .setPriority(Notification.PRIORITY_HIGH)
                 .setContentIntent(pi)
                 .setAutoCancel(true)
                 .setStyle(new Notification.BigTextStyle().bigText(msg));
                if (largeIcon != null) b.setLargeIcon(largeIcon);
                nm.notify(Math.abs(profileIdentityKeyStatic(st.hotelKey, st.uniqueId, st.nick).hashCode()), b.build());
            } catch(Exception ignored) {}
        }

        private static String readAllStatic(InputStream is) throws IOException {
            if (is == null) return "";
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) out.write(buf, 0, n);
            return out.toString("UTF-8");
        }

        private static String normalizeHotelKeyStatic(String hotel) {
            String h = hotel == null ? "" : hotel.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
            if ("us".equals(h)) h = "com";
            String[] allowed = {"br","com","es","de","fr","fi","it","nl","tr"};
            for (String a : allowed) if (a.equals(h)) return h;
            return "";
        }

        private static String profileIdentityKeyStatic(String hotelKey, String uniqueId, String nick) {
            String hotel = normalizeHotelKeyStatic(hotelKey);
            String id = normalizeNickKeyStatic(uniqueId);
            if (!id.isEmpty()) return hotel + ":id:" + id;
            return hotel + ":nick:" + normalizeNickKeyStatic(nick);
        }

        private static String normalizeNickKeyStatic(String raw) {
            return raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        }

        private static String hotelDomainStatic(String key) {
            String h = normalizeHotelKeyStatic(key);
            if ("com".equals(h)) return "www.habbo.com";
            if ("es".equals(h)) return "www.habbo.es";
            if ("de".equals(h)) return "www.habbo.de";
            if ("fr".equals(h)) return "www.habbo.fr";
            if ("fi".equals(h)) return "www.habbo.fi";
            if ("it".equals(h)) return "www.habbo.it";
            if ("nl".equals(h)) return "www.habbo.nl";
            if ("tr".equals(h)) return "www.habbo.com.tr";
            return "www.habbo.com.br";
        }
    }


    private class CircularPullProgressView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float progressPct = 0f;

        CircularPullProgressView(Context context) {
            super(context);
        }

        void setProgressPct(float value) {
            progressPct = Math.max(0f, Math.min(1f, value));
            invalidate();
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            float m = Math.min(w, h);
            if (m <= 0f) return;
            float cx = w / 2f;
            float cy = h / 2f;
            float stroke = Math.max(dp(4), m * 0.14f);
            float r = (m - stroke) / 2f - dp(1);
            RectF arc = new RectF(cx - r, cy - r, cx + r, cy + r);

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(stroke);
            p.setColor(lightTheme ? Color.rgb(228, 220, 238) : Color.argb(90, 255, 255, 255));
            canvas.drawCircle(cx, cy, r, p);

            if (progressPct > 0f) {
                p.setShader(null);
                p.setColor(purple);
                canvas.drawArc(arc, -90f, 360f * progressPct, false, p);
            }
        }
    }

    public class HotelFlagDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String hotel;
        HotelFlagDrawable(String hotelKey) { hotel = normalizeHotelKey(hotelKey); if (hotel.isEmpty()) hotel = "br"; }
        @Override public int getIntrinsicWidth() { return dp(24); }
        @Override public int getIntrinsicHeight() { return dp(16); }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left, b.top, b.right, b.bottom);
            p.setStyle(Paint.Style.FILL);
            p.setShader(null);
            c.save();
            Path clip = new Path();
            clip.addRoundRect(r, dp(3), dp(3), Path.Direction.CW);
            c.clipPath(clip);
            float w = r.width(), h = r.height(), x = r.left, y = r.top;
            if ("br".equals(hotel)) {
                p.setColor(Color.rgb(34, 166, 74)); c.drawRect(r, p);
                p.setColor(Color.rgb(255, 223, 64));
                Path d = new Path(); d.moveTo(x+w*.50f,y+h*.10f); d.lineTo(x+w*.90f,y+h*.50f); d.lineTo(x+w*.50f,y+h*.90f); d.lineTo(x+w*.10f,y+h*.50f); d.close(); c.drawPath(d,p);
                p.setColor(Color.rgb(39, 74, 160)); c.drawCircle(x+w*.50f, y+h*.50f, Math.min(w,h)*.20f, p);
            } else if ("com".equals(hotel)) {
                for (int i=0;i<7;i++){ p.setColor(i%2==0?Color.rgb(188,10,48):Color.WHITE); c.drawRect(x, y+h*i/7f, x+w, y+h*(i+1)/7f, p); }
                p.setColor(Color.rgb(40,60,130)); c.drawRect(x,y,x+w*.42f,y+h*.54f,p);
            } else if ("es".equals(hotel)) {
                p.setColor(Color.rgb(198, 0, 43)); c.drawRect(r,p); p.setColor(Color.rgb(255, 206, 0)); c.drawRect(x,y+h*.25f,x+w,y+h*.75f,p);
            } else if ("de".equals(hotel)) {
                p.setColor(Color.BLACK); c.drawRect(x,y,x+w,y+h/3f,p); p.setColor(Color.rgb(221,0,0)); c.drawRect(x,y+h/3f,x+w,y+2*h/3f,p); p.setColor(Color.rgb(255,206,0)); c.drawRect(x,y+2*h/3f,x+w,y+h,p);
            } else if ("fr".equals(hotel)) {
                p.setColor(Color.rgb(0,35,149)); c.drawRect(x,y,x+w/3f,y+h,p); p.setColor(Color.WHITE); c.drawRect(x+w/3f,y,x+2*w/3f,y+h,p); p.setColor(Color.rgb(237,41,57)); c.drawRect(x+2*w/3f,y,x+w,y+h,p);
            } else if ("fi".equals(hotel)) {
                p.setColor(Color.WHITE); c.drawRect(r,p); p.setColor(Color.rgb(0,53,128)); c.drawRect(x+w*.30f,y,x+w*.46f,y+h,p); c.drawRect(x,y+h*.38f,x+w,y+h*.58f,p);
            } else if ("it".equals(hotel)) {
                p.setColor(Color.rgb(0,146,70)); c.drawRect(x,y,x+w/3f,y+h,p); p.setColor(Color.WHITE); c.drawRect(x+w/3f,y,x+2*w/3f,y+h,p); p.setColor(Color.rgb(206,43,55)); c.drawRect(x+2*w/3f,y,x+w,y+h,p);
            } else if ("nl".equals(hotel)) {
                p.setColor(Color.rgb(174,28,40)); c.drawRect(x,y,x+w,y+h/3f,p); p.setColor(Color.WHITE); c.drawRect(x,y+h/3f,x+w,y+2*h/3f,p); p.setColor(Color.rgb(33,70,139)); c.drawRect(x,y+2*h/3f,x+w,y+h,p);
            } else if ("tr".equals(hotel)) {
                p.setColor(Color.rgb(227,10,23)); c.drawRect(r,p); p.setColor(Color.WHITE); c.drawCircle(x+w*.43f,y+h*.50f,h*.25f,p); p.setColor(Color.rgb(227,10,23)); c.drawCircle(x+w*.50f,y+h*.50f,h*.20f,p); p.setColor(Color.WHITE); Path star=new Path(); float cx=x+w*.64f, cy=y+h*.50f, rr=h*.15f; for(int i=0;i<10;i++){ double a=-Math.PI/2+i*Math.PI/5; float rad=(i%2==0)?rr:rr*.42f; float px=cx+(float)Math.cos(a)*rad, py=cy+(float)Math.sin(a)*rad; if(i==0) star.moveTo(px,py); else star.lineTo(px,py);} star.close(); c.drawPath(star,p);
            }
            c.restore();
            p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(1)); p.setColor(Color.argb(90,0,0,0)); c.drawRoundRect(r, dp(3), dp(3), p);
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    public class AddButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float x = b.left, y = b.top, w = b.width(), h = b.height();
            float size = Math.min(w, h);
            float left = x + (w - size) / 2f;
            float top = y + (h - size) / 2f;
            RectF r = new RectF(left, top, left + size, top + size);
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom, purple2, purple, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(r, dp(7), dp(7), p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(85,255,255,255));
            c.drawRoundRect(new RectF(r.left+1, r.top+1, r.right-1, r.bottom-1), dp(7), dp(7), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(Color.WHITE);
            float cx = r.centerX(), cy = r.centerY();
            float len = size * 0.22f;
            c.drawLine(cx - len, cy, cx + len, cy, p);
            c.drawLine(cx, cy - len, cx, cy + len, p);
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }





    public class BottomNavBarDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(lightTheme ? Color.WHITE : Color.rgb(10, 10, 14));
            c.drawRect(b.left, b.top, b.right, b.bottom, p);
        }

        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.OPAQUE; }
    }

    public class BottomNavIconDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        String type;
        boolean selected;
        BottomNavIconDrawable(String type, boolean selected) { this.type = type == null ? "home" : type; this.selected = selected; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), x = b.left, y = b.top, m = Math.min(w, h);
            float cx = b.centerX(), cy = b.centerY();
            int color = bottomNavIconColor(selected);

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, m * .085f));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(color);

            if ("home".equals(type)) {
                // Lupa vetorial: grossa, arredondada, proporcional aos outros ícones e sem círculo interno.
                float lensR = m * .215f;
                float lx = cx - m * .075f;
                float ly = cy - m * .070f;
                float stroke = Math.max(3.0f, m * .102f);

                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);

                // Leve base escura para dar profundidade sem criar novo círculo interno.
                p.setStrokeWidth(stroke + Math.max(1.2f, m * .026f));
                p.setColor(Color.argb(selected ? 105 : 86, 32, 0, 72));
                c.drawCircle(lx, ly, lensR, p);
                c.drawLine(lx + lensR * .72f, ly + lensR * .72f, cx + m * .265f, cy + m * .265f, p);

                p.setStrokeWidth(stroke);
                p.setColor(color);
                c.drawCircle(lx, ly, lensR, p);
                // O cabo começa fora da borda da lente para não sobrepor as linhas.
                c.drawLine(lx + lensR * .82f, ly + lensR * .82f, cx + m * .265f, cy + m * .265f, p);
            } else if ("visuals".equals(type)) {
                // Ícone de camiseta minimalista para o provador de visuais.
                Path shirt = new Path();
                shirt.moveTo(x + w*.27f, y + h*.28f);
                shirt.lineTo(x + w*.39f, y + h*.20f);
                shirt.quadTo(x + w*.50f, y + h*.28f, x + w*.61f, y + h*.20f);
                shirt.lineTo(x + w*.73f, y + h*.28f);
                shirt.lineTo(x + w*.86f, y + h*.43f);
                shirt.lineTo(x + w*.75f, y + h*.55f);
                shirt.lineTo(x + w*.70f, y + h*.49f);
                shirt.lineTo(x + w*.70f, y + h*.80f);
                shirt.lineTo(x + w*.30f, y + h*.80f);
                shirt.lineTo(x + w*.30f, y + h*.49f);
                shirt.lineTo(x + w*.25f, y + h*.55f);
                shirt.lineTo(x + w*.14f, y + h*.43f);
                shirt.close();
                if (selected) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(color);
                    c.drawPath(shirt, p);
                } else {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(Math.max(2f, m * .075f));
                    p.setColor(color);
                    c.drawPath(shirt, p);
                }
            } else if ("heart".equals(type)) {
                Path heart = new Path();
                heart.moveTo(cx, cy + m*.27f);
                heart.cubicTo(cx - m*.40f, cy + m*.02f, cx - m*.34f, cy - m*.25f, cx - m*.16f, cy - m*.25f);
                heart.cubicTo(cx - m*.06f, cy - m*.25f, cx, cy - m*.17f, cx, cy - m*.12f);
                heart.cubicTo(cx, cy - m*.17f, cx + m*.06f, cy - m*.25f, cx + m*.16f, cy - m*.25f);
                heart.cubicTo(cx + m*.34f, cy - m*.25f, cx + m*.40f, cy + m*.02f, cx, cy + m*.27f);
                heart.close();
                if (selected) {
                    p.setStyle(Paint.Style.FILL);
                    p.setColor(color);
                    c.drawPath(heart, p);
                } else {
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(Math.max(2.1f, m * .078f));
                    p.setStrokeJoin(Paint.Join.ROUND);
                    p.setStrokeCap(Paint.Cap.ROUND);
                    p.setColor(color);
                    c.drawPath(heart, p);
                }
            } else {
                // Ícone tipo menu/hambúrguer minimalista, mais alto e proporcional aos outros.
                p.setStrokeWidth(Math.max(2.2f, m*.09f));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(color);
                float left = x + w*.17f;
                float right = x + w*.83f;
                c.drawLine(left, y + h*.27f, right, y + h*.27f, p);
                c.drawLine(left, y + h*.50f, right, y + h*.50f, p);
                c.drawLine(left, y + h*.73f, right, y + h*.73f, p);
            }
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }


    public class AchievementSwitchDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean checked;
        AchievementSwitchDrawable(boolean checked) { this.checked = checked; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), x = b.left, y = b.top;
            float pad = Math.max(1f, h * .08f);
            RectF track = new RectF(x + pad, y + pad, x + w - pad, y + h - pad);
            float radius = track.height() / 2f;

            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(checked ? Color.rgb(39, 174, 96) : (lightTheme ? Color.rgb(210, 210, 214) : Color.rgb(55, 55, 64)));
            c.drawRoundRect(track, radius, radius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, h * .045f));
            p.setColor(checked ? Color.rgb(39, 174, 96) : (lightTheme ? Color.rgb(196, 196, 200) : Color.rgb(72, 72, 82)));
            c.drawRoundRect(track, radius, radius, p);

            float knobRadius = track.height() * .42f;
            float knobCx = checked ? (track.right - radius) : (track.left + radius);
            float knobCy = track.centerY();

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            c.drawCircle(knobCx, knobCy, knobRadius, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, h * .035f));
            p.setColor(checked ? Color.argb(35, 0, 0, 0) : (lightTheme ? Color.rgb(190,190,194) : Color.argb(80,255,255,255)));
            c.drawCircle(knobCx, knobCy, knobRadius, p);
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    private void drawRemovedTrash(Canvas c, Rect bounds, int color, float scaleFactor) {
        Paint trashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trashPaint.setStyle(Paint.Style.STROKE); trashPaint.setStrokeCap(Paint.Cap.ROUND); trashPaint.setStrokeJoin(Paint.Join.ROUND); trashPaint.setColor(color);
        float w=bounds.width(), h=bounds.height(); float scale=Math.min(w,h)*scaleFactor/512f; float ox=bounds.left+(w-512f*scale)/2f; float oy=bounds.top+(h-512f*scale)/2f;
        trashPaint.setStrokeWidth(Math.max(2.3f, 18f*scale));
        Path handle=new Path(); handle.moveTo(ox+210f*scale,oy+154f*scale); handle.lineTo(ox+210f*scale,oy+143f*scale); handle.cubicTo(ox+210f*scale,oy+134f*scale,ox+217f*scale,oy+127f*scale,ox+226f*scale,oy+127f*scale); handle.lineTo(ox+286f*scale,oy+127f*scale); handle.cubicTo(ox+295f*scale,oy+127f*scale,ox+302f*scale,oy+134f*scale,ox+302f*scale,oy+143f*scale); handle.lineTo(ox+302f*scale,oy+154f*scale); c.drawPath(handle,trashPaint);
        c.drawRoundRect(new RectF(ox+123f*scale,oy+154f*scale,ox+389f*scale,oy+184f*scale),15f*scale,15f*scale,trashPaint);
        Path body=new Path(); body.moveTo(ox+160f*scale,oy+194f*scale); body.lineTo(ox+160f*scale,oy+382f*scale); body.cubicTo(ox+160f*scale,oy+398f*scale,ox+173f*scale,oy+411f*scale,ox+189f*scale,oy+411f*scale); body.lineTo(ox+323f*scale,oy+411f*scale); body.cubicTo(ox+339f*scale,oy+411f*scale,ox+352f*scale,oy+398f*scale,ox+352f*scale,oy+382f*scale); body.lineTo(ox+352f*scale,oy+194f*scale); c.drawPath(body,trashPaint);
        c.drawLine(ox+212f*scale,oy+246f*scale,ox+212f*scale,oy+356f*scale,trashPaint); c.drawLine(ox+256f*scale,oy+246f*scale,ox+256f*scale,oy+356f*scale,trashPaint); c.drawLine(ox+300f*scale,oy+246f*scale,ox+300f*scale,oy+356f*scale,trashPaint);
    }

    public class TrashTabDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean active;
        TrashTabDrawable(boolean active) { this.active = active; }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            int color = active ? purple : (lightTheme ? Color.rgb(59,7,91) : Color.WHITE);
            drawRemovedTrash(c, b, color, .82f);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class FavoriteStarDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean active;
        FavoriteStarDrawable(boolean active) { this.active = active; }

        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float cx = b.centerX(), cy = b.centerY(), m = Math.min(b.width(), b.height());
            int color = bottomNavIconColor(active);

            Path heart = new Path();
            heart.moveTo(cx, cy + m*.27f);
            heart.cubicTo(cx - m*.40f, cy + m*.02f, cx - m*.34f, cy - m*.25f, cx - m*.16f, cy - m*.25f);
            heart.cubicTo(cx - m*.06f, cy - m*.25f, cx, cy - m*.17f, cx, cy - m*.12f);
            heart.cubicTo(cx, cy - m*.17f, cx + m*.06f, cy - m*.25f, cx + m*.16f, cy - m*.25f);
            heart.cubicTo(cx + m*.34f, cy - m*.25f, cx + m*.40f, cy + m*.02f, cx, cy + m*.27f);
            heart.close();

            p.setShader(null);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setColor(color);
            if (active) {
                p.setStyle(Paint.Style.FILL);
                c.drawPath(heart, p);
            } else {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(2.1f, m * .078f));
                c.drawPath(heart, p);
            }
        }

        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }


    public class RemoveXDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w=b.width(), h=b.height(), x=b.left, y=b.top, m=Math.min(w,h);
            RectF bg = new RectF(x+m*.10f, y+m*.10f, x+w-m*.10f, y+h-m*.10f);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(190, 48, 70));
            c.drawRoundRect(bg, m*.22f, m*.22f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeWidth(Math.max(2f, m*.075f));
            p.setColor(Color.WHITE);
            c.drawLine(x+w*.35f, y+h*.35f, x+w*.65f, y+h*.65f, p);
            c.drawLine(x+w*.65f, y+h*.35f, x+w*.35f, y+h*.65f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class HistoryClockDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), x = b.left, y = b.top;
            float cx = x + w / 2f, cy = y + h / 2f;
            float radius = Math.min(w, h) * 0.28f;
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(lightTheme ? Color.argb(0,0,0,0) : Color.argb(0,255,255,255));
            c.drawRect(x, y, x+w, y+h, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(lightTheme ? Color.rgb(45,45,45) : Color.argb(235,255,255,255));
            RectF oval = new RectF(cx - radius, cy - radius, cx + radius, cy + radius);
            c.drawArc(oval, 35, 285, false, p);

            Path arrow = new Path();
            double a = Math.toRadians(35);
            float ax = cx + (float)Math.cos(a) * radius;
            float ay = cy + (float)Math.sin(a) * radius;
            arrow.moveTo(ax, ay);
            arrow.lineTo(ax - dp(8), ay - dp(1));
            arrow.moveTo(ax, ay);
            arrow.lineTo(ax - dp(3), ay + dp(7));
            c.drawPath(arrow, p);

            c.drawLine(cx, cy, cx, cy - radius * 0.52f, p);
            c.drawLine(cx, cy, cx + radius * 0.48f, cy + radius * 0.18f, p);
            p.setStyle(Paint.Style.FILL);
            c.drawCircle(cx, cy, dp(2), p);
        }
        @Override public void setAlpha(int alpha) { p.setAlpha(alpha); }
        @Override public void setColorFilter(android.graphics.ColorFilter cf) { p.setColorFilter(cf); }
        @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
    }

    public class ArrowButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); boolean left;
        ArrowButtonDrawable(boolean left){ this.left = left; }
        @Override public void draw(Canvas c) {
            Rect b = getBounds(); float w=b.width(), h=b.height(), x=b.left, y=b.top;
            RectF r = new RectF(x, y, x+w, y+h);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(38, 35, 45));
            c.drawRoundRect(r, dp(11), dp(11), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(70,255,255,255));
            c.drawRoundRect(new RectF(x+1,y+1,x+w-1,y+h-1), dp(11), dp(11), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(Color.WHITE);
            float yMid = y + h * .50f;
            float startX = x + w * .26f, endX = x + w * .74f;
            if (left) {
                c.drawLine(endX, yMid, startX, yMid, p);
                Path path = new Path();
                path.moveTo(x+w*.42f, y+h*.30f);
                path.lineTo(startX, yMid);
                path.lineTo(x+w*.42f, y+h*.70f);
                c.drawPath(path, p);
            } else {
                c.drawLine(startX, yMid, endX, yMid, p);
                Path path = new Path();
                path.moveTo(x+w*.58f, y+h*.30f);
                path.lineTo(endX, yMid);
                path.lineTo(x+w*.58f, y+h*.70f);
                c.drawPath(path, p);
            }
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class ShirtDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds(); float w=b.width(), h=b.height(), x=b.left, y=b.top;
            RectF r = new RectF(x, y, x+w, y+h);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.rgb(38, 35, 45));
            c.drawRoundRect(r, dp(11), dp(11), p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(70,255,255,255));
            c.drawRoundRect(new RectF(x+1,y+1,x+w-1,y+h-1), dp(11), dp(11), p);

            float ox = x + w * .18f, oy = y + h * .15f, sw = w * .64f, sh = h * .68f;
            Path leftSleeve = new Path();
            leftSleeve.moveTo(ox+sw*.22f, oy+sh*.10f); leftSleeve.lineTo(ox+sw*.02f, oy+sh*.22f); leftSleeve.lineTo(ox+sw*.15f, oy+sh*.42f); leftSleeve.lineTo(ox+sw*.33f, oy+sh*.28f); leftSleeve.close();
            Path rightSleeve = new Path();
            rightSleeve.moveTo(ox+sw*.78f, oy+sh*.10f); rightSleeve.lineTo(ox+sw*.98f, oy+sh*.22f); rightSleeve.lineTo(ox+sw*.85f, oy+sh*.42f); rightSleeve.lineTo(ox+sw*.67f, oy+sh*.28f); rightSleeve.close();
            p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(255,107,122)); c.drawPath(leftSleeve,p); c.drawPath(rightSleeve,p);
            Path body = new Path();
            body.moveTo(ox+sw*.34f, oy+sh*.10f); body.lineTo(ox+sw*.22f, oy+sh*.16f); body.lineTo(ox+sw*.22f, oy+sh*.38f); body.lineTo(ox+sw*.28f, oy+sh*.44f); body.lineTo(ox+sw*.28f, oy+sh*.95f); body.lineTo(ox+sw*.72f, oy+sh*.95f); body.lineTo(ox+sw*.72f, oy+sh*.44f); body.lineTo(ox+sw*.78f, oy+sh*.38f); body.lineTo(ox+sw*.78f, oy+sh*.16f); body.lineTo(ox+sw*.66f, oy+sh*.10f); body.close();
            p.setColor(Color.rgb(217,75,66)); c.drawPath(body,p);
            p.setColor(Color.rgb(182,58,51)); c.drawRect(ox+sw*.28f, oy+sh*.44f, ox+sw*.34f, oy+sh*.95f, p); c.drawRect(ox+sw*.66f, oy+sh*.44f, ox+sw*.72f, oy+sh*.95f, p);
            p.setColor(Color.rgb(255,107,122)); c.drawRect(ox+sw*.36f, oy+sh*.84f, ox+sw*.64f, oy+sh*.91f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class PlaceholderDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); String type;
        PlaceholderDrawable(String t){type=t;}
        @Override public void draw(Canvas c){ Rect b=getBounds(); p.setStyle(Paint.Style.FILL); p.setColor(Color.argb(32,255,255,255)); c.drawRoundRect(new RectF(b), dp(12), dp(12), p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(dp(2)); p.setColor(Color.argb(190,255,255,255)); float cx=b.centerX(), cy=b.centerY(); if("groups".equals(type)){ c.drawCircle(cx,cy,Math.min(b.width(),b.height())*.25f,p); c.drawCircle(cx,cy,Math.min(b.width(),b.height())*.12f,p);} else { Path path=new Path(); path.moveTo(cx,b.top+dp(12)); path.lineTo(b.right-dp(12),cy-dp(4)); path.lineTo(b.right-dp(12),cy+dp(18)); path.lineTo(cx,b.bottom-dp(10)); path.lineTo(b.left+dp(12),cy+dp(18)); path.lineTo(b.left+dp(12),cy-dp(4)); path.close(); c.drawPath(path,p);} }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class IconView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG); String type;
        public IconView(Context c, String t) { super(c); type = t; }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            float w=getWidth(), h=getHeight(), cx=w/2f, cy=h/2f, m=Math.min(w,h);
            p.setShader(null); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(2f, m*.11f)); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);
            if ("dot".equals(type)) { p.setStyle(Paint.Style.FILL); p.setColor(purple); c.drawCircle(cx,cy,m*.28f,p); return; }
            if ("lock".equals(type)) { RectF body = new RectF(cx-m*.26f, cy-m*.02f, cx+m*.26f, cy+m*.30f); c.drawRoundRect(body, m*.08f, m*.08f, p); c.drawArc(new RectF(cx-m*.22f, cy-m*.36f, cx+m*.22f, cy+m*.12f), 200, 140, false, p); return; }
            if ("status".equals(type)) { p.setColor(Color.rgb(255,120,135)); c.drawCircle(cx,cy,m*.34f,p); p.setStyle(Paint.Style.FILL); p.setColor(Color.rgb(240,40,54)); c.drawCircle(cx,cy,m*.18f,p); return; }
            if ("clock".equals(type)) { c.drawCircle(cx,cy,m*.36f,p); c.drawLine(cx,cy,cx,cy-m*.20f,p); c.drawLine(cx,cy,cx+m*.17f,cy+m*.11f,p); return; }
            if ("calendar".equals(type)) { RectF r=new RectF(w*.16f,h*.22f,w*.84f,h*.82f); c.drawRoundRect(r,m*.10f,m*.10f,p); c.drawLine(w*.16f,h*.42f,w*.84f,h*.42f,p); c.drawLine(w*.32f,h*.12f,w*.32f,h*.30f,p); c.drawLine(w*.68f,h*.12f,w*.68f,h*.30f,p); return; }
            if ("friends".equals(type)) { c.drawCircle(cx-m*.18f,cy-m*.08f,m*.11f,p); c.drawCircle(cx+m*.18f,cy-m*.08f,m*.11f,p); c.drawArc(new RectF(cx-m*.43f,cy+m*.05f, cx-m*.02f, cy+m*.46f),205,130,false,p); c.drawArc(new RectF(cx+m*.02f,cy+m*.05f, cx+m*.43f, cy+m*.46f),205,130,false,p); return; }
            if ("rooms".equals(type)) {
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                float scale = Math.min(w, h) * 0.88f / 512f;
                float ox = (w - 512f * scale) / 2f;
                float oy = (h - 512f * scale) / 2f;
                p.setStrokeWidth(Math.max(2.8f, Math.min(w, h) * .118f));
                Path path = new Path();
                path.moveTo(ox + 256f * scale, oy + 18f * scale);
                path.lineTo(ox + 18f * scale, oy + 138f * scale);
                path.lineTo(ox + 18f * scale, oy + 398f * scale);
                path.lineTo(ox + 256f * scale, oy + 494f * scale);
                path.lineTo(ox + 494f * scale, oy + 398f * scale);
                path.lineTo(ox + 494f * scale, oy + 138f * scale);
                path.close();
                path.moveTo(ox + 256f * scale, oy + 18f * scale);
                path.lineTo(ox + 256f * scale, oy + 300f * scale);
                path.moveTo(ox + 18f * scale, oy + 398f * scale);
                path.lineTo(ox + 256f * scale, oy + 300f * scale);
                path.moveTo(ox + 256f * scale, oy + 300f * scale);
                path.lineTo(ox + 494f * scale, oy + 398f * scale);
                c.drawPath(path, p);
                return;
            }
            if ("groups".equals(type)) { c.drawCircle(cx,cy,m*.36f,p); c.drawCircle(cx,cy,m*.17f,p); Path chk=new Path(); chk.moveTo(cx-m*.10f,cy); chk.lineTo(cx-m*.02f,cy+m*.09f); chk.lineTo(cx+m*.15f,cy-m*.11f); c.drawPath(chk,p); return; }
            if ("photos".equals(type)) { RectF r=new RectF(w*.16f,h*.22f,w*.84f,h*.78f); c.drawRoundRect(r,m*.09f,m*.09f,p); c.drawCircle(w*.32f,h*.38f,m*.06f,p); c.drawLine(w*.22f,h*.68f,w*.43f,h*.52f,p); c.drawLine(w*.43f,h*.52f,w*.78f,h*.68f,p); return; }
            if ("star".equals(type)) { Path path=new Path(); for(int i=0;i<10;i++){ double a=-Math.PI/2+i*Math.PI/5; float rr=(i%2==0)?m*.40f:m*.17f; float x=cx+(float)Math.cos(a)*rr, y=cy+(float)Math.sin(a)*rr; if(i==0) path.moveTo(x,y); else path.lineTo(x,y);} path.close(); c.drawPath(path,p); return; }
            if ("badge".equals(type)) {
                p.setShader(null);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(Math.max(2f, m*.075f));
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);

                for (int i=0;i<8;i++){
                    double a = -Math.PI/2 + i*Math.PI/4;
                    float px = cx + (float)Math.cos(a)*m*.20f;
                    float py = cy + (float)Math.sin(a)*m*.20f;
                    RectF petalOval = new RectF(px-m*.11f, py-m*.17f, px+m*.11f, py+m*.17f);
                    c.save();
                    c.rotate((float)Math.toDegrees(a)+90, px, py);
                    c.drawOval(petalOval, p);
                    c.restore();
                }
                p.setStyle(Paint.Style.FILL);
                p.setColor(lightTheme ? Color.rgb(33, 33, 33) : Color.WHITE);
                c.drawCircle(cx, cy, m*.085f, p);
                return;
            }
            if ("level".equals(type)) { p.setStyle(Paint.Style.FILL); Path path=new Path(); path.moveTo(cx,h*.16f); path.lineTo(w*.80f,h*.48f); path.lineTo(w*.62f,h*.48f); path.lineTo(w*.62f,h*.84f); path.lineTo(w*.38f,h*.84f); path.lineTo(w*.38f,h*.48f); path.lineTo(w*.20f,h*.48f); path.close(); c.drawPath(path,p); }
        }
    }


    public class NoAdsBannerDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left + dp(1), b.top + dp(1), b.right - dp(1), b.bottom - dp(1));
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom,
                    new int[]{Color.rgb(133,83,235), Color.rgb(132,52,217), Color.rgb(68,36,179)},
                    new float[]{0f,.52f,1f}, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(r, dp(18), dp(18), p);
            p.setShader(null);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(24,255,255,255));
            c.drawCircle(r.left + r.height()*.94f, r.top + r.height()*.08f, r.height()*.42f, p);
            p.setColor(Color.argb(18,255,255,255));
            c.drawCircle(r.right - r.height()*.84f, r.bottom + r.height()*.04f, r.height()*.60f, p);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(105,255,255,255));
            c.drawRoundRect(new RectF(r.left+1, r.top+1, r.right-1, r.bottom-1), dp(18), dp(18), p);
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(130,255,255,255));
            c.drawCircle(r.left + r.height()*.62f, r.top + r.height()*.24f, r.height()*.018f, p);
            c.drawCircle(r.left + r.height()*.92f, r.top + r.height()*.12f, r.height()*.022f, p);
            c.drawCircle(r.left + r.height()*.36f, r.bottom - r.height()*.22f, r.height()*.016f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class PremiumCrownDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); float cx=b.centerX(), cy=b.centerY(), m=Math.min(b.width(),b.height());
            p.setStyle(Paint.Style.FILL); p.setColor(Color.WHITE); c.drawCircle(cx,cy,m*.46f,p);
            p.setColor(Color.rgb(255,190,0));
            Path crown=new Path(); crown.moveTo(cx-m*.28f,cy+m*.13f); crown.lineTo(cx-m*.33f,cy-m*.15f); crown.lineTo(cx-m*.12f,cy-m*.04f); crown.lineTo(cx,cy-m*.26f); crown.lineTo(cx+m*.12f,cy-m*.04f); crown.lineTo(cx+m*.33f,cy-m*.15f); crown.lineTo(cx+m*.28f,cy+m*.13f); crown.close(); c.drawPath(crown,p);
            c.drawRoundRect(new RectF(cx-m*.27f,cy+m*.17f,cx+m*.27f,cy+m*.25f),m*.025f,m*.025f,p); c.drawCircle(cx-m*.33f,cy-m*.16f,m*.035f,p); c.drawCircle(cx,cy-m*.27f,m*.035f,p); c.drawCircle(cx+m*.33f,cy-m*.16f,m*.035f,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class TinyNoAdDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); float cx=b.centerX(), cy=b.centerY(), m=Math.min(b.width(),b.height());
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setStrokeWidth(Math.max(1.3f,m*.075f)); p.setColor(Color.rgb(255,193,24));
            RectF r=new RectF(cx-m*.30f,cy-m*.22f,cx+m*.30f,cy+m*.22f); c.drawRoundRect(r,m*.07f,m*.07f,p); p.setTextAlign(Paint.Align.CENTER); p.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD)); p.setTextSize(m*.20f); p.setStyle(Paint.Style.FILL); c.drawText("AD",cx,cy+m*.075f,p); p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(Math.max(1.6f,m*.085f)); c.drawLine(cx-m*.38f,cy+m*.34f,cx+m*.38f,cy-m*.34f,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class PremiumArrowDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b=getBounds(); float cx=b.centerX(), cy=b.centerY(), m=Math.min(b.width(),b.height());
            p.setShader(new LinearGradient(b.left,b.top,b.right,b.bottom,Color.rgb(185,82,255),Color.rgb(119,65,236),Shader.TileMode.CLAMP)); p.setStyle(Paint.Style.FILL); c.drawCircle(cx,cy,m*.46f,p); p.setShader(null);
            p.setStyle(Paint.Style.STROKE); p.setStrokeCap(Paint.Cap.ROUND); p.setStrokeJoin(Paint.Join.ROUND); p.setStrokeWidth(Math.max(2.4f,m*.08f)); p.setColor(Color.WHITE); Path a=new Path(); a.moveTo(cx-m*.09f,cy-m*.19f); a.lineTo(cx+m*.12f,cy); a.lineTo(cx-m*.09f,cy+m*.19f); c.drawPath(a,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class RewardVideoDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float w = b.width(), h = b.height(), cx = b.centerX(), cy = b.centerY(), m = Math.min(w, h);
            RectF bgRect = new RectF(b.left + m*.10f, b.top + m*.10f, b.right - m*.10f, b.bottom - m*.10f);
            p.setShader(new LinearGradient(bgRect.left, bgRect.top, bgRect.right, bgRect.bottom, purple2, purple, Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(bgRect, m*.24f, m*.24f, p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(1f, m*.035f));
            p.setColor(Color.argb(80,255,255,255));
            c.drawRoundRect(bgRect, m*.24f, m*.24f, p);

            RectF screenRect = new RectF(cx-m*.25f, cy-m*.17f, cx+m*.25f, cy+m*.17f);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Math.max(2f, m*.06f));
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(Color.WHITE);
            c.drawRoundRect(screenRect, m*.06f, m*.06f, p);

            Path play = new Path();
            play.moveTo(cx-m*.055f, cy-m*.080f);
            play.lineTo(cx-m*.055f, cy+m*.080f);
            play.lineTo(cx+m*.100f, cy);
            play.close();
            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.WHITE);
            c.drawPath(play, p);

            p.setStrokeWidth(Math.max(1.5f, m*.035f));
            c.drawLine(cx-m*.09f, cy+m*.26f, cx+m*.09f, cy+m*.26f, p);
            c.drawLine(cx, cy+m*.16f, cx, cy+m*.26f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }




    public class VisualSaveLookDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float m = Math.min(b.width(), b.height());
            float cx = b.centerX(), cy = b.centerY();
            int iconColor = lightTheme ? Color.rgb(42,42,46) : Color.argb(242,255,255,255);
            int cutColor = lightTheme ? Color.WHITE : bg;

            p.setShader(null);
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.FILL);
            p.setColor(iconColor);

            RectF body = new RectF(cx - m * .34f, cy - m * .35f, cx + m * .34f, cy + m * .35f);
            float r = m * .085f;
            Path disk = new Path();
            disk.moveTo(body.left + r, body.top);
            disk.lineTo(body.right - m * .16f, body.top);
            disk.lineTo(body.right, body.top + m * .16f);
            disk.lineTo(body.right, body.bottom - r);
            disk.quadTo(body.right, body.bottom, body.right - r, body.bottom);
            disk.lineTo(body.left + r, body.bottom);
            disk.quadTo(body.left, body.bottom, body.left, body.bottom - r);
            disk.lineTo(body.left, body.top + r);
            disk.quadTo(body.left, body.top, body.left + r, body.top);
            disk.close();
            c.drawPath(disk, p);

            p.setColor(cutColor);
            RectF topSlot = new RectF(cx - m * .20f, cy - m * .27f, cx + m * .21f, cy - m * .10f);
            c.drawRoundRect(topSlot, m * .04f, m * .04f, p);

            p.setColor(iconColor);
            RectF topMark = new RectF(cx + m * .10f, cy - m * .245f, cx + m * .15f, cy - m * .125f);
            c.drawRoundRect(topMark, m * .02f, m * .02f, p);

            p.setColor(cutColor);
            c.drawCircle(cx, cy + m * .16f, m * .115f, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class VisualSavedLooksDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            float m = Math.min(b.width(), b.height());
            float cx = b.centerX(), cy = b.centerY();
            int iconColor = lightTheme ? Color.rgb(42,42,46) : Color.argb(242,255,255,255);

            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setStrokeWidth(Math.max(dp(2), m * 0.105f));
            p.setColor(iconColor);

            Path hook = new Path();
            hook.moveTo(cx, cy - m * .31f);
            hook.cubicTo(cx + m * .20f, cy - m * .32f, cx + m * .20f, cy - m * .08f, cx + m * .035f, cy - m * .055f);
            c.drawPath(hook, p);

            c.drawLine(cx, cy - m * .04f, cx, cy + m * .10f, p);

            Path hanger = new Path();
            hanger.moveTo(cx, cy + m * .10f);
            hanger.lineTo(cx - m * .36f, cy + m * .38f);
            hanger.quadTo(cx - m * .40f, cy + m * .42f, cx - m * .31f, cy + m * .42f);
            hanger.lineTo(cx + m * .31f, cy + m * .42f);
            hanger.quadTo(cx + m * .40f, cy + m * .42f, cx + m * .36f, cy + m * .38f);
            hanger.close();
            c.drawPath(hanger, p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);} @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);} @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class ThemeIconButtonDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        boolean sun;
        boolean selected;
        ThemeIconButtonDrawable(boolean sun, boolean selected) {
            this.sun = sun;
            this.selected = selected;
        }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left + dp(1), b.top + dp(1), b.right - dp(1), b.bottom - dp(1));
            float rad = dp(14);

            p.setStyle(Paint.Style.FILL);
            if (selected) {
                LinearGradient g = new LinearGradient(r.left, r.top, r.right, r.bottom, purple2, purple, Shader.TileMode.CLAMP);
                p.setShader(g);
                c.drawRoundRect(r, rad, rad, p);
                p.setShader(null);
            } else {
                p.setColor(lightTheme ? Color.rgb(250,250,250) : Color.argb(34, 255,255,255));
                c.drawRoundRect(r, rad, rad, p);
            }

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(selected ? Color.argb(120,255,255,255) : (lightTheme ? Color.rgb(218,218,218) : Color.argb(30,255,255,255)));
            c.drawRoundRect(r, rad, rad, p);

            float cx = r.centerX();
            float cy = r.centerY();
            p.setStrokeCap(Paint.Cap.ROUND);
            p.setStrokeJoin(Paint.Join.ROUND);
            p.setColor(selected ? Color.WHITE : (lightTheme ? Color.rgb(54,54,62) : Color.argb(220,255,255,255)));

            if (sun) {
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(cx, cy, (dp(5) + Math.max(1, dp(1) / 5)), p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth((dp(1) + Math.max(1, dp(1) / 2)));
                for (int i=0; i<8; i++) {
                    double a = i * Math.PI / 4.0;
                    float x1 = cx + (float)Math.cos(a) * dp(10);
                    float y1 = cy + (float)Math.sin(a) * dp(10);
                    float x2 = cx + (float)Math.cos(a) * dp(14);
                    float y2 = cy + (float)Math.sin(a) * dp(14);
                    c.drawLine(x1, y1, x2, y2, p);
                }
            } else {
                p.setStyle(Paint.Style.FILL);
                c.drawCircle(cx - dp(1), cy + dp(1), (dp(10) + Math.max(1, dp(1) / 2)), p);
                p.setColor(selected ? purple : (lightTheme ? Color.rgb(250,250,250) : Color.rgb(40, 28, 54)));
                c.drawCircle(cx + dp(4), cy - dp(3), (dp(10) + Math.max(1, dp(1) / 5)), p);
            }
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class TutorialOverlayDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        int step;
        TutorialOverlayDrawable(int s) { step = s; }
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF hole;
            if (step == 0) {
                // 4 itens na bottom nav: Home, Visuais, Favoritos, Configurações.
                // Considera o padding horizontal real da barra inferior para centralizar no botão Configurações.
                float navLeft = b.left + dp(12);
                float navWidth = b.width() - dp(24);
                float cx = navLeft + navWidth * (7f / 8f);
                float cy = b.bottom - dp(28);
                hole = new RectF(cx - dp(28), cy - dp(28), cx + dp(28), cy + dp(28));
            } else if (step == 1) {
                hole = new RectF(b.left + dp(12), b.top + dp(132), b.right - dp(12), b.top + dp(252));
            } else {
                float cx = b.left + dp(29);
                float cy = b.top + dp(35);
                hole = new RectF(cx - dp(34), cy - dp(34), cx + dp(34), cy + dp(34));
            }

            Path overlayPath = new Path();
            overlayPath.setFillType(Path.FillType.EVEN_ODD);
            overlayPath.addRect(new RectF(b.left, b.top, b.right, b.bottom), Path.Direction.CW);
            overlayPath.addRoundRect(hole, dp(24), dp(24), Path.Direction.CW);

            p.setStyle(Paint.Style.FILL);
            p.setColor(Color.argb(218, 0, 0, 0));
            c.drawPath(overlayPath, p);

            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(2));
            p.setColor(Color.argb(245, 210, 112, 255));
            c.drawRoundRect(hole, dp(24), dp(24), p);

            p.setStrokeWidth(dp(8));
            p.setColor(Color.argb(44, 210, 112, 255));
            c.drawRoundRect(hole, dp(28), dp(28), p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }

    public class TutorialCardDrawable extends Drawable {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        @Override public void draw(Canvas c) {
            Rect b = getBounds();
            RectF r = new RectF(b.left, b.top, b.right, b.bottom);
            p.setShader(new LinearGradient(r.left, r.top, r.right, r.bottom, Color.rgb(35, 20, 55), Color.rgb(78, 28, 112), Shader.TileMode.CLAMP));
            p.setStyle(Paint.Style.FILL);
            c.drawRoundRect(r, dp(26), dp(26), p);
            p.setShader(null);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(dp(1));
            p.setColor(Color.argb(70,255,255,255));
            c.drawRoundRect(r, dp(26), dp(26), p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);}
        @Override public void setColorFilter(android.graphics.ColorFilter f){p.setColorFilter(f);}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }



}
