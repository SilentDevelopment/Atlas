package io.github.silentdevelopment.atlas.core.node;

import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ListConfigNode(List<ConfigNode> elements) implements ConfigNode {

    public ListConfigNode {
        Objects.requireNonNull(elements, "elements");
        elements = List.copyOf(elements);
    }

    @Override
    public ConfigNodeType type() {
        return ConfigNodeType.LIST;
    }

    @Override
    public Optional<ConfigNode> child(String key) {
        Objects.requireNonNull(key, "key");
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> element(int index) {
        if (index < 0 || index >= elements.size()) {
            return Optional.empty();
        }

        return Optional.of(elements.get(index));
    }

    @Override
    public Map<String, ConfigNode> children() {
        return Map.of();
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