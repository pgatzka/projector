package io.github.pgatzka.projector.web;

import java.time.Instant;

import io.github.pgatzka.projector.store.DiagramDocument;

/** Lightweight diagram representation for listings (no YAML). */
public record DiagramSummary(String id, String name, Instant createdAt, Instant updatedAt) {

	public static DiagramSummary from(DiagramDocument doc) {
		return new DiagramSummary(doc.getId(), doc.getName(), doc.getCreatedAt(), doc.getUpdatedAt());
	}
}
