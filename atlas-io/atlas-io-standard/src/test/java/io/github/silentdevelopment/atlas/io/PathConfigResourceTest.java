package io.github.silentdevelopment.atlas.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathConfigResourceTest {

    @TempDir
    private Path tempDirectory;

    @Test
    void openOutputCreatesParentDirectoriesAndWritesFile() throws Exception {
        Path path = tempDirectory.resolve("configs/server/config.yml");
        PathConfigResource resource = PathConfigResource.of(path);

        try (OutputStream output = resource.openOutput()) {
            output.write("server: Lobby".getBytes(StandardCharsets.UTF_8));
        }

        assertTrue(resource.exists());

        try (InputStream input = resource.openInput()) {
            assertEquals("server: Lobby", new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void existsReflectsFilePresence() throws Exception {
        Path path = tempDirectory.resolve("config.yml");
        PathConfigResource resource = PathConfigResource.of(path);

        assertFalse(resource.exists());

        try (OutputStream ignored = resource.openOutput()) {
        }

        assertTrue(resource.exists());
    }

    @Test
    void nameReturnsPathString() {
        Path path = tempDirectory.resolve("config.yml");
        PathConfigResource resource = PathConfigResource.of(path);

        assertEquals(path.toString(), resource.name());
    }

    @Test
    void factoryRejectsNullPath() {
        assertThrows(NullPointerException.class, () -> PathConfigResource.of(null));
    }

}