package io.github.silentdevelopment.atlas.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public final class UrlConfigSource implements ConfigSource {

    private final URL url;

    private UrlConfigSource(URL url) {
        this.url = Objects.requireNonNull(url, "url");
    }

    public static UrlConfigSource of(URL url) {
        return new UrlConfigSource(url);
    }

    @Override
    public String name() {
        return url.toExternalForm();
    }

    @Override
    public InputStream openInput() throws IOException {
        return url.openStream();
    }

}