package io.github.pgatzka.projector.web;

/** Simple single-message error body (used for 400 and 404 responses). */
public record ErrorMessage(String message) {
}
