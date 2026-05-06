Feature: Health endpoint

  Scenario: Health endpoint returns ok
    When I GET "/api/health"
    Then the response status is 200
    And the response body field "status" equals "ok"
