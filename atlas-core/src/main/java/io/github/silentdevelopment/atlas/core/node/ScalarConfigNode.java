package io.github.silentdevelopment.atlas.core.node;

import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public record ScalarConfigNode(Object value) implements ConfigNode {

    public ScalarConfigNode {
        Objects.requireNonNull(value, "value");
    }

    @Override
    public ConfigNodeType type() {
        return ConfigNodeType.SCALAR;
    }

    @Override
    public Optional<ConfigNode> child(String key) {
        Objects.requireNonNull(key, "key");
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
        return Optional.of(value);
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(String.valueOf(value));
    }

    @Override
    public Optional<Integer> asInt() {
        if (value instanceof Number number) {
            return Optional.of(number.intValue());
        }

        try {
            return Optional.of(Integer.parseInt(String.valueOf(value)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> asLong() {
        if (value instanceof Number number) {
            return Optional.of(number.longValue());
        }

        try {
            return Optional.of(Long.parseLong(String.valueOf(value)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> asDouble() {
        if (value instanceof Number number) {
            return Optional.of(number.doubleValue());
        }

        try {
            return Optional.of(Double.parseDouble(String.valueOf(value)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> asBoolean() {
        if (value instanceof Boolean bool) {
            return Optional.of(bool);
        }

        String text = String.valueOf(value);

        if ("true".equalsIgnoreCase(text)) {
            return Optional.of(true);
        }

        if ("false".equalsIgnoreCase(text)) {
            return Optional.of(false);
        }

        return Optional.empty();
    }

}