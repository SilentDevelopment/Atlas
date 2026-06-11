package io.github.silentdevelopment.atlas.io;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ClasspathConfigSourceTest {

    @Test
    void ownerFactoryLoadsResourceAndNormalizesLeadingSlash() throws Exception {
        ClasspathConfigSource source = ClasspathConfigSource.of(ClasspathConfigSourceTest.class, "/atlas-test-resource.txt");

        assertEquals("classpath:atlas-test-resource.txt", source.name());

        try (InputStream input = source.openInput()) {
            assertEquals("classpath value", new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void classLoaderFactoryLoadsResource() throws Exception {
        ClassLoader classLoader = ClasspathConfigSourceTest.class.getClassLoader();
        ClasspathConfigSource source = ClasspathConfigSource.of(classLoader, "atlas-test-resource.txt");

        assertEquals("classpath:atlas-test-resource.txt", source.name());

        try (InputStream input = source.openInput()) {
            assertEquals("classpath value", new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Test
    void openInputThrowsWhenResourceIsMissing() {
        ClasspathConfigSource source = ClasspathConfigSource.of(ClasspathConfigSourceTest.class, "missing-resource.txt");

        assertThrows(FileNotFoundException.class, source::openInput);
    }

    @Test
    void factoriesRejectNullArguments() {
        ClassLoader classLoader = ClasspathConfigSourceTest.class.getClassLoader();

        assertThrows(NullPointerException.class, () -> ClasspathConfigSource.of((Class<?>) null, "atlas-test-resource.txt"));
        assertThrows(NullPointerException.class, () -> ClasspathConfigSource.of(ClasspathConfigSourceTest.class, null));
        assertThrows(NullPointerException.class, () -> ClasspathConfigSource.of((ClassLoader) null, "atlas-test-resource.txt"));
        assertThrows(NullPointerException.class, () -> ClasspathConfigSource.of(classLoader, null));
    }

}