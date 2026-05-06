package io.github.pgatzka.projector.e2e.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthSteps {

    @LocalServerPort
    private int port;

    private ResponseEntity<Map> response;

    @When("I request GET {string}")
    public void iRequestGet(String path) {
        RestClient client = RestClient.create();
        response = client.get()
            .uri("http://localhost:" + port + path)
            .retrieve()
            .toEntity(Map.class);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(response.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response body field {string} equals {string}")
    public void theResponseBodyFieldEquals(String field, String value) {
        assertThat(response.getBody()).containsEntry(field, value);
    }
}
