package io.github.silentdevelopment.atlas.core;

import io.github.silentdevelopment.atlas.core.node.ConfigNodes;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.ConfigNodeType;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultConfigNodeTest {

    @Test
    void objectNodeConvertsNestedMapsAndLists() {
        Map<String, Object> server = new LinkedHashMap<>();
        server.put("name", "Lobby");
        server.put("ports", List.of(25565, 25566));

        MutableConfigNode node = ConfigNodes.object();
        node.setChild("server", server);

        ConfigNode serverNode = node.child("server").orElseThrow();

        assertEquals(ConfigNodeType.OBJECT, serverNode.type());
        assertEquals("Lobby", serverNode.child("name").flatMap(ConfigNode::asString).orElseThrow());
        assertEquals(25565, serverNode.child("ports").flatMap(ports -> ports.element(0)).flatMap(ConfigNode::asInt).orElseThrow());
        assertEquals(25566, serverNode.child("ports").flatMap(ports -> ports.element(1)).flatMap(ConfigNode::asInt).orElseThrow());
    }

    @Test
    void listNodePadsMissingIndexesWithNullNodes() {
        MutableConfigNode node = ConfigNodes.list();

        node.setElement(2, "world_the_end");

        assertEquals(ConfigNodeType.NULL, node.element(0).orElseThrow().type());
        assertEquals(ConfigNodeType.NULL, node.element(1).orElseThrow().type());
        assertEquals("world_the_end", node.element(2).flatMap(ConfigNode::asString).orElseThrow());
    }

    @Test
    void scalarNodeConvertsCompatibleValues() {
        MutableConfigNode integerNode = ConfigNodes.scalar("25565");
        MutableConfigNode booleanNode = ConfigNodes.scalar("true");
        MutableConfigNode doubleNode = ConfigNodes.scalar(12);

        assertEquals(25565, integerNode.asInt().orElseThrow());
        assertTrue(booleanNode.asBoolean().orElseThrow());
        assertEquals(12D, doubleNode.asDouble().orElseThrow());
    }

    @Test
    void invalidScalarConversionsReturnEmptyOptionals() {
        MutableConfigNode node = ConfigNodes.scalar("not-a-number");

        assertFalse(node.asInt().isPresent());
        assertFalse(node.asLong().isPresent());
        assertFalse(node.asDouble().isPresent());
        assertFalse(node.asBoolean().isPresent());
    }

    @Test
    void changingTypeClearsPreviousState() {
        MutableConfigNode node = ConfigNodes.object();
        node.setChild("server", "Lobby");

        node.setElement(0, "world");

        assertEquals(ConfigNodeType.LIST, node.type());
        assertTrue(node.children().isEmpty());
        assertEquals("world", node.element(0).flatMap(ConfigNode::asString).orElseThrow());
    }

    @Test
    void exposedChildrenAndElementsAreUnmodifiable() {
        MutableConfigNode object = ConfigNodes.object();
        object.setChild("server", "Lobby");

        MutableConfigNode list = ConfigNodes.list();
        list.setElement(0, "world");

        assertThrows(UnsupportedOperationException.class, () -> object.children().put("database", ConfigNodes.scalar("localhost")));
        assertThrows(UnsupportedOperationException.class, () -> list.elements().add(ConfigNodes.scalar("world_nether")));
    }

    @Test
    void rejectsInvalidChildKeysAndIndexes() {
        MutableConfigNode node = ConfigNodes.object();

        assertThrows(IllegalArgumentException.class, () -> node.setChild(" ", "value"));
        assertThrows(IllegalArgumentException.class, () -> node.childOrCreate(" "));
        assertThrows(IllegalArgumentException.class, () -> node.setElement(-1, "value"));
        assertThrows(IllegalArgumentException.class, () -> node.elementOrCreate(-1));
    }

}