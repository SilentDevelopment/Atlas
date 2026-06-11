package io.github.silentdevelopment.atlas.core.node;

import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ObjectConfigNode(Map<String, ConfigNode> children) implements ConfigNode {

    public ObjectConfigNode {
        Objects.requireNonNull(children, "children");
        children = Collections.unmodifiableMap(new LinkedHashMap<>(children));
    }

    @Override
    public ConfigNodeType type() {
        return ConfigNodeType.OBJECT;
    }

    @Override
    public Optional<ConfigNode> child(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(children.get(key));
    }

    @Override
    public Optional<ConfigNode> element(int index) {
        return Optional.empty();
    }

    @Override
    public List<ConfigNode> elements() {
        return List.of();
    }

    @Override
    public Optional<Object> scalar() {
        return Optional.empty();
    }

    @Override
    public Optional<String> asString() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> asInt() {
        return Optional.empty();
    }

    @Override
    public Optional<Long> asLong() {
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> asBoolean() {
        return Optional.empty();
    }

}