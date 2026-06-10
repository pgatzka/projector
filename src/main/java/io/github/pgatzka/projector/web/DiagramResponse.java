package io.github.pgatzka.projector.web;

import java.time.Instant;

import io.github.pgatzka.projector.store.DiagramDocument;

/** Full diagram representation, including the YAML source. */
public record DiagramResponse(String id, String name, String yaml, Instant createdAt, Instant updatedAt) {

	public static DiagramResponse from(DiagramDocument doc) {
		return new DiagramResponse(doc.getId(), doc.getName(), doc.getYaml(), doc.getCreatedAt(), doc.getUpdatedAt());
	}
}
