package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueLabelSteps {

    @Autowired
    private AuthSteps auth;

    private final RestClient client = RestClient.create();

    @When("I create an issue in {string} titled {string} with labels {string}")
    public void iCreateIssueWithLabels(String projectKey, String title, String labelsCsv) {
        List<String> labelIds = Arrays.stream(labelsCsv.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .map(name -> findLabelIdByName(projectKey, name))
            .toList();
        Map<String, Object> body = new HashMap<>();
        body.put("title", title);
        body.put("labelIds", labelIds);
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

    @When("I assign label {string} in project {string} to issue {int}")
    public void iAssignLabel(String labelName, String projectKey, int issueNumber) {
        assignLabel(labelName, projectKey, issueNumber, true);
    }

    @Given("label {string} is assigned to issue {int} in {string}")
    public void labelIsAssignedTo(String labelName, int issueNumber, String projectKey) {
        assignLabel(labelName, projectKey, issueNumber, false);
    }

    @When("I unassign label {string} in project {string} from issue {int}")
    public void iUnassignLabel(String labelName, String projectKey, int issueNumber) {
        String labelId = findLabelIdByName(projectKey, labelName);
        try {
            ResponseEntity<Void> resp = client.delete()
                .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey
                    + "/issues/" + issueNumber + "/labels/" + labelId)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .retrieve()
                .toBodilessEntity();
            auth.setLastResponse(ResponseEntity.status(resp.getStatusCode()).build());
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }

    @Then("the response body has {int} label named {string}")
    @SuppressWarnings("unchecked")
    public void responseHasLabelNamed(int count, String labelName) {
        ResponseEntity<Map> resp = auth.getLastResponse();
        assertThat(resp.getBody()).isNotNull();
        Object labelsObj = resp.getBody().get("labels");
        assertThat(labelsObj).isInstanceOf(List.class);
        List<Map<String, Object>> labels = (List<Map<String, Object>>) labelsObj;
        assertThat(labels).hasSize(count);
        assertThat(labels.stream().anyMatch(l -> labelName.equals(l.get("name")))).isTrue();
    }

    private void assignLabel(String labelName, String projectKey, int issueNumber, boolean captureResponse) {
        String labelId = findLabelIdByName(projectKey, labelName);
        Map<String, String> body = Map.of("labelId", labelId);
        try {
            ResponseEntity<Map> resp = client.post()
                .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey
                    + "/issues/" + issueNumber + "/labels")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Cookie", auth.getSessionCookie())
                .header("X-XSRF-TOKEN", auth.getCsrfToken())
                .body(body)
                .retrieve()
                .toEntity(Map.class);
            if (captureResponse) auth.setLastResponse(resp);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            if (captureResponse) auth.setLastResponse(ResponseEntity.status(ex.getStatusCode()).build());
        }
    }

    private String findLabelIdByName(String projectKey, String labelName) {
        String id = lookupLabelInProject(projectKey, labelName);
        if (id != null) return id;
        // Fall back: scan every project (needed for cross-project label tests).
        List<Map<String, Object>> projects = client.get()
            .uri("http://localhost:" + auth.getPort() + "/api/projects")
            .header("Cookie", auth.getSessionCookie())
            .retrieve()
            .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (projects != null) {
            for (Map<String, Object> p : projects) {
                String pk = String.valueOf(p.get("key"));
                if (pk.equals(projectKey)) continue;
                String found = lookupLabelInProject(pk, labelName);
                if (found != null) return found;
            }
        }
        throw new IllegalStateException(
            "Label '" + labelName + "' not found in any project (searched from '" + projectKey + "')");
    }

    private String lookupLabelInProject(String projectKey, String labelName) {
        List<Map<String, Object>> labels = client.get()
            .uri("http://localhost:" + auth.getPort() + "/api/projects/" + projectKey + "/labels")
            .header("Cookie", auth.getSessionCookie())
            .retrieve()
            .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});
        if (labels == null) return null;
        return labels.stream()
            .filter(l -> labelName.equals(l.get("name")))
            .map(l -> String.valueOf(l.get("id")))
            .findFirst()
            .orElse(null);
    }
}
