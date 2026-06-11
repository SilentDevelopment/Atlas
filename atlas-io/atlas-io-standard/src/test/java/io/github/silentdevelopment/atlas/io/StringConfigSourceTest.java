package io.github.silentdevelopment.atlas.io;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class StringConfigSourceTest {

    @Test
    void defaultFactoryUsesUtf8AndStringName() throws Exception {
        StringConfigSource source = StringConfigSource.of("server: Lobby");

        assertEquals("string", source.name());
        assertEquals("server: Lobby", new String(source.openInput().readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void customFactoryUsesGivenNameValueAndCharset() throws Exception {
        StringConfigSource source = StringConfigSource.of("custom", "æøå", StandardCharsets.ISO_8859_1);

        assertEquals("custom", source.name());
        assertEquals("æøå", new String(source.openInput().readAllBytes(), StandardCharsets.ISO_8859_1));
    }

    @Test
    void customFactoryRejectsNullValues() {
        assertThrows(NullPointerException.class, () -> StringConfigSource.of(null, "value", StandardCharsets.UTF_8));
        assertThrows(NullPointerException.class, () -> StringConfigSource.of("name", null, StandardCharsets.UTF_8));
        assertThrows(NullPointerException.class, () -> StringConfigSource.of("name", "value", null));
    }

}