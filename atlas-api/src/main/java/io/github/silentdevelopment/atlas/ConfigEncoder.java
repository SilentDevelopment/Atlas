package io.github.silentdevelopment.atlas;

import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigEncodeException;

import java.io.IOException;
import java.io.OutputStream;

public interface ConfigEncoder {

    String format();

    void encode(ConfigDocument document, OutputStream output) throws IOException, ConfigEncodeException;

}