package io.github.pgatzka.projector.parser;

/**
 * A single parse or validation problem. {@code line}/{@code column} are populated for
 * YAML syntax errors (1-based, from the parser) and are null for semantic errors that
 * are not tied to a specific location.
 *
 * @param message human-readable description of the problem
 * @param line    1-based line, or null if not location-specific
 * @param column  1-based column, or null if not location-specific
 */
public record DiagramError(String message, Integer line, Integer column) {

	/** Creates a location-less (semantic) error. */
	public static DiagramError of(String message) {
		return new DiagramError(message, null, null);
	}

	/** Creates a located (syntax) error. */
	public static DiagramError at(String message, int line, int column) {
		return new DiagramError(message, line, column);
	}
}
