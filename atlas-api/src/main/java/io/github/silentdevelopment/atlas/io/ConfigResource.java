package io.github.silentdevelopment.atlas.io;

import java.io.IOException;

public interface ConfigResource extends ConfigSource, ConfigSink {

    boolean exists() throws IOException;

}