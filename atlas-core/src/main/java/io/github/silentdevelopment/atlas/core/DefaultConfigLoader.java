package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.ConfigCodec;
import io.github.silentdevelopment.atlas.ConfigDecoder;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.ConfigEncoder;
import io.github.silentdevelopment.atlas.ConfigLoader;
import io.github.silentdevelopment.atlas.io.ConfigResource;
import io.github.silentdevelopment.atlas.io.ConfigSink;
import io.github.silentdevelopment.atlas.io.ConfigSource;
import io.github.silentdevelopment.atlas.exception.ConfigLoadException;
import io.github.silentdevelopment.atlas.exception.ConfigSaveException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public final class DefaultConfigLoader implements ConfigLoader {

    private final ConfigSource source;
    private final ConfigSink sink;
    private final ConfigDecoder decoder;
    private final ConfigEncoder encoder;
    private final ConfigSource defaults;
    private final boolean copyDefaultsIfMissing;

    private DefaultConfigLoader(ConfigSource source, ConfigSink sink, ConfigDecoder decoder, ConfigEncoder encoder, ConfigSource defaults, boolean copyDefaultsIfMissing) {
        this.source = source;
        this.sink = sink;
        this.decoder = decoder;
        this.encoder = encoder;
        this.defaults = defaults;
        this.copyDefaultsIfMissing = copyDefaultsIfMissing;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public ConfigDocument load() throws IOException, ConfigLoadException {
        if (source == null) {
            throw new ConfigLoadException("No config source was provided.");
        }

        if (decoder == null) {
            throw new ConfigLoadException("No config decoder was provided.");
        }

        copyDefaultsIfNeeded();

        try (InputStream input = source.openInput()) {
            return decoder.decode(input);
        }
    }

    @Override
    public void save(ConfigDocument document) throws IOException, ConfigSaveException {
        Objects.requireNonNull(document, "document");

        if (sink == null) {
            throw new ConfigSaveException("No config sink was provided.");
        }

        if (encoder == null) {
            throw new ConfigSaveException("No config encoder was provided.");
        }

        try (OutputStream output = sink.openOutput()) {
            encoder.encode(document, output);
        } catch (ConfigSaveException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ConfigSaveException("Failed to save config to " + sink.name() + ".", exception);
        }
    }

    private void copyDefaultsIfNeeded() throws IOException {
        if (!copyDefaultsIfMissing) {
            return;
        }

        if (defaults == null) {
            return;
        }

        if (!(source instanceof ConfigResource resource)) {
            return;
        }

        if (resource.exists()) {
            return;
        }

        ConfigDefaults.copy(defaults, resource);
    }

    public static final class Builder {

        private ConfigSource source;
        private ConfigSink sink;
        private ConfigDecoder decoder;
        private ConfigEncoder encoder;
        private ConfigSource defaults;
        private boolean copyDefaultsIfMissing;

        private Builder() {
        }

        public Builder source(ConfigSource source) {
            this.source = Objects.requireNonNull(source, "source");
            return this;
        }

        public Builder sink(ConfigSink sink) {
            this.sink = Objects.requireNonNull(sink, "sink");
            return this;
        }

        public Builder resource(ConfigResource resource) {
            Objects.requireNonNull(resource, "resource");
            this.source = resource;
            this.sink = resource;
            return this;
        }

        public Builder decoder(ConfigDecoder decoder) {
            this.decoder = Objects.requireNonNull(decoder, "decoder");
            return this;
        }

        public Builder encoder(ConfigEncoder encoder) {
            this.encoder = Objects.requireNonNull(encoder, "encoder");
            return this;
        }

        public Builder codec(ConfigCodec codec) {
            Objects.requireNonNull(codec, "codec");
            this.decoder = codec;
            this.encoder = codec;
            return this;
        }

        public Builder defaults(ConfigSource defaults) {
            this.defaults = Objects.requireNonNull(defaults, "defaults");
            return this;
        }

        public Builder copyDefaultsIfMissing(boolean copyDefaultsIfMissing) {
            this.copyDefaultsIfMissing = copyDefaultsIfMissing;
            return this;
        }

        public DefaultConfigLoader build() {
            return new DefaultConfigLoader(source, sink, decoder, encoder, defaults, copyDefaultsIfMissing);
        }

    }

}