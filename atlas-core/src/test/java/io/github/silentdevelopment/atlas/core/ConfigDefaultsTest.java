package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.io.ConfigSink;
import io.github.silentdevelopment.atlas.io.ConfigSource;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigDefaultsTest {

    @Test
    void copyTransfersBytesFromSourceToSink() throws Exception {
        MemorySource source = new MemorySource("server:\n  name: Lobby\n");
        MemorySink sink = new MemorySink();

        ConfigDefaults.copy(source, sink);

        assertEquals("server:\n  name: Lobby\n", sink.value());
    }

    @Test
    void copyRejectsNullSourceAndSink() {
        MemorySource source = new MemorySource("value");
        MemorySink sink = new MemorySink();

        assertThrows(NullPointerException.class, () -> ConfigDefaults.copy(null, sink));
        assertThrows(NullPointerException.class, () -> ConfigDefaults.copy(source, null));
    }

    private record MemorySource(String value) implements ConfigSource {

        @Override
        public String name() {
            return "memory-source";
        }

        @Override
        public InputStream openInput() {
            return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
        }

    }

    private static final class MemorySink implements ConfigSink {

        private byte[] bytes = new byte[0];

        @Override
        public String name() {
            return "memory-sink";
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

        private String value() {
            return new String(bytes, StandardCharsets.UTF_8);
        }

    }

}