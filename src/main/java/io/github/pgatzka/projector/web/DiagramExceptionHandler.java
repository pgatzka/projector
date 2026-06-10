package io.github.pgatzka.projector.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.pgatzka.projector.parser.DiagramParseException;
import io.github.pgatzka.projector.store.DiagramNotFoundException;

/**
 * Maps domain exceptions to HTTP responses across all controllers:
 * <ul>
 * <li>{@link DiagramParseException} &rarr; 422 with a JSON list of problems</li>
 * <li>{@link DiagramNotFoundException} &rarr; 404</li>
 * <li>{@link IllegalArgumentException} (bad request body) &rarr; 400</li>
 * </ul>
 */
@RestControllerAdvice
public class DiagramExceptionHandler {

	@ExceptionHandler(DiagramParseException.class)
	public ResponseEntity<RenderErrorResponse> handleParseError(DiagramParseException ex) {
		return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(RenderErrorResponse.from(ex));
	}

	@ExceptionHandler(DiagramNotFoundException.class)
	public ResponseEntity<ErrorMessage> handleNotFound(DiagramNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessage(ex.getMessage()));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorMessage> handleBadRequest(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(new ErrorMessage(ex.getMessage()));
	}
}
