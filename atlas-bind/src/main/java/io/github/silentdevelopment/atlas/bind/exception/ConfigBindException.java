package io.github.silentdevelopment.atlas.bind.exception;

import io.github.silentdevelopment.atlas.exception.ConfigException;

public class ConfigBindException extends ConfigException {

    public ConfigBindException(String message) {
        super(message);
    }

    public ConfigBindException(String message, Throwable cause) {
        super(message, cause);
    }

}