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
import org.yaml.snakeyaml.DumperOptions;
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
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setIndicatorIndent(2);
        options.setWidth(120);
        return new YamlConfigCodec(new Yaml(options));
    }

    @Override
    public String format() {
        return "yaml";
    }

    @Override
    public ConfigDocument decode(InputStream input) throws ConfigDecodeException {
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
        String yamlText = yaml.dump(document.root());
        writer.write(addBlankLinesBetweenTopLevelSections(yamlText));
        writer.flush();
    }

    private static String addBlankLinesBetweenTopLevelSections(String yamlText) {
        String[] lines = yamlText.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        StringBuilder builder = new StringBuilder(yamlText.length() + 32);
        boolean wroteContentLine = false;
        boolean previousLineBlank = false;

        for (String line : lines) {
            if (line.isEmpty()) {
                if (!previousLineBlank) {
                    builder.append('\n');
                }

                previousLineBlank = true;
                continue;
            }

            boolean topLevelKey = isTopLevelKey(line);
            if (topLevelKey && wroteContentLine && !previousLineBlank) {
                builder.append('\n');
            }

            builder.append(line).append('\n');
            wroteContentLine = true;
            previousLineBlank = false;
        }

        return builder.toString();
    }

    private static boolean isTopLevelKey(String line) {
        if (line.isBlank()) {
            return false;
        }

        if (Character.isWhitespace(line.charAt(0))) {
            return false;
        }

        if (line.startsWith("-")) {
            return false;
        }

        int colonIndex = line.indexOf(':');
        return colonIndex > 0;
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