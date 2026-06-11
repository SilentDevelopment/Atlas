package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.ConfigCodec;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.core.document.ConfigDocuments;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigLoadException;
import io.github.silentdevelopment.atlas.exception.ConfigSaveException;
import io.github.silentdevelopment.atlas.io.ConfigResource;
import io.github.silentdevelopment.atlas.io.ConfigSink;
import io.github.silentdevelopment.atlas.io.ConfigSource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultConfigLoaderTest {

    @Test
    void loadDecodesSource() throws Exception {
        TextConfigCodec codec = new TextConfigCodec();
        ConfigSource source = new MemorySource("config", "Lobby");
        DefaultConfigLoader loader = ConfigLoaders.builder().source(source).decoder(codec).build();

        ConfigDocument document = loader.load();

        assertEquals("Lobby", document.getString(ConfigPath.of("value"), ""));
    }

    @Test
    void saveEncodesDocumentIntoSink() throws Exception {
        TextConfigCodec codec = new TextConfigCodec();
        MemorySink sink = new MemorySink("config");
        MutableConfigDocument document = ConfigDocuments.mutable();
        document.set(ConfigPath.of("value"), "Survival");
        DefaultConfigLoader loader = ConfigLoaders.builder().sink(sink).encoder(codec).build();

        loader.save(document);

        assertEquals("Survival", sink.value());
    }

    @Test
    void loadCopiesDefaultsWhenResourceIsMissing() throws Exception {
        TextConfigCodec codec = new TextConfigCodec();
        MemoryResource resource = new MemoryResource("config", false, "");
        ConfigSource defaults = new MemorySource("defaults", "Default Lobby");
        DefaultConfigLoader loader = ConfigLoaders.builder().resource(resource).codec(codec).defaults(defaults).copyDefaultsIfMissing(true).build();

        ConfigDocument document = loader.load();

        assertTrue(resource.exists());
        assertEquals("Default Lobby", resource.value());
        assertEquals("Default Lobby", document.getString(ConfigPath.of("value"), ""));
    }

    @Test
    void loadDoesNotCopyDefaultsWhenResourceExists() throws Exception {
        TextConfigCodec codec = new TextConfigCodec();
        MemoryResource resource = new MemoryResource("config", true, "Existing Lobby");
        ConfigSource defaults = new MemorySource("defaults", "Default Lobby");
        DefaultConfigLoader loader = ConfigLoaders.builder().resource(resource).codec(codec).defaults(defaults).copyDefaultsIfMissing(true).build();

        ConfigDocument document = loader.load();

        assertEquals("Existing Lobby", resource.value());
        assertEquals("Existing Lobby", document.getString(ConfigPath.of("value"), ""));
    }

    @Test
    void loadFailsWithoutSource() {
        DefaultConfigLoader loader = ConfigLoaders.builder().decoder(new TextConfigCodec()).build();

        assertThrows(ConfigLoadException.class, loader::load);
    }

    @Test
    void loadFailsWithoutDecoder() {
        DefaultConfigLoader loader = ConfigLoaders.builder().source(new MemorySource("config", "Lobby")).build();

        assertThrows(ConfigLoadException.class, loader::load);
    }

    @Test
    void saveFailsWithoutSink() {
        DefaultConfigLoader loader = ConfigLoaders.builder().encoder(new TextConfigCodec()).build();
        MutableConfigDocument document = ConfigDocuments.mutable();

        assertThrows(ConfigSaveException.class, () -> loader.save(document));
    }

    @Test
    void saveFailsWithoutEncoder() {
        DefaultConfigLoader loader = ConfigLoaders.builder().sink(new MemorySink("config")).build();
        MutableConfigDocument document = ConfigDocuments.mutable();

        assertThrows(ConfigSaveException.class, () -> loader.save(document));
    }

    private static final class TextConfigCodec implements ConfigCodec {

        @Override
        public String format() {
            return "text";
        }

        @Override
        public ConfigDocument decode(InputStream input) throws IOException {
            Objects.requireNonNull(input, "input");
            MutableConfigDocument document = ConfigDocuments.mutable();
            document.set(ConfigPath.of("value"), new String(input.readAllBytes(), StandardCharsets.UTF_8));
            return document;
        }

        @Override
        public void encode(ConfigDocument document, OutputStream output) throws IOException {
            Objects.requireNonNull(document, "document");
            Objects.requireNonNull(output, "output");
            output.write(document.getString(ConfigPath.of("value"), "").getBytes(StandardCharsets.UTF_8));
        }

    }

    private static class MemorySource implements ConfigSource {

        private final String name;
        private byte[] bytes;

        private MemorySource(String name, String value) {
            this.name = Objects.requireNonNull(name, "name");
            this.bytes = value.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public InputStream openInput() {
            return new ByteArrayInputStream(bytes);
        }

        protected String value() {
            return new String(bytes, StandardCharsets.UTF_8);
        }

        protected void value(String value) {
            this.bytes = value.getBytes(StandardCharsets.UTF_8);
        }

    }

    private static class MemorySink implements ConfigSink {

        private final String name;
        private byte[] bytes = new byte[0];

        private MemorySink(String name) {
            this.name = Objects.requireNonNull(name, "name");
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public OutputStream openOutput() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    bytes = toByteArray();
                }
            };
        }

        protected String value() {
            return new String(bytes, StandardCharsets.UTF_8);
        }

    }

    private static final class MemoryResource extends MemorySource implements ConfigResource {

        private boolean exists;

        private MemoryResource(String name, boolean exists, String value) {
            super(name, value);
            this.exists = exists;
        }

        @Override
        public OutputStream openOutput() {
            return new ByteArrayOutputStream() {
                @Override
                public void close() throws IOException {
                    super.close();
                    value(toString(StandardCharsets.UTF_8));
                    exists = true;
                }
            };
        }

        @Override
        public boolean exists() {
            return exists;
        }

    }

}