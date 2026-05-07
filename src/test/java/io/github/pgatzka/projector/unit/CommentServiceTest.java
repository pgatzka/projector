package io.github.pgatzka.projector.unit;

import io.github.pgatzka.projector.data.service.CommentDataService;
import io.github.pgatzka.projector.jooq.tables.pojos.Comment;
import io.github.pgatzka.projector.jooq.tables.pojos.Issue;
import io.github.pgatzka.projector.rest.dto.CommentDto;
import io.github.pgatzka.projector.rest.dto.CreateCommentRequest;
import io.github.pgatzka.projector.rest.exception.IssueNotFoundException;
import io.github.pgatzka.projector.rest.service.CommentService;
import io.github.pgatzka.projector.rest.service.IssueService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommentServiceTest {

    private static final String PROJECT_KEY = "ENG";
    private static final int ISSUE_NUMBER = 1;

    private CommentDataService commentData;
    private IssueService issueService;
    private CommentService service;

    private UUID issueId;

    @BeforeEach
    void setUp() {
        commentData = mock(CommentDataService.class);
        issueService = mock(IssueService.class);
        service = new CommentService(commentData, issueService);
        issueId = UUID.randomUUID();
    }

    @Test
    void create_throwsIssueNotFound_whenIssueMissing() {
        when(issueService.findByProjectKeyAndNumber(PROJECT_KEY, ISSUE_NUMBER))
            .thenThrow(new IssueNotFoundException(PROJECT_KEY + "-" + ISSUE_NUMBER));

        assertThatThrownBy(() -> service.create(PROJECT_KEY, ISSUE_NUMBER, "hi"))
            .isInstanceOf(IssueNotFoundException.class);
    }

    @Test
    void create_persistsAndReturnsDto() {
        Issue issue = new Issue();
        issue.setId(issueId);
        when(issueService.findByProjectKeyAndNumber(PROJECT_KEY, ISSUE_NUMBER)).thenReturn(issue);

        Comment persisted = new Comment();
        persisted.setId(UUID.randomUUID());
        persisted.setIssueId(issueId);
        persisted.setBodyMd("hello");
        persisted.setCreatedAt(OffsetDateTime.now());
        persisted.setCreatedBy("user:abc");
        when(commentData.create(eq(issueId), eq("hello"))).thenReturn(persisted);

        CommentDto dto = service.create(PROJECT_KEY, ISSUE_NUMBER, "hello");

        assertThat(dto.id()).isEqualTo(persisted.getId());
        assertThat(dto.bodyMd()).isEqualTo("hello");
        assertThat(dto.createdBy()).isEqualTo("user:abc");
    }

    @Test
    void listByIssue_returnsDtosFromData() {
        Issue issue = new Issue();
        issue.setId(issueId);
        when(issueService.findByProjectKeyAndNumber(PROJECT_KEY, ISSUE_NUMBER)).thenReturn(issue);

        Comment c1 = new Comment();
        c1.setId(UUID.randomUUID());
        c1.setBodyMd("first");
        c1.setCreatedAt(OffsetDateTime.now().minusMinutes(2));
        Comment c2 = new Comment();
        c2.setId(UUID.randomUUID());
        c2.setBodyMd("second");
        c2.setCreatedAt(OffsetDateTime.now().minusMinutes(1));
        when(commentData.findByIssueId(issueId)).thenReturn(List.of(c1, c2));

        List<CommentDto> result = service.listByIssue(PROJECT_KEY, ISSUE_NUMBER);

        assertThat(result).extracting(CommentDto::bodyMd).containsExactly("first", "second");
    }

    @Test
    void createCommentRequest_rejectsBlank() {
        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<CreateCommentRequest>> violations = v.validate(new CreateCommentRequest("  "));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void createCommentRequest_rejectsTooLong() {
        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        String tooLong = "x".repeat(10001);
        Set<ConstraintViolation<CreateCommentRequest>> violations = v.validate(new CreateCommentRequest(tooLong));
        assertThat(violations).isNotEmpty();
    }

    @Test
    void createCommentRequest_acceptsAtLimit() {
        Validator v = Validation.buildDefaultValidatorFactory().getValidator();
        String atLimit = "x".repeat(10000);
        Set<ConstraintViolation<CreateCommentRequest>> violations = v.validate(new CreateCommentRequest(atLimit));
        assertThat(violations).isEmpty();
    }
}
