package io.github.pgatzka.projector.web;

/** Request body for creating or updating a diagram. */
public record DiagramRequest(String name, String yaml) {
}
