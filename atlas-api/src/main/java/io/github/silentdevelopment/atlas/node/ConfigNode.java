package io.github.silentdevelopment.atlas.node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ConfigNode {

    ConfigNodeType type();

    Optional<ConfigNode> child(String key);

    Optional<ConfigNode> element(int index);

    Map<String, ConfigNode> children();

    List<ConfigNode> elements();

    Optional<Object> scalar();

    Optional<String> asString();

    Optional<Integer> asInt();

    Optional<Long> asLong();

    Optional<Double> asDouble();

    Optional<Boolean> asBoolean();

}