package io.github.pgatzka.projector.parser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when a diagram cannot be parsed or fails validation. Carries the full list of
 * collected problems so the caller (and ultimately the editor) can surface them all at
 * once rather than one at a time.
 */
public class DiagramParseException extends RuntimeException {

	private final transient List<DiagramError> errors;

	public DiagramParseException(List<DiagramError> errors) {
		super(summarize(errors));
		this.errors = List.copyOf(errors);
	}

	public List<DiagramError> errors() {
		return errors;
	}

	private static String summarize(List<DiagramError> errors) {
		if (errors.isEmpty()) {
			return "Invalid diagram";
		}
		return errors.stream()
				.map(DiagramError::message)
				.collect(Collectors.joining("; "));
	}
}
