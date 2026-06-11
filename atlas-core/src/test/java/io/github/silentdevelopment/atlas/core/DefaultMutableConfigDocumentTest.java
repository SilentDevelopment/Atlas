package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.ConfigKey;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.core.document.ConfigDocuments;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultMutableConfigDocumentTest {

    @Test
    void setCreatesNestedNodesAndReadsTypedValues() {
        MutableConfigDocument document = ConfigDocuments.mutable();

        document.set(ConfigPath.of("server", "name"), "Lobby");
        document.set(ConfigPath.of("server", "port"), 25565);
        document.set(ConfigPath.of("server", "online-mode"), true);

        assertEquals("Lobby", document.getString(ConfigPath.of("server", "name"), ""));
        assertEquals(25565, document.getInt(ConfigPath.of("server", "port"), 0));
        assertTrue(document.getBoolean(ConfigPath.of("server", "online-mode"), false));
        assertTrue(document.node(ConfigPath.of("server")).isPresent());
    }

    @Test
    void removeDeletesLeafWithoutDeletingParent() {
        MutableConfigDocument document = ConfigDocuments.mutable();

        document.set(ConfigPath.of("database", "host"), "localhost");
        document.set(ConfigPath.of("database", "port"), 3306);

        assertTrue(document.remove(ConfigPath.of("database", "port")));
        assertFalse(document.getInt(ConfigPath.of("database", "port")).isPresent());
        assertEquals("localhost", document.getString(ConfigPath.of("database", "host"), ""));
        assertFalse(document.remove(ConfigPath.of("database", "missing")));
    }

    @Test
    void getUsesConfigKeyDefaultWhenValueIsMissing() {
        MutableConfigDocument document = ConfigDocuments.mutable();
        ConfigKey<String> key = ConfigKey.string(ConfigPath.of("server", "name")).defaultValue("Lobby");

        assertEquals("Lobby", document.get(key));
    }

    @Test
    void getThrowsForMissingRequiredConfigKey() {
        MutableConfigDocument document = ConfigDocuments.mutable();
        ConfigKey<String> key = ConfigKey.string(ConfigPath.of("server", "name")).required();

        assertThrows(IllegalStateException.class, () -> document.get(key));
    }

    @Test
    void nodeOrCreateReturnsExistingNestedNode() {
        MutableConfigDocument document = ConfigDocuments.mutable();

        document.nodeOrCreate(ConfigPath.of("features", "chat")).setScalar(true);

        assertTrue(document.getBoolean(ConfigPath.of("features", "chat"), false));
        assertEquals(ConfigNodeType.OBJECT, document.root().type());
    }

    @Test
    void clearEmptiesObjectRoot() {
        MutableConfigDocument document = ConfigDocuments.mutable();
        document.set(ConfigPath.of("server", "name"), "Lobby");

        document.clear();

        assertTrue(document.root().children().isEmpty());
        assertEquals(ConfigNodeType.OBJECT, document.root().type());
    }

}