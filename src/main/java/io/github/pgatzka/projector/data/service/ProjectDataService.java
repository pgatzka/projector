package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.ProjectRepository;
import io.github.pgatzka.projector.jooq.tables.pojos.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class ProjectDataService {

    private final ProjectRepository repository;

    public ProjectDataService(ProjectRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() { return repository.findAllOrderByKey(); }

    @Transactional(readOnly = true)
    public Optional<Project> findById(UUID id) { return repository.findById(id); }

    @Transactional(readOnly = true)
    public Optional<Project> findByKey(String key) { return repository.findByKey(key); }

    @Transactional(readOnly = true)
    public boolean existsByKey(String key) { return repository.existsByKey(key); }

    public Project create(Project project) { return repository.insert(project); }

    public Project update(Project project) { return repository.update(project); }

    public int deleteById(UUID id) { return repository.deleteById(id); }

    public int claimNextIssueNumber(UUID projectId) { return repository.claimNextIssueNumber(projectId); }
}
