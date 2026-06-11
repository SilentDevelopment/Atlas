package io.github.silentdevelopment.atlas.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class ClasspathConfigSource implements ConfigSource {

    private final ClassLoader classLoader;
    private final String resource;

    private ClasspathConfigSource(ClassLoader classLoader, String resource) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
        this.resource = normalize(Objects.requireNonNull(resource, "resource"));
    }

    public static ClasspathConfigSource of(Class<?> owner, String resource) {
        Objects.requireNonNull(owner, "owner");
        return new ClasspathConfigSource(owner.getClassLoader(), resource);
    }

    public static ClasspathConfigSource of(ClassLoader classLoader, String resource) {
        return new ClasspathConfigSource(classLoader, resource);
    }

    @Override
    public String name() {
        return "classpath:" + resource;
    }

    @Override
    public InputStream openInput() throws IOException {
        InputStream input = classLoader.getResourceAsStream(resource);

        if (input == null) {
            throw new FileNotFoundException("Classpath resource not found: " + resource);
        }

        return input;
    }

    private static String normalize(String resource) {
        if (resource.startsWith("/")) {
            return resource.substring(1);
        }

        return resource;
    }

}