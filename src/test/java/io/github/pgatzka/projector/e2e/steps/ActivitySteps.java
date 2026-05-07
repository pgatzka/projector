package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ActivitySteps {

    @Autowired
    private AuthSteps auth;

    private final RestClient client = RestClient.create();

    private List<Map<String, Object>> lastActivityList;
    private Map<String, Object> lastTimeline;

    @When("I list the activity feed for issue {string}")
    @SuppressWarnings("unchecked")
    public void iListActivity(String issueIdentifier) {
        fetchTimeline(issueIdentifier);
        if (lastTimeline == null) {
            lastActivityList = null;
            return;
        }
        List<Map<String, Object>> entries = (List<Map<String, Object>>) lastTimeline.get("entries");
        lastActivityList = entries.stream()
            .filter(e -> "activity".equals(e.get("type")))
            .toList();
    }

    @When("I list the timeline for issue {string}")
    public void iListTimeline(String issueIdentifier) {
        fetchTimeline(issueIdentifier);
    }

    private void fetchTimeline(String issueIdentifier) {
        String[] parts = issueIdentifier.split("-", 2);
        String projectKey = parts[0];
        String number = parts[1];
        try {
            ResponseEntity<Map<String, Object>> resp = client.get()
                .uri("http://localhost:" + auth.getPort()
                    + "/api/projects/" + projectKey + "/issues/" + number + "/timeline")
                .header("Cookie", auth.getSessionCookie())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<Map<String, Object>>() {});
            lastTimeline = resp.getBody();
            auth.setLastResponse(ResponseEntity.status(resp.getStatusCode()).build());
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            lastTimeline = null;
            auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }

    @Then("the activity feed has {int} entries")
    public void activityFeedHasEntries(int count) {
        assertThat(lastActivityList).isNotNull();
        assertThat(lastActivityList).hasSize(count);
    }

    @Then("the activity feed entry at index {int} has action {string}")
    public void activityFeedEntryHasAction(int index, String action) {
        assertThat(lastActivityList).isNotNull();
        assertThat(lastActivityList.size()).isGreaterThan(index);
        assertThat(lastActivityList.get(index).get("action")).isEqualTo(action);
    }

    @Then("the activity feed entry at index {int} payload has {string} {string}")
    @SuppressWarnings("unchecked")
    public void activityFeedEntryPayloadField(int index, String field, String value) {
        assertThat(lastActivityList).isNotNull();
        assertThat(lastActivityList.size()).isGreaterThan(index);
        Map<String, Object> payload = (Map<String, Object>) lastActivityList.get(index).get("payload");
        assertThat(payload).isNotNull();
        assertThat(String.valueOf(payload.get(field))).isEqualTo(value);
    }

    @Then("the timeline has {int} entries")
    @SuppressWarnings("unchecked")
    public void timelineHasEntries(int count) {
        assertThat(lastTimeline).isNotNull();
        List<Map<String, Object>> entries = (List<Map<String, Object>>) lastTimeline.get("entries");
        assertThat(entries).hasSize(count);
        assertThat(((Number) lastTimeline.get("total")).intValue()).isEqualTo(count);
    }

    @Then("the timeline entry at index {int} has type {string}")
    @SuppressWarnings("unchecked")
    public void timelineEntryHasType(int index, String type) {
        assertThat(lastTimeline).isNotNull();
        List<Map<String, Object>> entries = (List<Map<String, Object>>) lastTimeline.get("entries");
        assertThat(entries.size()).isGreaterThan(index);
        assertThat(entries.get(index).get("type")).isEqualTo(type);
    }

    @Then("the timeline entry at index {int} has action {string}")
    @SuppressWarnings("unchecked")
    public void timelineEntryHasAction(int index, String action) {
        assertThat(lastTimeline).isNotNull();
        List<Map<String, Object>> entries = (List<Map<String, Object>>) lastTimeline.get("entries");
        assertThat(entries.size()).isGreaterThan(index);
        assertThat(entries.get(index).get("action")).isEqualTo(action);
    }

    @Then("the timeline entry at index {int} has bodyMd {string}")
    @SuppressWarnings("unchecked")
    public void timelineEntryHasBodyMd(int index, String body) {
        assertThat(lastTimeline).isNotNull();
        List<Map<String, Object>> entries = (List<Map<String, Object>>) lastTimeline.get("entries");
        assertThat(entries.size()).isGreaterThan(index);
        assertThat(entries.get(index).get("bodyMd")).isEqualTo(body);
    }
}
