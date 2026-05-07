package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CommentSteps {

    @Autowired
    private AuthSteps auth;

    private final RestClient client = RestClient.create();
    private List<Map<String, Object>> lastCommentList;

    @Given("a comment {string} exists on issue {string}")
    public void commentExistsOnIssue(String body, String issueIdentifier) {
        String[] parts = issueIdentifier.split("-", 2);
        String projectKey = parts[0];
        String number = parts[1];
        client.post()
            .uri("http://localhost:" + auth.getPort()
                + "/api/projects/" + projectKey + "/issues/" + number + "/comments")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Cookie", auth.getSessionCookie())
            .header("X-XSRF-TOKEN", auth.getCsrfToken())
            .body(Map.of("bodyMd", body))
            .retrieve()
            .toBodilessEntity();
    }

    @When("I POST a comment of {int} chars to {string}")
    public void iPostOversizedComment(int length, String issueIdentifier) {
        String[] parts = issueIdentifier.split("-", 2);
        String projectKey = parts[0];
        String number = parts[1];
        String body = "x".repeat(length);
        try {
            ResponseEntity<Map> resp = client.post()
                .uri("http://localhost:" + auth.getPort()
                    + "/api/projects/" + projectKey + "/issues/" + number + "/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .body(Map.of("bodyMd", body))
                .retrieve()
                .toEntity(Map.class);
            auth.setLastResponse(resp);
        } catch (HttpStatusCodeException ex) {
            auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }

    @When("I list comments on issue {string}")
    public void iListComments(String issueIdentifier) {
        String[] parts = issueIdentifier.split("-", 2);
        String projectKey = parts[0];
        String number = parts[1];
        lastCommentList = client.get()
            .uri("http://localhost:" + auth.getPort()
                + "/api/projects/" + projectKey + "/issues/" + number + "/comments")
            .header("Cookie", auth.getSessionCookie())
            .retrieve()
            .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        auth.setLastResponse(ResponseEntity.ok().build());
    }

    @Then("the comment list at index {int} has bodyMd {string}")
    public void commentListAtIndex(int index, String expected) {
        assertThat(lastCommentList).isNotNull();
        assertThat(lastCommentList.size()).isGreaterThan(index);
        assertThat(lastCommentList.get(index).get("bodyMd")).isEqualTo(expected);
    }
}
