package com.toxic.search;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public final class HabbodexImageTest {
    private static final String ICON = "https://habbodex.com/images/furni/clothing_r20_inuit/clothing_r20_inuit_icon.png";

    @Test public void originalAndIconUrlsUseTheAuthenticatedSource() {
        for (String url : new String[]{ICON, ICON.replace("_icon.png", ".png")}) {
            AtomicInteger calls = new AtomicInteger();
            byte[] png = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
            HabbodexImage model = new HabbodexImage(url, requested -> {
                assertEquals(url, requested);
                calls.incrementAndGet();
                return png;
            });
            ModelLoader.LoadData<ByteBuffer> request = new HabbodexImage.Loader().buildLoadData(model, 64, 64, new Options());
            assertEquals(DataSource.REMOTE, request.fetcher.getDataSource());
            final ByteBuffer[] received = {null};
            request.fetcher.loadData(Priority.NORMAL, new DataFetcher.DataCallback<ByteBuffer>() {
                @Override public void onDataReady(ByteBuffer data) { received[0] = data; }
                @Override public void onLoadFailed(Exception error) { throw new AssertionError(error); }
            });
            assertArrayEquals(png, received[0].array());
            assertEquals(1, calls.get());
        }
    }

    @Test public void theCacheKeyDoesNotDependOnTheActivityOrTransportInstance() {
        HabbodexImage first = new HabbodexImage(ICON, url -> new byte[]{1});
        HabbodexImage second = new HabbodexImage(ICON, url -> new byte[]{2});
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        HabbodexImage.Loader loader = new HabbodexImage.Loader();
        assertEquals(loader.buildLoadData(first, 64, 64, new Options()).sourceKey,
                loader.buildLoadData(second, 128, 128, new Options()).sourceKey);
    }

    @Test public void cancellingBeforeLoadingDoesNotRequestAnImage() {
        AtomicInteger calls = new AtomicInteger();
        HabbodexImage model = new HabbodexImage(ICON, url -> { calls.incrementAndGet(); return new byte[]{1}; });
        HabbodexImage.Fetcher fetcher = new HabbodexImage.Fetcher(model);
        fetcher.cancel();
        fetcher.loadData(Priority.NORMAL, new DataFetcher.DataCallback<ByteBuffer>() {
            @Override public void onDataReady(ByteBuffer data) { fail("Cancelled request delivered data"); }
            @Override public void onLoadFailed(Exception error) { fail("Cancelled request delivered an error"); }
        });
        assertEquals(0, calls.get());
    }

    @Test public void failuresRemainImageFailuresWithoutChangingCatalogueData() {
        HabbodexImage model = new HabbodexImage(ICON, url -> { throw new java.io.IOException("Unavailable"); });
        final Exception[] received = {null};
        new HabbodexImage.Fetcher(model).loadData(Priority.NORMAL, new DataFetcher.DataCallback<ByteBuffer>() {
            @Override public void onDataReady(ByteBuffer data) { fail("Unexpected image"); }
            @Override public void onLoadFailed(Exception error) { received[0] = error; }
        });
        assertTrue(received[0] instanceof java.io.IOException);
    }

    @Test public void onlyCatalogueImageUrlsAreAccepted() {
        assertTrue(HabbodexImage.allowed(ICON));
        assertFalse(HabbodexImage.allowed(ICON.replace("https://", "http://")));
        assertFalse(HabbodexImage.allowed(ICON.replace("habbodex.com", "example.test")));
        assertFalse(HabbodexImage.allowed("https://habbodex.com/api/v1/furnidex/furni/clothing_r20_inuit"));
        assertFalse(HabbodexImage.allowed("https://habbodex.com/images/furni/../secret.png"));
    }
}
