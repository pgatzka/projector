Feature: Issues CRUD

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"
    And a project "ENG" / "Engineering" exists

  Scenario: Create issue gets number 1
    When I POST to "/api/projects/ENG/issues" with body:
      """
      {"title":"First issue","priority":"high"}
      """
    Then the response status is 201
    And the response body field "number" equals "1"
    And the response body field "identifier" equals "ENG-1"
    And the response body field "title" equals "First issue"
    And the response body field "status" equals "todo"

  Scenario: Subsequent issues increment per project
    Given an issue in "ENG" with title "First" exists
    When I POST to "/api/projects/ENG/issues" with body:
      """
      {"title":"Second"}
      """
    Then the response status is 201
    And the response body field "number" equals "2"

  Scenario: Lookup issue by KEY-N
    Given an issue in "ENG" with title "First" exists
    When I GET "/api/projects/ENG/issues/1"
    Then the response status is 200
    And the response body field "identifier" equals "ENG-1"

  Scenario: Update issue title and status
    Given an issue in "ENG" with title "First" exists
    When I PATCH "/api/projects/ENG/issues/1" with body:
      """
      {"title":"First (revised)","status":"in_progress"}
      """
    Then the response status is 200
    And the response body field "title" equals "First (revised)"
    And the response body field "status" equals "in_progress"

  Scenario: Delete issue
    Given an issue in "ENG" with title "First" exists
    When I DELETE "/api/projects/ENG/issues/1"
    Then the response status is 204
    When I GET "/api/projects/ENG/issues/1"
    Then the response status is 404
