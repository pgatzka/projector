package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AuthSteps {

    @LocalServerPort
    private int port;

    private final RestClient client = RestClient.create();
    private String csrfToken;
    private String sessionCookie;
    private ResponseEntity<Map> response;

    private String url(String path) { return "http://localhost:" + port + path; }

    @Given("the first admin {string} \\/ {string} \\/ {string} has been created")
    public void firstAdminCreated(String email, String password, String displayName) {
        Map<String, String> body = Map.of("email", email, "password", password, "displayName", displayName);
        client.post()
            .uri(url("/api/setup"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity();
    }

    @Given("I am logged in as {string} \\/ {string}")
    public void loggedInAs(String email, String password) {
        Map<String, String> body = Map.of("email", email, "password", password);
        ResponseEntity<Map> entity = client.post()
            .uri(url("/api/login"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toEntity(Map.class);
        captureCookies(entity);
        // Spring Security 7 sets the CSRF cookie lazily — only when a non-exempt
        // request causes the token attribute to be read. /api/login is CSRF-exempt,
        // so its response doesn't include XSRF-TOKEN. Make a GET to /api/me to
        // prime the cookie (this mirrors what the real SPA's AuthProvider does).
        ResponseEntity<Map> me = client.get()
            .uri(url("/api/me"))
            .header("Cookie", sessionCookie)
            .retrieve()
            .toEntity(Map.class);
        captureCookies(me);
    }

    @When("I POST to {string} with body:")
    public void iPostBody(String path, String json) {
        var spec = client.post()
            .uri(url(path))
            .contentType(MediaType.APPLICATION_JSON);
        if (sessionCookie != null) spec = (RestClient.RequestBodySpec) spec.header("Cookie", sessionCookie);
        if (csrfToken != null)     spec = (RestClient.RequestBodySpec) spec.header("X-XSRF-TOKEN", csrfToken);
        try {
            response = spec.body(json).retrieve().toEntity(Map.class);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            response = ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @When("I GET {string}")
    public void iGet(String path) {
        var spec = client.get().uri(url(path));
        if (sessionCookie != null) spec = (RestClient.RequestHeadersSpec) spec.header("Cookie", sessionCookie);
        try {
            response = spec.retrieve().toEntity(Map.class);
        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            response = ResponseEntity.status(ex.getStatusCode()).build();
        }
    }

    @Then("the response status is {int}")
    public void responseStatus(int status) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response body field {string} equals {string}")
    public void responseBodyField(String field, String value) {
        Object actual = response.getBody() == null ? null : response.getBody().get(field);
        assertThat(String.valueOf(actual)).isEqualTo(value);
    }

    private void captureCookies(ResponseEntity<?> entity) {
        HttpHeaders headers = entity.getHeaders();
        List<String> setCookies = headers.get("Set-Cookie");
        if (setCookies == null) return;
        StringBuilder cookieBuilder = new StringBuilder();
        for (String c : setCookies) {
            int semi = c.indexOf(';');
            String pair = (semi < 0) ? c : c.substring(0, semi);
            if (cookieBuilder.length() > 0) cookieBuilder.append("; ");
            cookieBuilder.append(pair);
            if (pair.startsWith("XSRF-TOKEN=")) {
                csrfToken = pair.substring("XSRF-TOKEN=".length());
            }
        }
        sessionCookie = cookieBuilder.toString();
    }

    public int getPort() { return port; }
    public String getSessionCookie() { return sessionCookie; }
    public String getCsrfToken() { return csrfToken; }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void setLastResponse(org.springframework.http.ResponseEntity<?> r) {
        this.response = (org.springframework.http.ResponseEntity) r;
    }
}
