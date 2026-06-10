package io.github.pgatzka.projector.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.pgatzka.projector.parser.DiagramParseException;

/**
 * Translates diagram parse/validation failures into a 422 response with a JSON list of
 * problems. Applies to any controller (e.g. render and, later, save).
 */
@RestControllerAdvice
public class DiagramExceptionHandler {

	@ExceptionHandler(DiagramParseException.class)
	public ResponseEntity<RenderErrorResponse> handleParseError(DiagramParseException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(RenderErrorResponse.from(ex));
	}
}
