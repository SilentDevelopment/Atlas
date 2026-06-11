package io.github.silentdevelopment.atlas.io;

import java.io.IOException;
import java.io.OutputStream;

public interface ConfigSink {

    String name();

    OutputStream openOutput() throws IOException;

}