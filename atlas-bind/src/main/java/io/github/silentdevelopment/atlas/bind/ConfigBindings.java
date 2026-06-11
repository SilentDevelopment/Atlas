package io.github.silentdevelopment.atlas.bind;

import io.github.silentdevelopment.atlas.ConfigLoader;

import java.io.IOException;
import java.util.Objects;

public final class ConfigBindings {

    private ConfigBindings() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static BindConfigRequest config() {
        return new BindConfigRequest();
    }

    public static <T> BoundConfig<T> load(ConfigLoader loader, Class<T> type) throws IOException {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(type, "type");

        return BoundConfig.load(loader, type);
    }

    public static BoundConfigGroup loadGroup(ConfigLoader loader, Class<?>... types) throws IOException {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(types, "types");

        return BoundConfigGroup.load(loader, types);
    }

}