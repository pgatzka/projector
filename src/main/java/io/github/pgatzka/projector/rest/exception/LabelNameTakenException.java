package io.github.pgatzka.projector.rest.exception;

public class LabelNameTakenException extends RuntimeException {
    public LabelNameTakenException(String projectKey, String name) {
        super("Label name '" + name + "' is already in use in project '" + projectKey + "'.");
    }
}
