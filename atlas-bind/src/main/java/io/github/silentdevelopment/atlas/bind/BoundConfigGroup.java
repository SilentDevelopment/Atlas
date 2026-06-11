package io.github.silentdevelopment.atlas.bind;

import io.github.silentdevelopment.atlas.ConfigLoader;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.bind.exception.ConfigBindException;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class BoundConfigGroup {

    private final ConfigLoader loader;
    private final ConfigBinder binder;
    private final Class<?>[] types;
    private final Map<Class<?>, Object> values;
    private MutableConfigDocument document;

    private BoundConfigGroup(ConfigLoader loader, ConfigBinder binder, Class<?>[] types, MutableConfigDocument document, Map<Class<?>, Object> values) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.binder = Objects.requireNonNull(binder, "binder");
        this.types = Objects.requireNonNull(types, "types").clone();
        this.document = Objects.requireNonNull(document, "document");
        this.values = new LinkedHashMap<>(Objects.requireNonNull(values, "values"));
    }

    public static BoundConfigGroup load(ConfigLoader loader, Class<?>... types) throws IOException {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(types, "types");

        ConfigBinder binder = ConfigBinder.create();
        validateTypes(types);
        validatePaths(binder, types);

        ConfigDocument loaded = loader.load();

        if (!(loaded instanceof MutableConfigDocument document)) {
            throw new ConfigBindException("Loaded config document is not mutable.");
        }

        Map<Class<?>, Object> values = new LinkedHashMap<>();

        for (Class<?> type : types) {
            Object value = binder.load(type, document);
            values.put(type, value);
        }

        return new BoundConfigGroup(loader, binder, types, document, values);
    }

    public <T> T value(Class<T> type) {
        Objects.requireNonNull(type, "type");

        Object value = values.get(type);

        if (value == null) {
            throw new ConfigBindException("No bound config value exists for " + type.getName() + ".");
        }

        return type.cast(value);
    }

    public Map<Class<?>, Object> values() {
        return Collections.unmodifiableMap(values);
    }

    public MutableConfigDocument document() {
        return document;
    }

    public void save() throws IOException {
        for (Object value : values.values()) {
            binder.save(value, document);
        }

        loader.save(document);
    }

    public void reload() throws IOException {
        ConfigDocument loaded = loader.load();

        if (!(loaded instanceof MutableConfigDocument mutable)) {
            throw new ConfigBindException("Loaded config document is not mutable.");
        }

        Map<Class<?>, Object> reloaded = new LinkedHashMap<>();

        for (Class<?> type : types) {
            Object value = binder.load(type, mutable);
            reloaded.put(type, value);
        }

        this.document = mutable;
        this.values.clear();
        this.values.putAll(reloaded);
    }

    private static void validateTypes(Class<?>[] types) {
        Map<Class<?>, Boolean> seen = new LinkedHashMap<>();

        for (Class<?> type : types) {
            if (type == null) {
                throw new NullPointerException("types cannot contain null.");
            }

            if (seen.containsKey(type)) {
                throw new ConfigBindException("Config type " + type.getName() + " is bound more than once.");
            }

            seen.put(type, true);
        }
    }

    private static void validatePaths(ConfigBinder binder, Class<?>[] types) {
        Map<ConfigPath, Class<?>> owners = new LinkedHashMap<>();

        for (Class<?> type : types) {
            for (ConfigPath path : binder.paths(type)) {
                Class<?> previous = owners.putIfAbsent(path, type);

                if (previous == null) {
                    continue;
                }

                throw new ConfigBindException("Duplicate bound config path " + path + " is used by both " + previous.getName() + " and " + type.getName() + ".");
            }
        }
    }

}