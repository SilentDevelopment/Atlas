package io.github.silentdevelopment.atlas.core;

public final class ConfigLoaders {

    private ConfigLoaders() {
        throw new UnsupportedOperationException("Utility class.");
    }

    public static DefaultConfigLoader.Builder builder() {
        return DefaultConfigLoader.builder();
    }

}