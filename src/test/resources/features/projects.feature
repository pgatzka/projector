Feature: Projects CRUD

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"

  Scenario: Create a project
    When I POST to "/api/projects" with body:
      """
      {"key":"ENG","name":"Engineering","description":"Backend stuff"}
      """
    Then the response status is 201
    And the response body field "key" equals "ENG"
    And the response body field "name" equals "Engineering"

  Scenario: Reject duplicate project key
    Given a project "ENG" / "Engineering" exists
    When I POST to "/api/projects" with body:
      """
      {"key":"ENG","name":"Engineering 2"}
      """
    Then the response status is 409

  Scenario: Reject invalid project key
    When I POST to "/api/projects" with body:
      """
      {"key":"eng","name":"Engineering"}
      """
    Then the response status is 400

  Scenario: Update project
    Given a project "ENG" / "Engineering" exists
    When I PATCH "/api/projects/ENG" with body:
      """
      {"name":"Engineering team","description":"Backend + infra"}
      """
    Then the response status is 200
    And the response body field "name" equals "Engineering team"

  Scenario: Delete project
    Given a project "ENG" / "Engineering" exists
    When I DELETE "/api/projects/ENG"
    Then the response status is 204
    When I GET "/api/projects/ENG"
    Then the response status is 404
