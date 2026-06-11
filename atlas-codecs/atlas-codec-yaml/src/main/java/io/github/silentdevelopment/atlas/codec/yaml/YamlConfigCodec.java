package io.github.silentdevelopment.atlas.codec.yaml;

import io.github.silentdevelopment.atlas.ConfigCodec;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.core.document.ConfigDocuments;
import io.github.silentdevelopment.atlas.core.node.ConfigNodes;
import io.github.silentdevelopment.atlas.document.CommentedConfigDocument;
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
import java.util.List;
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
        options.setIndicatorIndent(0);
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

        try {
            OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8);

            if (document instanceof CommentedConfigDocument commented) {
                writer.write(renderCommented(commented));
                writer.flush();
                return;
            }

            String yamlText = yaml.dump(toYamlValue(document.root()));
            writer.write(addBlankLinesBetweenTopLevelSections(yamlText));
            writer.flush();
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ConfigEncodeException("Failed to encode YAML config.", exception);
        }
    }

    private static String renderCommented(CommentedConfigDocument document) {
        StringBuilder builder = new StringBuilder();

        document.headerComment().ifPresent(lines -> {
            appendComment(builder, 0, lines);
            builder.append('\n');
        });

        renderNode(builder, document, ConfigPath.of(), document.root(), 0, true);

        return builder.toString();
    }

    private static void renderNode(StringBuilder builder, CommentedConfigDocument document, ConfigPath path, ConfigNode node, int indent, boolean topLevel) {
        if (node.type() == ConfigNodeType.OBJECT) {
            boolean first = true;

            for (Map.Entry<String, ConfigNode> entry : node.children().entrySet()) {
                ConfigPath childPath = path.append(entry.getKey());

                if (topLevel && !first) {
                    builder.append('\n');
                }

                appendComment(builder, indent, document.comment(childPath));
                appendIndent(builder, indent);
                builder.append(entry.getKey()).append(':');

                ConfigNode child = entry.getValue();
                if (child.type() == ConfigNodeType.OBJECT || child.type() == ConfigNodeType.LIST) {
                    builder.append('\n');
                    renderNode(builder, document, childPath, child, indent + 2, false);
                } else {
                    builder.append(' ').append(formatScalar(child)).append('\n');
                }

                first = false;
            }

            return;
        }

        if (node.type() == ConfigNodeType.LIST) {
            for (ConfigNode element : node.elements()) {
                appendIndent(builder, indent);
                builder.append("-");

                if (element.type() == ConfigNodeType.OBJECT || element.type() == ConfigNodeType.LIST) {
                    builder.append('\n');
                    renderNode(builder, document, path, element, indent + 2, false);
                } else {
                    builder.append(' ').append(formatScalar(element)).append('\n');
                }
            }

            return;
        }

        appendIndent(builder, indent);
        builder.append(formatScalar(node)).append('\n');
    }

    private static void appendComment(StringBuilder builder, int indent, List<String> lines) {
        for (String line : lines) {
            appendIndent(builder, indent);

            if (line.isBlank()) {
                builder.append('#').append('\n');
                continue;
            }

            builder.append("# ").append(line).append('\n');
        }
    }

    private static void appendIndent(StringBuilder builder, int indent) {
        builder.append(" ".repeat(indent));
    }

    private static String formatScalar(ConfigNode node) {
        if (node.type() == ConfigNodeType.NULL) {
            return "null";
        }

        Object value = node.scalar().orElse(null);
        if (value == null) {
            return "null";
        }

        if (value instanceof Boolean || value instanceof Number) {
            return String.valueOf(value);
        }

        return formatString(String.valueOf(value));
    }

    private static String formatString(String value) {
        if (value.isEmpty()) {
            return "\"\"";
        }

        if (plainString(value)) {
            return value;
        }

        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static boolean plainString(String value) {
        if (value.isBlank()) {
            return false;
        }

        if (value.startsWith(" ") || value.endsWith(" ")) {
            return false;
        }

        if (value.equalsIgnoreCase("null") || value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return false;
        }

        return value.matches("[A-Za-z0-9_./:-]+");
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

    private static Object toYamlValue(ConfigNode node) {
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