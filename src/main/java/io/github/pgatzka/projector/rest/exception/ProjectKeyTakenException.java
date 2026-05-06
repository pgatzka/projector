package io.github.pgatzka.projector.rest.exception;

public class ProjectKeyTakenException extends RuntimeException {
    public ProjectKeyTakenException(String key) {
        super("Project key '" + key + "' is already in use.");
    }
}
