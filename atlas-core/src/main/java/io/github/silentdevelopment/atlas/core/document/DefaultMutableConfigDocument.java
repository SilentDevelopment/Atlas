package io.github.silentdevelopment.atlas.core.document;

import io.github.silentdevelopment.atlas.ConfigKey;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.document.MutableCommentedConfigDocument;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultMutableConfigDocument implements MutableCommentedConfigDocument {

    private final MutableConfigNode root;
    private final Map<ConfigPath, List<String>> comments = new LinkedHashMap<>();
    private List<String> headerComment = List.of();

    public DefaultMutableConfigDocument(MutableConfigNode root) {
        this.root = Objects.requireNonNull(root, "root");
    }

    @Override
    public MutableConfigNode root() {
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
    public MutableConfigNode nodeOrCreate(ConfigPath path) {
        Objects.requireNonNull(path, "path");

        MutableConfigNode current = root;

        for (String part : path.parts()) {
            current = current.childOrCreate(part);
        }

        return current;
    }

    @Override
    public void set(ConfigPath path, Object value) {
        Objects.requireNonNull(path, "path");

        List<String> parts = path.parts();

        if (parts.isEmpty()) {
            root.setScalar(value);
            return;
        }

        MutableConfigNode parent = root;

        for (int i = 0; i < parts.size() - 1; i++) {
            parent = parent.childOrCreate(parts.get(i));
        }

        parent.setChild(parts.getLast(), value);
    }

    @Override
    public boolean remove(ConfigPath path) {
        Objects.requireNonNull(path, "path");

        List<String> parts = path.parts();

        if (parts.isEmpty()) {
            root.clear();
            clearComments();
            return true;
        }

        MutableConfigNode parent = root;

        for (int i = 0; i < parts.size() - 1; i++) {
            Optional<MutableConfigNode> child = parent.mutableChild(parts.get(i));

            if (child.isEmpty()) {
                return false;
            }

            parent = child.get();
        }

        boolean removed = parent.removeChild(parts.getLast());

        if (removed) {
            removeComment(path);
            removeDescendantComments(path);
        }

        return removed;
    }

    @Override
    public void clear() {
        root.clear();
        clearComments();
    }

    @Override
    public Optional<List<String>> headerComment() {
        if (headerComment.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(headerComment);
    }

    @Override
    public List<String> comment(ConfigPath path) {
        Objects.requireNonNull(path, "path");
        return comments.getOrDefault(path, List.of());
    }

    @Override
    public void setHeaderComment(List<String> lines) {
        this.headerComment = normalizeComment(lines);
    }

    @Override
    public void removeHeaderComment() {
        this.headerComment = List.of();
    }

    @Override
    public void setComment(ConfigPath path, List<String> lines) {
        Objects.requireNonNull(path, "path");

        List<String> normalized = normalizeComment(lines);

        if (normalized.isEmpty()) {
            comments.remove(path);
            return;
        }

        comments.put(path, normalized);
    }

    @Override
    public void removeComment(ConfigPath path) {
        Objects.requireNonNull(path, "path");
        comments.remove(path);
    }

    @Override
    public void clearComments() {
        comments.clear();
        headerComment = List.of();
    }

    @Override
    public <T> Optional<T> get(ConfigPath path, Class<T> type) {
        Objects.requireNonNull(path, "path");
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

    private List<String> normalizeComment(List<String> lines) {
        Objects.requireNonNull(lines, "lines");

        List<String> normalized = new ArrayList<>();

        for (String line : lines) {
            if (line == null) {
                continue;
            }

            normalized.add(line);
        }

        return List.copyOf(normalized);
    }

    private void removeDescendantComments(ConfigPath path) {
        comments.keySet().removeIf(commentPath -> isDescendant(path, commentPath));
    }

    private boolean isDescendant(ConfigPath parent, ConfigPath child) {
        if (child.parts().size() <= parent.parts().size()) {
            return false;
        }

        for (int i = 0; i < parent.parts().size(); i++) {
            if (!Objects.equals(parent.parts().get(i), child.parts().get(i))) {
                return false;
            }
        }

        return true;
    }

}