package com.toxic.search;

import android.app.Activity;
import android.os.Build;

/** Keeps API 33 types out of classes loaded on Android 6. */
final class BackNavigationController {
    interface Handler { boolean handle(); }
    private Object callback;

    void install(Activity activity, Handler handler) {
        if (Build.VERSION.SDK_INT >= 33 && callback == null) callback = Api33.install(activity, handler);
    }

    void dispose(Activity activity) {
        if (Build.VERSION.SDK_INT >= 33 && callback != null) Api33.dispose(activity, callback);
        callback = null;
    }

    @android.annotation.TargetApi(33)
    private static final class Api33 {
        static Object install(Activity activity, Handler handler) {
            android.window.OnBackInvokedCallback callback = () -> {
                if (!handler.handle()) {
                    if (activity.isTaskRoot()) activity.moveTaskToBack(true);
                    else activity.finishAfterTransition();
                }
            };
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
            return callback;
        }

        static void dispose(Activity activity, Object callback) {
            activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(
                    (android.window.OnBackInvokedCallback) callback);
        }
    }
}
