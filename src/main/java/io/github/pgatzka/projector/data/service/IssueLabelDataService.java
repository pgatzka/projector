package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.IssueLabelRepository;
import io.github.pgatzka.projector.jooq.tables.pojos.IssueLabel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class IssueLabelDataService {

    private final IssueLabelRepository repository;

    public IssueLabelDataService(IssueLabelRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<IssueLabel> findByIssueId(UUID issueId) { return repository.findByIssueId(issueId); }

    @Transactional(readOnly = true)
    public Optional<IssueLabel> findByIssueIdAndLabelId(UUID issueId, UUID labelId) {
        return repository.findByIssueIdAndLabelId(issueId, labelId);
    }

    @Transactional(readOnly = true)
    public int countByLabelId(UUID labelId) { return repository.countByLabelId(labelId); }

    public IssueLabel assign(UUID issueId, UUID labelId) {
        IssueLabel il = new IssueLabel();
        il.setIssueId(issueId);
        il.setLabelId(labelId);
        return repository.insert(il);
    }

    public int unassign(UUID issueId, UUID labelId) {
        return repository.deleteByIssueIdAndLabelId(issueId, labelId);
    }

    public int bulkAssign(UUID issueId, List<UUID> labelIds) {
        return repository.bulkInsert(issueId, labelIds);
    }
}
