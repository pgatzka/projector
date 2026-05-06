Feature: Current account (/api/me)

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created

  Scenario: /api/me returns 401 when not logged in
    When I GET "/api/me"
    Then the response status is 401

  Scenario: /api/me returns the current account when logged in
    Given I am logged in as "admin@example.test" / "hunter22!"
    When I GET "/api/me"
    Then the response status is 200
    And the response body field "email" equals "admin@example.test"
    And the response body field "displayName" equals "Admin"
