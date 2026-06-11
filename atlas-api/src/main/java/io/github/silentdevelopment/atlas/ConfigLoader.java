package io.github.silentdevelopment.atlas;

import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigLoadException;
import io.github.silentdevelopment.atlas.exception.ConfigSaveException;

import java.io.IOException;

public interface ConfigLoader {

    ConfigDocument load() throws IOException, ConfigLoadException;

    void save(ConfigDocument document) throws IOException, ConfigSaveException;

}