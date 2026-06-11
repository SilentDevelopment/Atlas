package io.github.silentdevelopment.atlas;

import java.util.Objects;

public final class ConfigKey<T> {

    private final ConfigPath path;
    private final Class<T> type;
    private final T defaultValue;
    private final boolean hasDefault;

    private ConfigKey(ConfigPath path, Class<T> type, T defaultValue, boolean hasDefault) {
        this.path = Objects.requireNonNull(path, "path");
        this.type = Objects.requireNonNull(type, "type");
        this.defaultValue = defaultValue;
        this.hasDefault = hasDefault;
    }

    public static ConfigKeyBuilder<String> string(ConfigPath path) {
        return new ConfigKeyBuilder<>(path, String.class);
    }

    public static ConfigKeyBuilder<Integer> integer(ConfigPath path) {
        return new ConfigKeyBuilder<>(path, Integer.class);
    }

    public static ConfigKeyBuilder<Long> longNumber(ConfigPath path) {
        return new ConfigKeyBuilder<>(path, Long.class);
    }

    public static ConfigKeyBuilder<Double> doubleNumber(ConfigPath path) {
        return new ConfigKeyBuilder<>(path, Double.class);
    }

    public static ConfigKeyBuilder<Boolean> bool(ConfigPath path) {
        return new ConfigKeyBuilder<>(path, Boolean.class);
    }

    public static <T> ConfigKeyBuilder<T> of(ConfigPath path, Class<T> type) {
        return new ConfigKeyBuilder<>(path, type);
    }

    public ConfigPath path() {
        return path;
    }

    public Class<T> type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public boolean hasDefault() {
        return hasDefault;
    }

    public static final class ConfigKeyBuilder<T> {

        private final ConfigPath path;
        private final Class<T> type;

        private ConfigKeyBuilder(ConfigPath path, Class<T> type) {
            this.path = Objects.requireNonNull(path, "path");
            this.type = Objects.requireNonNull(type, "type");
        }

        public ConfigKey<T> required() {
            return new ConfigKey<>(path, type, null, false);
        }

        public ConfigKey<T> defaultValue(T defaultValue) {
            return new ConfigKey<>(path, type, defaultValue, true);
        }

    }

}