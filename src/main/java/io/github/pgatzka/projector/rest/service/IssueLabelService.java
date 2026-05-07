package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.IssueLabelDataService;
import io.github.pgatzka.projector.data.service.LabelDataService;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.jooq.tables.pojos.Label;
import io.github.pgatzka.projector.rest.exception.LabelNotFoundException;
import io.github.pgatzka.projector.rest.exception.LabelNotInProjectException;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class IssueLabelService {

    private final IssueService issueService;
    private final LabelDataService labelData;
    private final IssueLabelDataService issueLabelData;
    private final ActivityService activityService;

    public IssueLabelService(IssueService issueService,
                             LabelDataService labelData,
                             IssueLabelDataService issueLabelData,
                             @Lazy ActivityService activityService) {
        this.issueService = issueService;
        this.labelData = labelData;
        this.issueLabelData = issueLabelData;
        this.activityService = activityService;
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
            activityService.emit(issue.getId(), ActivityAction.label_added, labelPayload(label));
        }
        return issue;
    }

    @Transactional
    public void unassign(String projectKey, int issueNumber, UUID labelId) {
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, issueNumber);
        Label label = labelData.findById(labelId).orElse(null);
        int deleted = issueLabelData.unassign(issue.getId(), labelId);
        if (deleted > 0 && label != null) {
            activityService.emit(issue.getId(), ActivityAction.label_removed, labelPayload(label));
        }
    }

    private static Map<String, Object> labelPayload(Label label) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("labelId", label.getId() == null ? null : label.getId().toString());
        m.put("labelName", label.getName());
        m.put("labelColor", label.getColor() == null ? null : label.getColor().name());
        return m;
    }
}
