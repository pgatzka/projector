package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.CommentRepository;
import io.github.pgatzka.projector.jooq.tables.pojos.Comment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CommentDataService {

    private final CommentRepository repository;

    public CommentDataService(CommentRepository repository) {
        this.repository = repository;
    }

    public Comment create(UUID issueId, String bodyMd) {
        Comment comment = new Comment();
        comment.setIssueId(issueId);
        comment.setBodyMd(bodyMd);
        return repository.insert(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> findByIssueId(UUID issueId) {
        return repository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
}
