package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.LabelRepository;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class LabelDataService {

    private final LabelRepository repository;

    public LabelDataService(LabelRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Label> findByProjectId(UUID projectId) { return repository.findByProjectId(projectId); }

    @Transactional(readOnly = true)
    public Optional<Label> findById(UUID id) { return repository.findById(id); }

    @Transactional(readOnly = true)
    public Optional<Label> findByProjectIdAndNameIgnoreCase(UUID projectId, String name) {
        return repository.findByProjectIdAndNameIgnoreCase(projectId, name);
    }

    public Label create(Label label) { return repository.insert(label); }

    public Label update(Label label) { return repository.update(label); }

    public int deleteById(UUID id) { return repository.deleteById(id); }
}
