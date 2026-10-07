package com.toxic.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import java.io.File;
import java.util.concurrent.TimeUnit;

/** Bounded, cached images for work outside an ImageView. Call on a worker thread. */
final class FavoriteAvatarLoader {
    private static final int ICON_SIZE = 96;

    static Bitmap load(Context context, Object model) {
        if (context == null || model == null || Looper.myLooper() == Looper.getMainLooper()) return null;
        Context app = context.getApplicationContext();
        FutureTarget<Bitmap> target = null;
        try {
            target = Glide.with(app).asBitmap().load(model).fitCenter()
                    .disallowHardwareConfig().timeout(5000).submit(ICON_SIZE, ICON_SIZE);
            Bitmap bitmap = target.get(6, TimeUnit.SECONDS);
            // The notification/cache owns this small copy; Glide can reuse its bitmap.
            return bitmap == null ? null : bitmap.copy(Bitmap.Config.ARGB_8888, false);
        } catch (InterruptedException cancelled) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception unavailable) {
            return null;
        } finally {
            if (target != null) Glide.with(app).clear(target);
        }
    }

    static Bitmap notificationIcon(Context context, String url, File legacyCache) {
        Bitmap bitmap = url == null || url.isEmpty() ? null : load(context, url);
        if (bitmap == null && !Thread.currentThread().isInterrupted()
                && legacyCache != null && legacyCache.isFile()) bitmap = load(context, legacyCache);
        if (bitmap == null && !Thread.currentThread().isInterrupted()) bitmap = load(context, R.drawable.pre_load_head);
        return bitmap;
    }
}
