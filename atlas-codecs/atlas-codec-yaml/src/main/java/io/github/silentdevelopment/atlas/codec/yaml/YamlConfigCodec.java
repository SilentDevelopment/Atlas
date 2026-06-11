package io.github.silentdevelopment.atlas.codec.yaml;

import io.github.silentdevelopment.atlas.ConfigCodec;
import io.github.silentdevelopment.atlas.core.document.ConfigDocuments;
import io.github.silentdevelopment.atlas.core.node.ConfigNodes;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigDecodeException;
import io.github.silentdevelopment.atlas.exception.ConfigEncodeException;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class YamlConfigCodec implements ConfigCodec {

    private final Yaml yaml;

    private YamlConfigCodec(Yaml yaml) {
        this.yaml = Objects.requireNonNull(yaml, "yaml");
    }

    public static YamlConfigCodec create() {
        return new YamlConfigCodec(new Yaml());
    }

    @Override
    public String format() {
        return "yaml";
    }

    @Override
    public ConfigDocument decode(InputStream input) throws IOException, ConfigDecodeException {
        Objects.requireNonNull(input, "input");

        try {
            Object value = yaml.load(input);
            MutableConfigNode root = toNode(value);

            if (root.type() == ConfigNodeType.NULL) {
                root = ConfigNodes.object();
            }

            return ConfigDocuments.mutable(root);
        } catch (Exception exception) {
            throw new ConfigDecodeException("Failed to decode YAML config.", exception);
        }
    }

    @Override
    public void encode(ConfigDocument document, OutputStream output) throws IOException, ConfigEncodeException {
        Objects.requireNonNull(document, "document");
        Objects.requireNonNull(output, "output");

        OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);
        yaml.dump(toYamlValue(document.root()), writer);
        writer.flush();
    }

    private MutableConfigNode toNode(Object value) {
        if (value == null) {
            return ConfigNodes.nullNode();
        }

        if (value instanceof Map<?, ?> map) {
            MutableConfigNode node = ConfigNodes.object();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                node.setChild(String.valueOf(entry.getKey()), toNode(entry.getValue()));
            }

            return node;
        }

        if (value instanceof Iterable<?> iterable) {
            MutableConfigNode node = ConfigNodes.list();
            int index = 0;

            for (Object element : iterable) {
                node.setElement(index, toNode(element));
                index++;
            }

            return node;
        }

        return ConfigNodes.scalar(value);
    }

    private Object toYamlValue(ConfigNode node) {
        if (node.type() == ConfigNodeType.OBJECT) {
            Map<String, Object> map = new LinkedHashMap<>();

            for (Map.Entry<String, ConfigNode> entry : node.children().entrySet()) {
                map.put(entry.getKey(), toYamlValue(entry.getValue()));
            }

            return map;
        }

        if (node.type() == ConfigNodeType.LIST) {
            ArrayList<Object> list = new ArrayList<>();

            for (ConfigNode element : node.elements()) {
                list.add(toYamlValue(element));
            }

            return list;
        }

        if (node.type() == ConfigNodeType.SCALAR) {
            return node.scalar().orElse(null);
        }

        return null;
    }

}