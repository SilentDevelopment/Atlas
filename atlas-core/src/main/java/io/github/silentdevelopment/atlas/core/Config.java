package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.ConfigKey;
import io.github.silentdevelopment.atlas.ConfigLoader;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.CommentedConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableCommentedConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigLoadException;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public final class Config implements MutableConfigDocument {

    private final ConfigLoader loader;
    private MutableConfigDocument document;

    Config(ConfigLoader loader, MutableConfigDocument document) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.document = Objects.requireNonNull(document, "document");
    }

    public static ConfigRequest create() {
        return new ConfigRequest();
    }

    public ConfigLoader loader() {
        return loader;
    }

    public ConfigDocument document() {
        return document;
    }

    public MutableConfigDocument mutableDocument() {
        return document;
    }

    public Optional<CommentedConfigDocument> commentedDocument() {
        if (!(document instanceof CommentedConfigDocument commented)) {
            return Optional.empty();
        }

        return Optional.of(commented);
    }

    public Optional<MutableCommentedConfigDocument> mutableCommentedDocument() {
        if (!(document instanceof MutableCommentedConfigDocument commented)) {
            return Optional.empty();
        }

        return Optional.of(commented);
    }

    public void save() throws IOException {
        loader.save(document);
    }

    public void reload() throws IOException {
        ConfigDocument loaded = loader.load();

        if (!(loaded instanceof MutableConfigDocument mutable)) {
            throw new ConfigLoadException("Loaded config document is not mutable.");
        }

        this.document = mutable;
    }

    @Override
    public MutableConfigNode root() {
        return document.root();
    }

    @Override
    public Optional<ConfigNode> node(ConfigPath path) {
        return document.node(path);
    }

    @Override
    public MutableConfigNode nodeOrCreate(ConfigPath path) {
        return document.nodeOrCreate(path);
    }

    @Override
    public void set(ConfigPath path, Object value) {
        document.set(path, value);
    }

    @Override
    public boolean remove(ConfigPath path) {
        return document.remove(path);
    }

    @Override
    public void clear() {
        document.clear();
    }

    @Override
    public <T> Optional<T> get(ConfigPath path, Class<T> type) {
        return document.get(path, type);
    }

    @Override
    public <T> T get(ConfigKey<T> key) {
        return document.get(key);
    }

    @Override
    public Optional<String> getString(ConfigPath path) {
        return document.getString(path);
    }

    @Override
    public String getString(ConfigPath path, String fallback) {
        return document.getString(path, fallback);
    }

    @Override
    public Optional<Integer> getInt(ConfigPath path) {
        return document.getInt(path);
    }

    @Override
    public int getInt(ConfigPath path, int fallback) {
        return document.getInt(path, fallback);
    }

    @Override
    public Optional<Boolean> getBoolean(ConfigPath path) {
        return document.getBoolean(path);
    }

    @Override
    public boolean getBoolean(ConfigPath path, boolean fallback) {
        return document.getBoolean(path, fallback);
    }

}