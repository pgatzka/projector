package io.github.pgatzka.projector.web;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.github.pgatzka.projector.parser.DiagramParseException;

/**
 * JSON body returned when a diagram fails to parse or validate. Carries every problem so
 * the editor can show them all at once. {@code line}/{@code column} are omitted when not
 * applicable (semantic errors).
 */
public record RenderErrorResponse(List<Item> errors) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Item(String message, Integer line, Integer column) {
	}

	public static RenderErrorResponse from(DiagramParseException ex) {
		List<Item> items = ex.errors().stream()
				.map(e -> new Item(e.message(), e.line(), e.column()))
				.toList();
		return new RenderErrorResponse(items);
	}
}
