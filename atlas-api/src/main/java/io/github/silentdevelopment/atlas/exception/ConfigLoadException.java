package io.github.silentdevelopment.atlas.exception;

public final class ConfigLoadException extends ConfigException {

    public ConfigLoadException(String message) {
        super(message);
    }

    public ConfigLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}