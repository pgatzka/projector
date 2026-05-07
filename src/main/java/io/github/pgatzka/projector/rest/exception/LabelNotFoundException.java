package io.github.pgatzka.projector.rest.exception;

import java.util.UUID;

public class LabelNotFoundException extends RuntimeException {
    public LabelNotFoundException(UUID id) {
        super("No label with id '" + id + "'.");
    }
}
