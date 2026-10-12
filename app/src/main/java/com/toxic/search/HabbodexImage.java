package com.toxic.search;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.signature.ObjectKey;
import java.nio.ByteBuffer;

/** Uses the authenticated HabboDex transport while Glide owns decoding and caching. */
final class HabbodexImage {
    interface Source { byte[] fetch(String url) throws Exception; }
    final String url;
    final Source source;
    private static boolean registered;

    HabbodexImage(String url, Source source) {
        if (!allowed(url) || source == null) throw new IllegalArgumentException("Invalid catalogue image");
        this.url = url;
        this.source = source;
    }

    static boolean allowed(String url) {
        return url != null && url.matches("https://habbodex\\.com/images/furni/[a-z][a-z0-9_]{2,119}/[a-z][a-z0-9_]{2,124}\\.png");
    }

    static synchronized void register(Glide glide) {
        if (registered) return;
        glide.getRegistry().append(HabbodexImage.class, ByteBuffer.class, new Factory());
        registered = true;
    }

    @Override public boolean equals(Object other) {
        return other instanceof HabbodexImage && url.equals(((HabbodexImage) other).url);
    }
    @Override public int hashCode() { return url.hashCode(); }

    static final class Loader implements ModelLoader<HabbodexImage, ByteBuffer> {
        @Override public boolean handles(HabbodexImage model) { return allowed(model.url); }
        @Override public LoadData<ByteBuffer> buildLoadData(HabbodexImage model, int width, int height, Options options) {
            return new LoadData<>(new ObjectKey(model.url), new Fetcher(model));
        }
    }

    static final class Fetcher implements DataFetcher<ByteBuffer> {
        private final HabbodexImage model;
        private volatile boolean cancelled;
        Fetcher(HabbodexImage model) { this.model = model; }
        @Override public void loadData(Priority priority, DataCallback<? super ByteBuffer> callback) {
            if (cancelled) return;
            try {
                byte[] bytes = model.source.fetch(model.url);
                if (bytes == null || bytes.length == 0) throw new java.io.IOException("Empty catalogue image");
                if (!cancelled) callback.onDataReady(ByteBuffer.wrap(bytes));
            } catch (Exception error) {
                if (!cancelled) callback.onLoadFailed(error);
            }
        }
        @Override public void cancel() { cancelled = true; }
        @Override public void cleanup() {}
        @Override public Class<ByteBuffer> getDataClass() { return ByteBuffer.class; }
        @Override public DataSource getDataSource() { return DataSource.REMOTE; }
    }

    private static final class Factory implements ModelLoaderFactory<HabbodexImage, ByteBuffer> {
        @Override public ModelLoader<HabbodexImage, ByteBuffer> build(MultiModelLoaderFactory multi) { return new Loader(); }
        @Override public void teardown() {}
    }
}
