package io.github.silentdevelopment.atlas.document;

import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.node.MutableConfigNode;

public interface MutableConfigDocument extends ConfigDocument {

    @Override
    MutableConfigNode root();

    MutableConfigNode nodeOrCreate(ConfigPath path);

    void set(ConfigPath path, Object value);

    boolean remove(ConfigPath path);

    void clear();

}