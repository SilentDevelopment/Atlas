package io.github.silentdevelopment.atlas.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UrlConfigSourceTest {

    @TempDir
    private Path tempDirectory;

    @Test
    void openInputReadsFromUrl() throws Exception {
        Path file = tempDirectory.resolve("config.yml");
        Files.writeString(file, "server: Lobby", StandardCharsets.UTF_8);

        URL url = file.toUri().toURL();
        UrlConfigSource source = UrlConfigSource.of(url);

        assertEquals(url.toExternalForm(), source.name());

        try (InputStream input = source.openInput()) {
            assertEquals("server: Lobby", new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void factoryRejectsNullUrl() {
        assertThrows(NullPointerException.class, () -> UrlConfigSource.of(null));
    }

}