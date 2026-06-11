package io.github.silentdevelopment.atlas.io;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class StringConfigSource implements ConfigSource {

    private final String name;
    private final String value;
    private final Charset charset;

    private StringConfigSource(String name, String value, Charset charset) {
        this.name = Objects.requireNonNull(name, "name");
        this.value = Objects.requireNonNull(value, "value");
        this.charset = Objects.requireNonNull(charset, "charset");
    }

    public static StringConfigSource of(String value) {
        return new StringConfigSource("string", value, StandardCharsets.UTF_8);
    }

    public static StringConfigSource of(String name, String value, Charset charset) {
        return new StringConfigSource(name, value, charset);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public InputStream openInput() {
        return new ByteArrayInputStream(value.getBytes(charset));
    }

}