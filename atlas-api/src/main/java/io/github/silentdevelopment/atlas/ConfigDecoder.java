package io.github.silentdevelopment.atlas;

import io.github.silentdevelopment.atlas.document.ConfigDocument;
import io.github.silentdevelopment.atlas.exception.ConfigDecodeException;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigDecoder {

    String format();

    ConfigDocument decode(InputStream input) throws IOException, ConfigDecodeException;

}