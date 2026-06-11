package io.github.silentdevelopment.atlas.core.document;

import io.github.silentdevelopment.atlas.core.node.ConfigNodes;
import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableCommentedConfigDocument;
import io.github.silentdevelopment.atlas.document.MutableConfigDocument;
import io.github.silentdevelopment.atlas.node.ConfigNode;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;

import java.util.Objects;

public final class ConfigDocuments {

    private ConfigDocuments() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static ConfigDocument of(ConfigNode root) {
        return new DefaultConfigDocument(Objects.requireNonNull(root, "root"));
    }

    public static MutableConfigDocument mutable() {
        return new DefaultMutableConfigDocument(ConfigNodes.object());
    }

    public static MutableConfigDocument mutable(MutableConfigNode root) {
        return new DefaultMutableConfigDocument(Objects.requireNonNull(root, "root"));
    }

    public static MutableCommentedConfigDocument commented() {
        return new DefaultMutableConfigDocument(ConfigNodes.object());
    }

    public static MutableCommentedConfigDocument commented(MutableConfigNode root) {
        return new DefaultMutableConfigDocument(Objects.requireNonNull(root, "root"));
    }

}