package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.io.ConfigSink;
import io.github.silentdevelopment.atlas.io.ConfigSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public final class ConfigDefaults {

    private ConfigDefaults() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static void copy(ConfigSource source, ConfigSink sink) throws IOException {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(sink, "sink");

        try (InputStream input = source.openInput(); OutputStream output = sink.openOutput()) {
            input.transferTo(output);
        }
    }

}