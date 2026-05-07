package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.IssueRepository;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.rest.dto.IssueListQuery;
import io.github.pgatzka.projector.rest.dto.PageDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class IssueDataService {

    private final IssueRepository repository;

    public IssueDataService(IssueRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Issue> findByProjectId(UUID projectId) { return repository.findByProjectId(projectId); }

    @Transactional(readOnly = true)
    public Optional<Issue> findById(UUID id) { return repository.findById(id); }

    @Transactional(readOnly = true)
    public Optional<Issue> findByProjectIdAndNumber(UUID projectId, int number) {
        return repository.findByProjectIdAndNumber(projectId, number);
    }

    public Issue create(Issue issue) { return repository.insert(issue); }

    public Issue update(Issue issue) { return repository.update(issue); }

    public int deleteById(UUID id) { return repository.deleteById(id); }

    @Transactional(readOnly = true)
    public PageDto<Issue> listForProject(UUID projectId, IssueListQuery query) {
        return repository.listForProject(projectId, query);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<Label>> loadLabelsByIssueIds(Collection<UUID> issueIds) {
        return repository.loadLabelsByIssueIds(issueIds);
    }
}
