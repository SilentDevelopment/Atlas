package io.github.silentdevelopment.atlas.bind;

import java.io.IOException;
import java.util.Objects;

public final class BoundConfigRequest<T> {

    private final BindConfigRequest request;
    private final Class<T> type;

    BoundConfigRequest(BindConfigRequest request, Class<T> type) {
        this.request = Objects.requireNonNull(request, "request");
        this.type = Objects.requireNonNull(type, "type");
    }

    public BoundConfig<T> load() throws IOException {
        return request.loadBound(type);
    }

}