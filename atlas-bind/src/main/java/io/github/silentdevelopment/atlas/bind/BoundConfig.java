package io.github.silentdevelopment.atlas.bind;

import io.github.silentdevelopment.atlas.ConfigLoader;
import io.github.silentdevelopment.atlas.bind.exception.ConfigBindException;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;

import java.io.IOException;
import java.util.Objects;

public final class BoundConfig<T> {

    private final ConfigLoader loader;
    private final ConfigBinder binder;
    private final Class<T> type;
    private MutableConfigDocument document;
    private T value;

    private BoundConfig(ConfigLoader loader, ConfigBinder binder, Class<T> type, MutableConfigDocument document, T value) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.binder = Objects.requireNonNull(binder, "binder");
        this.type = Objects.requireNonNull(type, "type");
        this.document = Objects.requireNonNull(document, "document");
        this.value = Objects.requireNonNull(value, "value");
    }

    public static <T> BoundConfig<T> load(ConfigLoader loader, Class<T> type) throws IOException {
        Objects.requireNonNull(loader, "loader");
        Objects.requireNonNull(type, "type");

        ConfigDocument loaded = loader.load();

        if (!(loaded instanceof MutableConfigDocument document)) {
            throw new ConfigBindException("Loaded config document is not mutable.");
        }

        ConfigBinder binder = ConfigBinder.create();
        T value = binder.load(type, document);
        return new BoundConfig<>(loader, binder, type, document, value);
    }

    public T value() {
        return value;
    }

    public MutableConfigDocument document() {
        return document;
    }

    public void save() throws IOException {
        binder.save(value, document);
        loader.save(document);
    }

    public void reload() throws IOException {
        ConfigDocument loaded = loader.load();

        if (!(loaded instanceof MutableConfigDocument mutable)) {
            throw new ConfigBindException("Loaded config document is not mutable.");
        }

        this.document = mutable;
        this.value = binder.load(type, mutable);
    }

}