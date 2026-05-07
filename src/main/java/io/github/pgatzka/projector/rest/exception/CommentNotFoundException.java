package io.github.pgatzka.projector.rest.exception;

import java.util.UUID;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(UUID id) {
        super("No comment with id '" + id + "'.");
    }
}
