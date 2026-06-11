package io.github.silentdevelopment.atlas.bind;

import io.github.silentdevelopment.atlas.ConfigCodec;
import io.github.silentdevelopment.atlas.ConfigLoader;
import io.github.silentdevelopment.atlas.core.ConfigLoaders;
import io.github.silentdevelopment.atlas.core.DefaultConfigLoader;
import io.github.silentdevelopment.atlas.io.ConfigResource;
import io.github.silentdevelopment.atlas.io.ConfigSink;
import io.github.silentdevelopment.atlas.io.ConfigSource;

import java.io.IOException;
import java.util.Objects;

public final class BindConfigRequest {

    private ConfigSource source;
    private ConfigSink sink;
    private ConfigCodec codec;
    private ConfigSource defaults;
    private boolean copyDefaultsIfMissing;

    BindConfigRequest() {
    }

    public BindConfigRequest source(ConfigSource source) {
        this.source = Objects.requireNonNull(source, "source");
        return this;
    }

    public BindConfigRequest sink(ConfigSink sink) {
        this.sink = Objects.requireNonNull(sink, "sink");
        return this;
    }

    public BindConfigRequest resource(ConfigResource resource) {
        Objects.requireNonNull(resource, "resource");

        this.source = resource;
        this.sink = resource;
        return this;
    }

    public BindConfigRequest codec(ConfigCodec codec) {
        this.codec = Objects.requireNonNull(codec, "codec");
        return this;
    }

    public BindConfigRequest defaults(ConfigSource defaults) {
        this.defaults = Objects.requireNonNull(defaults, "defaults");
        return this;
    }

    public BindConfigRequest copyDefaults() {
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

    public <T> BoundConfigRequest<T> bind(Class<T> type) {
        Objects.requireNonNull(type, "type");

        return new BoundConfigRequest<>(this, type);
    }

    public BoundConfigGroupRequest bind(Class<?>... types) {
        Objects.requireNonNull(types, "types");

        return new BoundConfigGroupRequest(this, types);
    }

    <T> BoundConfig<T> loadBound(Class<T> type) throws IOException {
        return BoundConfig.load(loader(), type);
    }

    BoundConfigGroup loadGroup(Class<?>... types) throws IOException {
        return BoundConfigGroup.load(loader(), types);
    }

}