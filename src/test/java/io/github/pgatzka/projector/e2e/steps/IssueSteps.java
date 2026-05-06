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
        Map<String, String> body = Map.of("title", title);
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
