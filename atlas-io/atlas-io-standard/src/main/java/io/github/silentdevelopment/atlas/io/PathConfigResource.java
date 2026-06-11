package io.github.silentdevelopment.atlas.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class PathConfigResource implements ConfigResource {

    private final Path path;

    private PathConfigResource(Path path) {
        this.path = Objects.requireNonNull(path, "path");
    }

    public static PathConfigResource of(Path path) {
        return new PathConfigResource(path);
    }

    public Path path() {
        return path;
    }

    @Override
    public String name() {
        return path.toString();
    }

    @Override
    public InputStream openInput() throws IOException {
        return Files.newInputStream(path);
    }

    @Override
    public OutputStream openOutput() throws IOException {
        Path parent = path.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        return Files.newOutputStream(path);
    }

    @Override
    public boolean exists() {
        return Files.exists(path);
    }

}