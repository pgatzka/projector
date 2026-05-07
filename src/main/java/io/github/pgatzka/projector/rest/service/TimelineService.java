package io.github.pgatzka.projector.rest.service;

import io.github.pgatzka.projector.rest.dto.ActivityDto;
import io.github.pgatzka.projector.rest.dto.CommentDto;
import io.github.pgatzka.projector.rest.dto.TimelineDto;
import io.github.pgatzka.projector.rest.dto.TimelineEntryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TimelineService {

    private final CommentService commentService;
    private final ActivityService activityService;

    public TimelineService(CommentService commentService, ActivityService activityService) {
        this.commentService = commentService;
        this.activityService = activityService;
    }

    @Transactional(readOnly = true)
    public TimelineDto getTimeline(String projectKey, int issueNumber) {
        List<CommentDto> comments = commentService.listByIssue(projectKey, issueNumber);
        List<ActivityDto> activities = activityService.listByIssue(projectKey, issueNumber);

        List<TimelineEntryDto> entries = new ArrayList<>(comments.size() + activities.size());
        comments.forEach(c -> entries.add(TimelineEntryDto.comment(c)));
        activities.forEach(a -> entries.add(TimelineEntryDto.activity(a)));
        entries.sort(Comparator.comparing(TimelineEntryDto::createdAt));

        return new TimelineDto(entries, entries.size());
    }
}
