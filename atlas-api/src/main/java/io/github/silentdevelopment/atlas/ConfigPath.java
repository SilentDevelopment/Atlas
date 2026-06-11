package io.github.silentdevelopment.atlas;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public record ConfigPath(List<String> parts) {

    public ConfigPath {
        Objects.requireNonNull(parts, "parts");

        for (String part : parts) {
            if (part == null) {
                throw new NullPointerException("path part");
            }

            if (part.isBlank()) {
                throw new IllegalArgumentException("path part cannot be blank.");
            }
        }

        parts = List.copyOf(parts);
    }

    public static ConfigPath of(String... parts) {
        Objects.requireNonNull(parts, "parts");
        return new ConfigPath(Arrays.asList(parts));
    }

    public static ConfigPath parse(String path) {
        Objects.requireNonNull(path, "path");

        if (path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank.");
        }

        return new ConfigPath(Arrays.asList(path.split("\\.", -1)));
    }

    public ConfigPath append(String part) {
        Objects.requireNonNull(part, "part");

        if (part.isBlank()) {
            throw new IllegalArgumentException("part cannot be blank.");
        }

        List<String> copy = new java.util.ArrayList<>(parts);
        copy.add(part);
        return new ConfigPath(copy);
    }

    @Override
    public String toString() {
        return String.join(".", parts);
    }

}