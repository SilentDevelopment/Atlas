package io.github.silentdevelopment.atlas.core.node;

import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class NullConfigNode implements ConfigNode {

    @Override
    public ConfigNodeType type() {
        return ConfigNodeType.NULL;
    }

    @Override
    public Optional<ConfigNode> child(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigNode> element(int index) {
        return Optional.empty();
    }

    @Override
    public Map<String, ConfigNode> children() {
        return Map.of();
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