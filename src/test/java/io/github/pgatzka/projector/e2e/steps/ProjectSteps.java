package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

public class ProjectSteps {

    @Autowired
    private AuthSteps auth;

    private final RestClient client = RestClient.create();

    @Given("a project {string} \\/ {string} exists")
    public void projectExists(String key, String name) {
        Map<String, String> body = Map.of("key", key, "name", name);
        client.post()
            .uri("http://localhost:" + auth.getPort() + "/api/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Cookie", auth.getSessionCookie())
            .header("X-XSRF-TOKEN", auth.getCsrfToken())
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }

    @When("I PATCH {string} with body:")
    public void iPatch(String path, String json) {
        try {
            ResponseEntity<Map> resp = client.patch()
                .uri("http://localhost:" + auth.getPort() + path)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .body(json)
                .retrieve()
                .toEntity(Map.class);
            auth.setLastResponse(resp);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }

    @When("I DELETE {string}")
    public void iDelete(String path) {
        try {
            ResponseEntity<Void> resp = client.delete()
                .uri("http://localhost:" + auth.getPort() + path)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .retrieve()
                .toBodilessEntity();
            auth.setLastResponse(ResponseEntity.status(resp.getStatusCode()).build());
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }
}
