package io.github.silentdevelopment.atlas.core.node;

import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ConfigNodes {

    private ConfigNodes() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static MutableConfigNode object() {
        return new DefaultConfigNode(ConfigNodeType.OBJECT, Map.of(), null, null);
    }

    public static MutableConfigNode object(Map<String, ? extends ConfigNode> children) {
        Objects.requireNonNull(children, "children");
        return new DefaultConfigNode(ConfigNodeType.OBJECT, children, null, null);
    }

    public static MutableConfigNode list() {
        return new DefaultConfigNode(ConfigNodeType.LIST, null, List.of(), null);
    }

    public static MutableConfigNode list(List<? extends ConfigNode> elements) {
        Objects.requireNonNull(elements, "elements");
        return new DefaultConfigNode(ConfigNodeType.LIST, null, elements, null);
    }

    public static MutableConfigNode scalar(Object value) {
        if (value == null) {
            return nullNode();
        }

        return new DefaultConfigNode(ConfigNodeType.SCALAR, null, null, value);
    }

    public static MutableConfigNode nullNode() {
        return new DefaultConfigNode(ConfigNodeType.NULL, null, null, null);
    }

}