package io.github.pgatzka.projector.rest.exception;

public class IssueNotFoundException extends RuntimeException {
    public IssueNotFoundException(String identifier) {
        super("No issue with identifier '" + identifier + "'.");
    }
}
