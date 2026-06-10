package io.github.pgatzka.projector.store;

/** Thrown when a diagram id does not exist. Mapped to HTTP 404. */
public class DiagramNotFoundException extends RuntimeException {

	public DiagramNotFoundException(String id) {
		super("Diagram not found: " + id);
	}
}
