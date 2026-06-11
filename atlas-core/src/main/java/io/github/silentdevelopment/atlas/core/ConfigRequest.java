package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.ConfigCodec;
import io.github.silentdevelopment.atlas.ConfigLoader;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigLoadException;
import io.github.silentdevelopment.atlas.io.ConfigResource;
import io.github.silentdevelopment.atlas.io.ConfigSink;
import io.github.silentdevelopment.atlas.io.ConfigSource;

import java.io.IOException;
import java.util.Objects;

public final class ConfigRequest {

    private ConfigSource source;
    private ConfigSink sink;
    private ConfigCodec codec;
    private ConfigSource defaults;
    private boolean copyDefaultsIfMissing;

    ConfigRequest() {
    }

    public ConfigRequest source(ConfigSource source) {
        this.source = Objects.requireNonNull(source, "source");
        return this;
    }

    public ConfigRequest sink(ConfigSink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
        return this;
    }

    public ConfigRequest resource(ConfigResource resource) {
        Objects.requireNonNull(resource, "resource");

        this.source = resource;
        this.sink = resource;
        return this;
    }

    public ConfigRequest codec(ConfigCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
        return this;
    }

    public ConfigRequest defaults(ConfigSource defaults) {
        this.defaults = Objects.requireNonNull(defaults, "defaults");
        return this;
    }

    public ConfigRequest copyDefaults() {
        this.copyDefaultsIfMissing = true;
        return this;
    }

    public ConfigLoader loader() {
        DefaultConfigLoader.Builder builder = ConfigLoaders.builder();

        if (source != null) {
            builder.source(source);
        }

        if (sink != null) {
            builder.sink(sink);
        }

        if (codec != null) {
            builder.codec(codec);
        }

        if (defaults != null) {
            builder.defaults(defaults);
        }

        builder.copyDefaultsIfMissing(copyDefaultsIfMissing);

        return builder.build();
    }

    public ConfigDocument loadDocument() throws IOException {
        return loader().load();
    }

    public MutableConfigDocument loadMutable() throws IOException {
        ConfigDocument document = loadDocument();

        if (!(document instanceof MutableConfigDocument mutable)) {
            throw new ConfigLoadException("Loaded config document is not mutable.");
        }

        return mutable;
    }

    public Config load() throws IOException {
        ConfigLoader loader = loader();
        ConfigDocument document = loader.load();

        if (!(document instanceof MutableConfigDocument mutable)) {
            throw new ConfigLoadException("Loaded config document is not mutable.");
        }

        return new Config(loader, mutable);
    }

}