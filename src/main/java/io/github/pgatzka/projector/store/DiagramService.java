package io.github.pgatzka.projector.store;

import java.util.List;

import org.springframework.stereotype.Service;

/**
 * CRUD operations for saved diagrams. Saving does not validate the YAML, so work in
 * progress can be stored even if it does not yet parse or render.
 */
@Service
public class DiagramService {

	private final DiagramRepository repository;

	public DiagramService(DiagramRepository repository) {
		this.repository = repository;
	}

	public DiagramDocument create(String name, String yaml) {
		return repository.save(new DiagramDocument(name, yaml));
	}

	public List<DiagramDocument> findAll() {
		return repository.findAll();
	}

	public DiagramDocument get(String id) {
		return repository.findById(id).orElseThrow(() -> new DiagramNotFoundException(id));
	}

	public DiagramDocument update(String id, String name, String yaml) {
		DiagramDocument existing = get(id);
		existing.setName(name);
		existing.setYaml(yaml);
		return repository.save(existing);
	}

	public void delete(String id) {
		if (!repository.existsById(id)) {
			throw new DiagramNotFoundException(id);
		}
		repository.deleteById(id);
	}
}
