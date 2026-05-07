package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.rest.exception.LabelNotFoundException;
import io.github.pgatzka.projector.rest.exception.LabelNotInProjectException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IssueLabelService {

    private final IssueService issueService;
    private final LabelDataService labelData;
    private final IssueLabelDataService issueLabelData;

    public IssueLabelService(IssueService issueService,
                             LabelDataService labelData,
                             IssueLabelDataService issueLabelData) {
        this.issueService = issueService;
        this.labelData = labelData;
        this.issueLabelData = issueLabelData;
    }

    @Transactional
    public Issue assign(String projectKey, int issueNumber, UUID labelId) {
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, issueNumber);
        Label label = labelData.findById(labelId).orElseThrow(() -> new LabelNotFoundException(labelId));
        if (!label.getProjectId().equals(issue.getProjectId())) {
            throw new LabelNotInProjectException(labelId, projectKey);
        }
        if (issueLabelData.findByIssueIdAndLabelId(issue.getId(), labelId).isEmpty()) {
            issueLabelData.assign(issue.getId(), labelId);
        }
        return issue;
    }

    @Transactional
    public void unassign(String projectKey, int issueNumber, UUID labelId) {
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, issueNumber);
        issueLabelData.unassign(issue.getId(), labelId);
    }
}
