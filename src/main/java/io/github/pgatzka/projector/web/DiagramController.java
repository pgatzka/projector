package io.github.pgatzka.projector.web;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.pgatzka.projector.store.DiagramDocument;
import io.github.pgatzka.projector.store.DiagramService;

/**
 * CRUD for saved diagrams. Saving stores the YAML as-is (no validation), so work in
 * progress can be persisted; missing diagrams yield 404 and bad requests 400 (see
 * {@link DiagramExceptionHandler}).
 */
@RestController
@RequestMapping("/api/diagrams")
public class DiagramController {

	private final DiagramService service;

	public DiagramController(DiagramService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<DiagramResponse> create(@RequestBody(required = false) DiagramRequest request) {
		validate(request);
		DiagramDocument doc = service.create(request.name().trim(), request.yaml());
		return ResponseEntity.created(URI.create("/api/diagrams/" + doc.getId())).body(DiagramResponse.from(doc));
	}

	@GetMapping
	public List<DiagramSummary> list() {
		return service.findAll().stream().map(DiagramSummary::from).toList();
	}

	@GetMapping("/{id}")
	public DiagramResponse get(@PathVariable String id) {
		return DiagramResponse.from(service.get(id));
	}

	@PutMapping("/{id}")
	public DiagramResponse update(@PathVariable String id, @RequestBody(required = false) DiagramRequest request) {
		validate(request);
		return DiagramResponse.from(service.update(id, request.name().trim(), request.yaml()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable String id) {
		service.delete(id);
		return ResponseEntity.noContent().build();
	}

	private static void validate(DiagramRequest request) {
		if (request == null || request.name() == null || request.name().isBlank()) {
			throw new IllegalArgumentException("'name' is required");
		}
		if (request.yaml() == null) {
			throw new IllegalArgumentException("'yaml' is required");
		}
	}
}
