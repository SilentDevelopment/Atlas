package io.github.silentdevelopment.atlas.io;

import java.io.IOException;
import java.io.InputStream;

public interface ConfigSource {

    String name();

    InputStream openInput() throws IOException;;

}