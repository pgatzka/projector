package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

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
