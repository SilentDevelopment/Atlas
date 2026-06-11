package io.github.silentdevelopment.atlas.bind;

import java.io.IOException;
import java.util.Objects;

public final class BoundConfigGroupRequest {

    private final BindConfigRequest request;
    private final Class<?>[] types;

    BoundConfigGroupRequest(BindConfigRequest request, Class<?>[] types) {
        this.request = Objects.requireNonNull(request, "request");
        this.types = Objects.requireNonNull(types, "types").clone();
    }

    public BoundConfigGroup load() throws IOException {
        return request.loadGroup(types);
    }

}