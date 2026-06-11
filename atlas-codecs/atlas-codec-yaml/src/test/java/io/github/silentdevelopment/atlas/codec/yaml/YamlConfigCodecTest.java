package io.github.silentdevelopment.atlas.codec.yaml;

import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlConfigCodecTest {

    @Test
    void formatIsYaml() {
        YamlConfigCodec codec = YamlConfigCodec.create();

        assertEquals("yaml", codec.format());
    }

    @Test
    void decodeReadsNestedObjectsListsAndScalars() throws Exception {
        YamlConfigCodec codec = YamlConfigCodec.create();
        String yaml = """
                server:
                  name: Lobby
                  port: 25565
                  online-mode: true
                database:
                  host: localhost
                  port: 3306
                worlds:
                  - world
                  - world_nether
                  - world_the_end
                """;

        ConfigDocument document = codec.decode(input(yaml));

        assertEquals("Lobby", document.getString(ConfigPath.of("server", "name"), ""));
        assertEquals(25565, document.getInt(ConfigPath.of("server", "port"), 0));
        assertTrue(document.getBoolean(ConfigPath.of("server", "online-mode"), false));
        assertEquals("localhost", document.getString(ConfigPath.of("database", "host"), ""));
        assertEquals("world", document.node(ConfigPath.of("worlds")).flatMap(node -> node.element(0)).flatMap(ConfigNode::asString).orElseThrow());
        assertEquals("world_the_end", document.node(ConfigPath.of("worlds")).flatMap(node -> node.element(2)).flatMap(ConfigNode::asString).orElseThrow());
    }

    @Test
    void decodeReturnsMutableDocument() throws Exception {
        YamlConfigCodec codec = YamlConfigCodec.create();

        ConfigDocument loaded = codec.decode(input("server:\n  name: Lobby\n"));

        MutableConfigDocument document = assertInstanceOf(MutableConfigDocument.class, loaded);
        document.set(ConfigPath.of("server", "name"), "Survival");
        assertEquals("Survival", document.getString(ConfigPath.of("server", "name"), ""));
    }

    @Test
    void emptyYamlDecodesAsObjectRoot() throws Exception {
        YamlConfigCodec codec = YamlConfigCodec.create();

        ConfigDocument document = codec.decode(input(""));

        assertEquals(ConfigNodeType.OBJECT, document.root().type());
        assertTrue(document.root().children().isEmpty());
    }

    @Test
    void encodeAndDecodeRoundTripsMutationsFromExample() throws Exception {
        YamlConfigCodec codec = YamlConfigCodec.create();
        String yaml = """
                server:
                  name: Lobby
                  port: 25565
                  online-mode: true
                database:
                  host: localhost
                  port: 3306
                messages:
                  welcome: "Welcome, {player}!"
                """;
        MutableConfigDocument document = assertInstanceOf(MutableConfigDocument.class, codec.decode(input(yaml)));

        document.set(ConfigPath.of("server", "name"), "Survival");
        document.set(ConfigPath.of("server", "port"), 25566);
        document.set(ConfigPath.of("server", "motd"), "A YAML round-trip test.");
        document.set(ConfigPath.of("features", "chat"), true);
        document.remove(ConfigPath.of("database", "port"));

        ConfigDocument reloaded = codec.decode(input(encode(codec, document)));

        assertEquals("Survival", reloaded.getString(ConfigPath.of("server", "name"), ""));
        assertEquals(25566, reloaded.getInt(ConfigPath.of("server", "port"), 0));
        assertEquals("A YAML round-trip test.", reloaded.getString(ConfigPath.of("server", "motd"), ""));
        assertTrue(reloaded.getBoolean(ConfigPath.of("features", "chat"), false));
        assertFalse(reloaded.getInt(ConfigPath.of("database", "port")).isPresent());
    }

    @Test
    void encodePreservesListsAndNullValuesThroughRoundTrip() throws Exception {
        YamlConfigCodec codec = YamlConfigCodec.create();
        String yaml = """
                worlds:
                  - world
                  - null
                  - world_the_end
                """;

        ConfigDocument reloaded = codec.decode(input(encode(codec, codec.decode(input(yaml)))));
        ConfigNode worlds = reloaded.node(ConfigPath.of("worlds")).orElseThrow();

        assertEquals("world", worlds.element(0).flatMap(ConfigNode::asString).orElseThrow());
        assertEquals(ConfigNodeType.NULL, worlds.element(1).orElseThrow().type());
        assertEquals("world_the_end", worlds.element(2).flatMap(ConfigNode::asString).orElseThrow());
    }

    @Test
    void rejectsNullInputAndOutputArguments() {
        YamlConfigCodec codec = YamlConfigCodec.create();

        assertThrows(NullPointerException.class, () -> codec.decode(null));
        assertThrows(NullPointerException.class, () -> codec.encode(null, new ByteArrayOutputStream()));
        assertThrows(NullPointerException.class, () -> codec.encode(codec.decode(input("server:\n  name: Lobby\n")), null));
    }

    private static ByteArrayInputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String encode(YamlConfigCodec codec, ConfigDocument document) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        codec.encode(document, output);
        return output.toString(StandardCharsets.UTF_8);
    }

}