package io.github.silentdevelopment.atlas.document;

import io.github.silentdevelopment.atlas.ConfigKey;
import io.github.silentdevelopment.atlas.ConfigPath;
import io.github.silentdevelopment.atlas.node.ConfigNode;

import java.util.Optional;

public interface ConfigDocument {

    ConfigNode root();

    Optional<ConfigNode> node(ConfigPath path);

    <T> Optional<T> get(ConfigPath path, Class<T> type);

    <T> T get(ConfigKey<T> key);

    Optional<String> getString(ConfigPath path);

    String getString(ConfigPath path, String fallback);

    Optional<Integer> getInt(ConfigPath path);

    int getInt(ConfigPath path, int fallback);

    Optional<Boolean> getBoolean(ConfigPath path);

    boolean getBoolean(ConfigPath path, boolean fallback);

}