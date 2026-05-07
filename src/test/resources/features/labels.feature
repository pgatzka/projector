Feature: Labels CRUD

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"
    And a project "ENG" / "Engineering" exists

  Scenario: Create a label
    When I POST to "/api/projects/ENG/labels" with body:
      """
      {"name":"bug","color":"red"}
      """
    Then the response status is 201
    And the response body field "name" equals "bug"
    And the response body field "color" equals "red"

  Scenario: Reject duplicate label name
    Given a label "bug" / "red" exists in project "ENG"
    When I POST to "/api/projects/ENG/labels" with body:
      """
      {"name":"BUG","color":"blue"}
      """
    Then the response status is 409

  Scenario: Update label name and color
    Given a label "bug" / "red" exists in project "ENG"
    When I PATCH a label in project "ENG" named "bug" with body:
      """
      {"name":"defect","color":"orange"}
      """
    Then the response status is 200
    And the response body field "name" equals "defect"
    And the response body field "color" equals "orange"

  Scenario: Delete label
    Given a label "bug" / "red" exists in project "ENG"
    When I DELETE a label in project "ENG" named "bug"
    Then the response status is 204

  Scenario: Project delete cascades to labels
    Given a label "bug" / "red" exists in project "ENG"
    When I DELETE "/api/projects/ENG"
    Then the response status is 204
    When I GET "/api/projects/ENG/labels"
    Then the response status is 404
