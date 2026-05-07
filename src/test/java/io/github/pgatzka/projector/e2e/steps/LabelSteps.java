package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

public class LabelSteps {

    @Autowired
    private AuthSteps auth;

    private final RestClient client = RestClient.create();

    @Given("a label {string} \\/ {string} exists in project {string}")
    public void labelExists(String name, String color, String projectKey) {
        Map<String, String> body = Map.of("name", name, "color", color);
        client.post()
            .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/labels")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Cookie", auth.getSessionCookie())
            .header("X-XSRF-TOKEN", auth.getCsrfToken())
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }

    @When("I PATCH a label in project {string} named {string} with body:")
    public void iPatchLabelNamed(String projectKey, String labelName, String json) {
        try {
            String labelId = findLabelIdByName(projectKey, labelName);
            ResponseEntity<Map> resp = client.patch()
                .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/labels/" + labelId)
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

    @When("I DELETE a label in project {string} named {string}")
    public void iDeleteLabelNamed(String projectKey, String labelName) {
        try {
            String labelId = findLabelIdByName(projectKey, labelName);
            ResponseEntity<Void> resp = client.delete()
                .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/labels/" + labelId)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .retrieve()
                .toBodilessEntity();
            auth.setLastResponse(ResponseEntity.status(resp.getStatusCode()).build());
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
}
