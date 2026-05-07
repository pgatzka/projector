package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IssueSteps {

    @Autowired
    private AuthSteps auth;

    private final RestClient client = RestClient.create();

    @Given("an issue in {string} with title {string} exists")
    public void issueExists(String projectKey, String title) {
        postIssue(projectKey, Map.of("title", title));
    }

    @Given("an issue in {string} with title {string} with status {string} exists")
    public void issueExistsWithStatus(String projectKey, String title, String status) {
        postIssue(projectKey, Map.of("title", title, "status", status));
    }

    @Given("an issue in {string} with title {string} with priority {string} exists")
    public void issueExistsWithPriority(String projectKey, String title, String priority) {
        postIssue(projectKey, Map.of("title", title, "priority", priority));
    }

    @Given("an issue in {string} with title {string} with description {string} exists")
    public void issueExistsWithDescription(String projectKey, String title, String description) {
        postIssue(projectKey, Map.of("title", title, "descriptionMd", description));
    }

    @Given("{int} issues in {string} exist")
    public void nIssuesExist(int count, String projectKey) {
        for (int i = 1; i <= count; i++) {
            postIssue(projectKey, Map.of("title", "Auto-" + i));
        }
    }

    @When("I create an issue in {string} with title {string} and labels {string} and {string}")
    public void iCreateIssueWithLabels(String projectKey, String title, String label1, String label2) {
        List<String> labelIds = new ArrayList<>();
        labelIds.add(findLabelIdByName(projectKey, label1));
        labelIds.add(findLabelIdByName(projectKey, label2));

        Map<String, Object> body = Map.of(
            "title", title,
            "priority", "medium",
            "status", "todo",
            "labelIds", labelIds
        );

        try {
            ResponseEntity<Map> resp = client.post()
                .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/issues")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .body(body)
                .retrieve()
                .toEntity(Map.class);
            auth.setLastResponse(resp);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }

    private String findLabelIdByName(String projectKey, String labelName) {
        List<Map<String, Object>> labels = client.get()
            .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/labels")
            .header("Cookie", auth.getSessionCookie())
            .retrieve()
            .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (labels == null) {
            throw new IllegalStateException("No labels returned for project " + projectKey);
        }
        return labels.stream()
            .filter(l -> labelName.equals(l.get("name")))
            .map(l -> String.valueOf(l.get("id")))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "Label '" + labelName + "' not found in project " + projectKey));
    }

    private void postIssue(String projectKey, Map<String, String> body) {
        client.post()
            .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/issues")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Cookie", auth.getSessionCookie())
            .header("X-XSRF-TOKEN", auth.getCsrfToken())
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }
}
