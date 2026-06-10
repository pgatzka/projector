package io.github.pgatzka.projector.model;

import java.util.Locale;
import java.util.Optional;

/**
 * The kinds of node an activity diagram can contain. The YAML {@code type} value maps
 * to these case-insensitively (e.g. {@code decision} -> {@link #DECISION}).
 */
public enum NodeType {
	START,
	END,
	ACTION,
	DECISION,
	MERGE,
	FORK,
	JOIN;

	/**
	 * Parses a YAML {@code type} value into a {@link NodeType}, case-insensitively.
	 * Returns empty for null, blank, or unknown values so the caller can collect a
	 * validation error rather than throwing.
	 */
	public static Optional<NodeType> fromYaml(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}
		try {
			return Optional.of(NodeType.valueOf(value.trim().toUpperCase(Locale.ROOT)));
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}
}
