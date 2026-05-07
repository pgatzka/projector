package io.github.pgatzka.projector.data.service;

import io.github.pgatzka.projector.data.repository.ActivityRepository;
import io.github.pgatzka.projector.jooq.enums.ActivityAction;
import io.github.pgatzka.projector.jooq.tables.pojos.Activity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.JSONB;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ActivityDataService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ActivityRepository repository;

    public ActivityDataService(ActivityRepository repository) {
        this.repository = repository;
    }

    public Activity emit(UUID issueId, ActivityAction action, Map<String, Object> payload) {
        Activity activity = new Activity();
        activity.setIssueId(issueId);
        activity.setAction(action);
        try {
            String jsonString = payload == null || payload.isEmpty()
                ? "{}"
                : OBJECT_MAPPER.writeValueAsString(payload);
            activity.setPayload(JSONB.valueOf(jsonString));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize activity payload", e);
        }
        return repository.insert(activity);
    }

    @Transactional(readOnly = true)
    public List<Activity> findByIssueId(UUID issueId) {
        return repository.findByIssueIdOrderByCreatedAtAsc(issueId);
    }
}
