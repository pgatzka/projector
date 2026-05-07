package io.github.pgatzka.projector.rest.exception;

import java.util.UUID;

public class LabelNotInProjectException extends RuntimeException {
    public LabelNotInProjectException(UUID labelId, String projectKey) {
        super("Label " + labelId + " does not belong to project " + projectKey);
    }
}
