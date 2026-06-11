package io.github.silentdevelopment.atlas.core.node;

import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultConfigNode implements MutableConfigNode {

    private ConfigNodeType type;
    private Map<String, MutableConfigNode> children;
    private List<MutableConfigNode> elements;
    private Object scalar;

    public DefaultConfigNode() {
        setObject(Map.of());
    }

    public DefaultConfigNode(ConfigNodeType type, Map<String, ? extends ConfigNode> children, List<? extends ConfigNode> elements, Object scalar) {
        Objects.requireNonNull(type, "type");

        if (type == ConfigNodeType.OBJECT) {
            setObject(children == null ? Map.of() : children);
            return;
        }

        if (type == ConfigNodeType.LIST) {
            setList(elements == null ? List.of() : elements);
            return;
        }

        if (type == ConfigNodeType.SCALAR) {
            setScalar(scalar);
            return;
        }

        setNull();
    }

    @Override
    public ConfigNodeType type() {
        return type;
    }

    @Override
    public Optional<ConfigNode> child(String key) {
        return mutableChild(key).map(node -> node);
    }

    @Override
    public Optional<MutableConfigNode> mutableChild(String key) {
        Objects.requireNonNull(key, "key");

        if (type != ConfigNodeType.OBJECT) {
            return Optional.empty();
        }

        return Optional.ofNullable(children.get(key));
    }

    @Override
    public Optional<ConfigNode> element(int index) {
        return mutableElement(index).map(node -> node);
    }

    @Override
    public Optional<MutableConfigNode> mutableElement(int index) {
        if (type != ConfigNodeType.LIST) {
            return Optional.empty();
        }

        if (index < 0 || index >= elements.size()) {
            return Optional.empty();
        }

        return Optional.of(elements.get(index));
    }

    @Override
    public Map<String, ConfigNode> children() {
        if (type != ConfigNodeType.OBJECT) {
            return Map.of();
        }

        Map<String, ConfigNode> copy = new LinkedHashMap<>();

        for (Map.Entry<String, MutableConfigNode> entry : children.entrySet()) {
            copy.put(entry.getKey(), entry.getValue());
        }

        return Collections.unmodifiableMap(copy);
    }

    @Override
    public Map<String, MutableConfigNode> mutableChildren() {
        if (type != ConfigNodeType.OBJECT) {
            return Map.of();
        }

        return Collections.unmodifiableMap(children);
    }

    @Override
    public List<ConfigNode> elements() {
        if (type != ConfigNodeType.LIST) {
            return List.of();
        }

        List<ConfigNode> copy = new ArrayList<>();

        for (MutableConfigNode element : elements) {
            copy.add(element);
        }

        return Collections.unmodifiableList(copy);
    }

    @Override
    public List<MutableConfigNode> mutableElements() {
        if (type != ConfigNodeType.LIST) {
            return List.of();
        }

        return Collections.unmodifiableList(elements);
    }

    @Override
    public Optional<Object> scalar() {
        if (type != ConfigNodeType.SCALAR) {
            return Optional.empty();
        }

        return Optional.ofNullable(scalar);
    }

    @Override
    public Optional<String> asString() {
        if (type != ConfigNodeType.SCALAR) {
            return Optional.empty();
        }

        if (scalar == null) {
            return Optional.empty();
        }

        return Optional.of(String.valueOf(scalar));
    }

    @Override
    public Optional<Integer> asInt() {
        if (type != ConfigNodeType.SCALAR) {
            return Optional.empty();
        }

        if (scalar instanceof Number number) {
            return Optional.of(number.intValue());
        }

        if (scalar == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(String.valueOf(scalar)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Long> asLong() {
        if (type != ConfigNodeType.SCALAR) {
            return Optional.empty();
        }

        if (scalar instanceof Number number) {
            return Optional.of(number.longValue());
        }

        if (scalar == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Long.parseLong(String.valueOf(scalar)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> asDouble() {
        if (type != ConfigNodeType.SCALAR) {
            return Optional.empty();
        }

        if (scalar instanceof Number number) {
            return Optional.of(number.doubleValue());
        }

        if (scalar == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(Double.parseDouble(String.valueOf(scalar)));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Boolean> asBoolean() {
        if (type != ConfigNodeType.SCALAR) {
            return Optional.empty();
        }

        if (scalar instanceof Boolean bool) {
            return Optional.of(bool);
        }

        if (scalar == null) {
            return Optional.empty();
        }

        String value = String.valueOf(scalar);

        if ("true".equalsIgnoreCase(value)) {
            return Optional.of(true);
        }

        if ("false".equalsIgnoreCase(value)) {
            return Optional.of(false);
        }

        return Optional.empty();
    }

    @Override
    public MutableConfigNode childOrCreate(String key) {
        Objects.requireNonNull(key, "key");

        if (key.isBlank()) {
            throw new IllegalArgumentException("key cannot be blank.");
        }

        ensureObject();

        MutableConfigNode existing = children.get(key);

        if (existing != null) {
            return existing;
        }

        MutableConfigNode created = ConfigNodes.object();
        children.put(key, created);
        return created;
    }

    @Override
    public MutableConfigNode elementOrCreate(int index) {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be negative.");
        }

        ensureList();

        while (elements.size() <= index) {
            elements.add(ConfigNodes.nullNode());
        }

        return elements.get(index);
    }

    @Override
    public void setChild(String key, Object value) {
        Objects.requireNonNull(key, "key");

        if (key.isBlank()) {
            throw new IllegalArgumentException("key cannot be blank.");
        }

        ensureObject();
        children.put(key, toMutableNode(value));
    }

    @Override
    public void setElement(int index, Object value) {
        if (index < 0) {
            throw new IllegalArgumentException("index cannot be negative.");
        }

        ensureList();

        while (elements.size() <= index) {
            elements.add(ConfigNodes.nullNode());
        }

        elements.set(index, toMutableNode(value));
    }

    @Override
    public boolean removeChild(String key) {
        Objects.requireNonNull(key, "key");

        if (type != ConfigNodeType.OBJECT) {
            return false;
        }

        return children.remove(key) != null;
    }

    @Override
    public boolean removeElement(int index) {
        if (type != ConfigNodeType.LIST) {
            return false;
        }

        if (index < 0 || index >= elements.size()) {
            return false;
        }

        elements.remove(index);
        return true;
    }

    @Override
    public void setScalar(Object value) {
        if (value == null) {
            setNull();
            return;
        }

        this.type = ConfigNodeType.SCALAR;
        this.children = null;
        this.elements = null;
        this.scalar = value;
    }

    @Override
    public void setNull() {
        this.type = ConfigNodeType.NULL;
        this.children = null;
        this.elements = null;
        this.scalar = null;
    }

    @Override
    public void clear() {
        if (type == ConfigNodeType.OBJECT) {
            children.clear();
            return;
        }

        if (type == ConfigNodeType.LIST) {
            elements.clear();
            return;
        }

        setNull();
    }

    private void ensureObject() {
        if (type == ConfigNodeType.OBJECT) {
            return;
        }

        setObject(Map.of());
    }

    private void ensureList() {
        if (type == ConfigNodeType.LIST) {
            return;
        }

        setList(List.of());
    }

    private void setObject(Map<String, ? extends ConfigNode> children) {
        Objects.requireNonNull(children, "children");

        this.type = ConfigNodeType.OBJECT;
        this.children = new LinkedHashMap<>();
        this.elements = null;
        this.scalar = null;

        for (Map.Entry<String, ? extends ConfigNode> entry : children.entrySet()) {
            this.children.put(entry.getKey(), toMutableNode(entry.getValue()));
        }
    }

    private void setList(List<? extends ConfigNode> elements) {
        Objects.requireNonNull(elements, "elements");

        this.type = ConfigNodeType.LIST;
        this.children = null;
        this.elements = new ArrayList<>();
        this.scalar = null;

        for (ConfigNode element : elements) {
            this.elements.add(toMutableNode(element));
        }
    }

    private MutableConfigNode toMutableNode(Object value) {
        if (value instanceof MutableConfigNode mutableNode) {
            return mutableNode;
        }

        if (value instanceof ConfigNode node) {
            return copy(node);
        }

        if (value instanceof Map<?, ?> map) {
            MutableConfigNode object = ConfigNodes.object();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                object.setChild(String.valueOf(entry.getKey()), entry.getValue());
            }

            return object;
        }

        if (value instanceof Iterable<?> iterable) {
            MutableConfigNode list = ConfigNodes.list();
            int index = 0;

            for (Object element : iterable) {
                list.setElement(index, element);
                index++;
            }

            return list;
        }

        return ConfigNodes.scalar(value);
    }

    private MutableConfigNode copy(ConfigNode node) {
        Objects.requireNonNull(node, "node");

        if (node.type() == ConfigNodeType.OBJECT) {
            MutableConfigNode copy = ConfigNodes.object();

            for (Map.Entry<String, ConfigNode> entry : node.children().entrySet()) {
                copy.setChild(entry.getKey(), copy(entry.getValue()));
            }

            return copy;
        }

        if (node.type() == ConfigNodeType.LIST) {
            MutableConfigNode copy = ConfigNodes.list();

            for (int i = 0; i < node.elements().size(); i++) {
                copy.setElement(i, copy(node.elements().get(i)));
            }

            return copy;
        }

        if (node.type() == ConfigNodeType.SCALAR) {
            return ConfigNodes.scalar(node.scalar().orElse(null));
        }

        return ConfigNodes.nullNode();
    }

}