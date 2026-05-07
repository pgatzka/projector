Feature: First-run setup

  Scenario: First-run setup creates the admin
    When I POST to "/api/setup" with body:
      """
      {"email":"admin@example.test","password":"hunter22!","displayName":"Admin"}
      """
    Then the response status is 201
    And the response body field "email" equals "admin@example.test"
    And the response body field "displayName" equals "Admin"

  Scenario: Setup is rejected after the first admin exists
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    When I POST to "/api/setup" with body:
      """
      {"email":"second@example.test","password":"another1!","displayName":"Second"}
      """
    Then the response status is 409

  Scenario: setup-required returns true on fresh database
    When I GET "/api/setup-required"
    Then the response status is 200
    And the response body field "required" equals "true"

  Scenario: setup-required returns false after setup
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    When I GET "/api/setup-required"
    Then the response status is 200
    And the response body field "required" equals "false"
