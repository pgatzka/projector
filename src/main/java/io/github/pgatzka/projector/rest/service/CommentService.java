package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.data.service.CommentDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Comment;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.rest.dto.CommentDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    private final CommentDataService commentData;
    private final IssueService issueService;

    public CommentService(CommentDataService commentData, IssueService issueService) {
        this.commentData = commentData;
        this.issueService = issueService;
    }

    @Transactional
    public CommentDto create(String projectKey, int issueNumber, String bodyMd) {
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, issueNumber);
        Comment created = commentData.create(issue.getId(), bodyMd);
        return CommentDto.of(created);
    }

    @Transactional(readOnly = true)
    public List<CommentDto> listByIssue(String projectKey, int issueNumber) {
        Issue issue = issueService.findByProjectKeyAndNumber(projectKey, issueNumber);
        return commentData.findByIssueId(issue.getId()).stream()
            .map(CommentDto::of)
            .toList();
    }
}
