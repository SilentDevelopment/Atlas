package io.github.silentdevelopment.atlas.exception;

public final class ConfigDecodeException extends ConfigException {

    public ConfigDecodeException(String message) {
        super(message);
    }

    public ConfigDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

}