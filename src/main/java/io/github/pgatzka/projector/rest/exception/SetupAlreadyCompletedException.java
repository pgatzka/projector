package io.github.pgatzka.projector.rest.exception;

public class SetupAlreadyCompletedException extends RuntimeException {
    public SetupAlreadyCompletedException() {
        super("Initial setup has already been completed.");
    }
}
