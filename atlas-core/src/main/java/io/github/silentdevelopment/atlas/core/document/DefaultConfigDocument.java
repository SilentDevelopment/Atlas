package io.github.silentdevelopment.atlas.core.document;

import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.ConfigKey;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.ConfigPath;

import java.util.Objects;
import java.util.Optional;

public final class DefaultConfigDocument implements ConfigDocument {

    private final ConfigNode root;

    public DefaultConfigDocument(ConfigNode root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    @Override
    public ConfigNode root() {
        return root;
    }

    @Override
    public Optional<ConfigNode> node(ConfigPath path) {
        Objects.requireNonNull(path, "path");

        ConfigNode current = root;

        for (String part : path.parts()) {
            Optional<ConfigNode> child = current.child(part);

            if (child.isEmpty()) {
                return Optional.empty();
            }

            current = child.get();
        }

        return Optional.of(current);
    }

    @Override
    public <T> Optional<T> get(ConfigPath path, Class<T> type) {
        Objects.requireNonNull(type, "type");
        return node(path).flatMap(node -> convert(node, type));
    }

    @Override
    public <T> T get(ConfigKey<T> key) {
        Objects.requireNonNull(key, "key");

        Optional<T> value = get(key.path(), key.type());

        if (value.isPresent()) {
            return value.get();
        }

        if (key.hasDefault()) {
            return key.defaultValue();
        }

        throw new IllegalStateException("Missing required config value at " + key.path() + ".");
    }

    @Override
    public Optional<String> getString(ConfigPath path) {
        return node(path).flatMap(ConfigNode::asString);
    }

    @Override
    public String getString(ConfigPath path, String fallback) {
        return getString(path).orElse(fallback);
    }

    @Override
    public Optional<Integer> getInt(ConfigPath path) {
        return node(path).flatMap(ConfigNode::asInt);
    }

    @Override
    public int getInt(ConfigPath path, int fallback) {
        return getInt(path).orElse(fallback);
    }

    @Override
    public Optional<Boolean> getBoolean(ConfigPath path) {
        return node(path).flatMap(ConfigNode::asBoolean);
    }

    @Override
    public boolean getBoolean(ConfigPath path, boolean fallback) {
        return getBoolean(path).orElse(fallback);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> convert(ConfigNode node, Class<T> type) {
        if (type == String.class) {
            return node.asString().map(value -> (T) value);
        }

        if (type == Integer.class || type == int.class) {
            return node.asInt().map(value -> (T) value);
        }

        if (type == Long.class || type == long.class) {
            return node.asLong().map(value -> (T) value);
        }

        if (type == Double.class || type == double.class) {
            return node.asDouble().map(value -> (T) value);
        }

        if (type == Boolean.class || type == boolean.class) {
            return node.asBoolean().map(value -> (T) value);
        }

        return node.scalar().filter(type::isInstance).map(type::cast);
    }

}