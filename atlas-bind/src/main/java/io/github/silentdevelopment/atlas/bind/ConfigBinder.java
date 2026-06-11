package io.github.silentdevelopment.atlas.bind;

import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.bind.annotation.Accept;
import io.github.silentdevelopment.atlas.bind.annotation.Allowed;
import io.github.silentdevelopment.atlas.bind.annotation.Comment;
import io.github.silentdevelopment.atlas.bind.annotation.Ignore;
import io.github.silentdevelopment.atlas.bind.annotation.Key;
import io.github.silentdevelopment.atlas.bind.annotation.Prefix;
import io.github.silentdevelopment.atlas.bind.annotation.Range;
import io.github.silentdevelopment.atlas.bind.annotation.Required;
import io.github.silentdevelopment.atlas.bind.exception.ConfigBindException;
import io.github.silentdevelopment.atlas.document.MutableCommentedConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class ConfigBinder {

    private ConfigBinder() {
    }

    public static ConfigBinder create() {
        return new ConfigBinder();
    }

    public <T> T load(Class<T> type, MutableConfigDocument document) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(document, "document");

        T instance = newInstance(type);
        load(instance, document);
        return instance;
    }

    public void load(Object instance, MutableConfigDocument document) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(document, "document");

        writeHeaderComment(instance.getClass(), document);

        for (Field field : fields(instance.getClass())) {
            FieldBinding binding = FieldBinding.create(instance.getClass(), field);
            Optional<ConfigNode> node = document.node(binding.path());

            writeComment(binding, document);

            if (node.isPresent()) {
                Object value = binding.read(node.get());
                binding.validate(value);
                binding.set(instance, value);
                continue;
            }

            if (binding.required()) {
                throw new ConfigBindException("Missing required config value at " + binding.path() + ".");
            }

            Object value = binding.get(instance);
            binding.validate(value);
            document.set(binding.path(), binding.write(value));
        }
    }

    public void save(Object instance, MutableConfigDocument document) {
        Objects.requireNonNull(instance, "instance");
        Objects.requireNonNull(document, "document");

        writeHeaderComment(instance.getClass(), document);

        for (Field field : fields(instance.getClass())) {
            FieldBinding binding = FieldBinding.create(instance.getClass(), field);
            Object value = binding.get(instance);
            binding.validate(value);
            document.set(binding.path(), binding.write(value));
            writeComment(binding, document);
        }
    }

    private <T> T newInstance(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new ConfigBindException("Cannot create config instance for " + type.getName() + ". A no-args constructor is required.", exception);
        }
    }

    private List<Field> fields(Class<?> type) {
        List<Class<?>> hierarchy = new ArrayList<>();
        Class<?> current = type;

        while (current != null && current != Object.class) {
            hierarchy.add(current);
            current = current.getSuperclass();
        }

        Collections.reverse(hierarchy);

        List<Field> fields = new ArrayList<>();

        for (Class<?> currentType : hierarchy) {
            for (Field field : currentType.getDeclaredFields()) {
                if (!bindable(field)) {
                    continue;
                }

                fields.add(field);
            }
        }

        return List.copyOf(fields);
    }

    private boolean bindable(Field field) {
        if (field.isSynthetic()) {
            return false;
        }

        if (field.isAnnotationPresent(Ignore.class)) {
            return false;
        }

        int modifiers = field.getModifiers();

        if (Modifier.isStatic(modifiers)) {
            return false;
        }

        if (Modifier.isTransient(modifiers)) {
            return false;
        }

        if (Modifier.isFinal(modifiers)) {
            throw new ConfigBindException("Config field " + field.getDeclaringClass().getName() + "#" + field.getName() + " cannot be final.");
        }

        return true;
    }

    private void writeHeaderComment(Class<?> type, MutableConfigDocument document) {
        if (!(document instanceof MutableCommentedConfigDocument commented)) {
            return;
        }

        Comment comment = type.getAnnotation(Comment.class);

        if (comment == null) {
            return;
        }

        List<String> lines = cleanLines(comment.value());

        if (lines.isEmpty()) {
            return;
        }

        Prefix prefix = type.getAnnotation(Prefix.class);

        if (prefix == null || prefix.value().trim().isBlank()) {
            commented.setHeaderComment(lines);
            return;
        }

        commented.setComment(ConfigPath.parse(prefix.value().trim()), lines);
    }

    private void writeComment(FieldBinding binding, MutableConfigDocument document) {
        if (!(document instanceof MutableCommentedConfigDocument commented)) {
            return;
        }

        List<String> lines = binding.comment();

        if (lines.isEmpty()) {
            return;
        }

        commented.setComment(binding.path(), lines);
    }

    private static List<String> cleanLines(String[] lines) {
        if (lines == null || lines.length == 0) {
            return List.of();
        }

        List<String> cleaned = new ArrayList<>();

        for (String line : lines) {
            if (line == null) {
                continue;
            }

            cleaned.add(line);
        }

        return List.copyOf(cleaned);
    }

    public List<ConfigPath> paths(Class<?> type) {
        Objects.requireNonNull(type, "type");

        List<ConfigPath> paths = new ArrayList<>();

        for (Field field : fields(type)) {
            paths.add(FieldBinding.create(type, field).path());
        }

        return List.copyOf(paths);
    }

    private static final class FieldBinding {

        private final Field field;
        private final ConfigPath path;
        private final Set<String> accepted;
        private final Range range;
        private final boolean required;
        private final List<String> comment;

        private FieldBinding(Field field, ConfigPath path, Set<String> accepted, Range range, boolean required, List<String> comment) {
            this.field = Objects.requireNonNull(field, "field");
            this.path = Objects.requireNonNull(path, "path");
            this.accepted = Collections.unmodifiableSet(new LinkedHashSet<>(accepted));
            this.range = range;
            this.required = required;
            this.comment = List.copyOf(comment);
            this.field.setAccessible(true);
        }

        private static FieldBinding create(Class<?> owner, Field field) {
            Objects.requireNonNull(owner, "owner");
            Objects.requireNonNull(field, "field");

            return new FieldBinding(field, path(owner, field), accepted(field), field.getAnnotation(Range.class), field.isAnnotationPresent(Required.class), comment(field));
        }

        private ConfigPath path() {
            return path;
        }

        private boolean required() {
            return required;
        }

        private List<String> comment() {
            return comment;
        }

        private Object read(ConfigNode node) {
            Objects.requireNonNull(node, "node");

            Class<?> type = field.getType();

            if (type == String.class) {
                return node.asString().orElseThrow(() -> conversionError("string"));
            }

            if (type == int.class || type == Integer.class) {
                return node.asInt().orElseThrow(() -> conversionError("integer"));
            }

            if (type == long.class || type == Long.class) {
                return node.asLong().orElseThrow(() -> conversionError("long"));
            }

            if (type == double.class || type == Double.class) {
                return node.asDouble().orElseThrow(() -> conversionError("double"));
            }

            if (type == boolean.class || type == Boolean.class) {
                return node.asBoolean().orElseThrow(() -> conversionError("boolean"));
            }

            if (type.isEnum()) {
                return readEnum(node, type.asSubclass(Enum.class));
            }

            if (List.class.isAssignableFrom(type)) {
                return readList(node);
            }

            throw new ConfigBindException("Unsupported config field type " + type.getName() + " at " + path + ".");
        }

        private Object write(Object value) {
            if (value == null) {
                return null;
            }

            if (value instanceof Enum<?> enumValue) {
                return enumValue.name();
            }

            if (value instanceof List<?> list) {
                List<Object> values = new ArrayList<>();

                for (Object element : list) {
                    values.add(write(element));
                }

                return List.copyOf(values);
            }

            return value;
        }

        private Object readEnum(ConfigNode node, Class<? extends Enum> type) {
            String value = node.asString().orElseThrow(() -> conversionError("enum"));

            for (Object constant : type.getEnumConstants()) {
                Enum<?> enumValue = (Enum<?>) constant;

                if (enumValue.name().equalsIgnoreCase(value)) {
                    return enumValue;
                }
            }

            throw new ConfigBindException("Invalid enum value '" + value + "' at " + path + ".");
        }

        private List<?> readList(ConfigNode node) {
            if (node.type() != ConfigNodeType.LIST) {
                throw conversionError("list");
            }

            Class<?> elementType = listElementType();
            List<Object> values = new ArrayList<>();

            for (ConfigNode element : node.elements()) {
                values.add(readElement(element, elementType));
            }

            return List.copyOf(values);
        }

        private Object readElement(ConfigNode node, Class<?> type) {
            if (type == String.class) {
                return node.asString().orElseThrow(() -> conversionError("string list element"));
            }

            if (type == Integer.class || type == int.class) {
                return node.asInt().orElseThrow(() -> conversionError("integer list element"));
            }

            if (type == Long.class || type == long.class) {
                return node.asLong().orElseThrow(() -> conversionError("long list element"));
            }

            if (type == Double.class || type == double.class) {
                return node.asDouble().orElseThrow(() -> conversionError("double list element"));
            }

            if (type == Boolean.class || type == boolean.class) {
                return node.asBoolean().orElseThrow(() -> conversionError("boolean list element"));
            }

            if (type.isEnum()) {
                return readEnum(node, type.asSubclass(Enum.class));
            }

            throw new ConfigBindException("Unsupported list element type " + type.getName() + " at " + path + ".");
        }

        private Class<?> listElementType() {
            Type genericType = field.getGenericType();

            if (!(genericType instanceof ParameterizedType parameterizedType)) {
                throw new ConfigBindException("List config field " + field.getName() + " must declare an element type.");
            }

            Type argument = parameterizedType.getActualTypeArguments()[0];

            if (!(argument instanceof Class<?> elementType)) {
                throw new ConfigBindException("List config field " + field.getName() + " uses an unsupported element type.");
            }

            return elementType;
        }

        private Object get(Object instance) {
            try {
                return field.get(instance);
            } catch (IllegalAccessException exception) {
                throw new ConfigBindException("Cannot read config field " + field.getName() + ".", exception);
            }
        }

        private void set(Object instance, Object value) {
            try {
                field.set(instance, value);
            } catch (IllegalAccessException exception) {
                throw new ConfigBindException("Cannot write config field " + field.getName() + ".", exception);
            }
        }

        private void validate(Object value) {
            if (required && value == null) {
                throw new ConfigBindException("Required config value at " + path + " cannot be null.");
            }

            validateAccepted(value);
            validateRange(value);
        }

        private void validateAccepted(Object value) {
            if (accepted.isEmpty()) {
                return;
            }

            if (value instanceof Iterable<?> iterable) {
                for (Object element : iterable) {
                    validateAcceptedSingle(element);
                }

                return;
            }

            validateAcceptedSingle(value);
        }

        private void validateAcceptedSingle(Object value) {
            String actual = acceptedValue(value);

            if (accepted.contains(actual)) {
                return;
            }

            throw new ConfigBindException("Invalid value '" + actual + "' at " + path + ". Accepted values: " + accepted + ".");
        }

        private void validateRange(Object value) {
            if (range == null) {
                return;
            }

            if (value instanceof Iterable<?> iterable) {
                for (Object element : iterable) {
                    validateRangeSingle(element);
                }

                return;
            }

            validateRangeSingle(value);
        }

        private void validateRangeSingle(Object value) {
            if (!(value instanceof Number number)) {
                throw new ConfigBindException("@Range can only be used on numeric fields or numeric lists at " + path + ".");
            }

            double actual = number.doubleValue();

            if (actual < range.min()) {
                throw new ConfigBindException("Value " + actual + " at " + path + " is below minimum " + range.min() + ".");
            }

            if (actual > range.max()) {
                throw new ConfigBindException("Value " + actual + " at " + path + " is above maximum " + range.max() + ".");
            }
        }

        private String acceptedValue(Object value) {
            if (value instanceof Enum<?> enumValue) {
                return enumValue.name();
            }

            return String.valueOf(value);
        }

        private ConfigBindException conversionError(String target) {
            return new ConfigBindException("Cannot convert value at " + path + " to " + target + ".");
        }

        private static ConfigPath path(Class<?> owner, Field field) {
            Prefix prefix = owner.getAnnotation(Prefix.class);
            Key key = field.getAnnotation(Key.class);
            String prefixValue = prefix == null ? "" : prefix.value().trim();
            String keyValue = key == null ? "" : key.value().trim();

            if (keyValue.isBlank()) {
                keyValue = field.getName();
            }

            if (prefixValue.isBlank()) {
                return ConfigPath.parse(keyValue);
            }

            return ConfigPath.parse(prefixValue + "." + keyValue);
        }

        private static Set<String> accepted(Field field) {
            Set<String> values = new LinkedHashSet<>();
            Accept accept = field.getAnnotation(Accept.class);
            Allowed allowed = field.getAnnotation(Allowed.class);

            if (accept != null) {
                values.addAll(Arrays.asList(accept.value()));
            }

            if (allowed != null) {
                values.addAll(Arrays.asList(allowed.value()));
            }

            values.removeIf(value -> value == null || value.isBlank());
            return values;
        }

        private static List<String> comment(Field field) {
            Comment comment = field.getAnnotation(Comment.class);

            if (comment == null) {
                return List.of();
            }

            return cleanLines(comment.value());
        }

    }

}