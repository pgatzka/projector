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

  Scenario: List issues returns wrapped page
    Given an issue in "ENG" with title "First" exists
    When I GET "/api/projects/ENG/issues"
    Then the response status is 200
    And the response body field "total" equals "1"
    And the response body field "size" equals "50"

  Scenario: Filter issues by status
    Given an issue in "ENG" with title "Bug" exists
    And an issue in "ENG" with title "Done thing" with status "done" exists
    When I GET "/api/projects/ENG/issues?status=done"
    Then the response status is 200
    And the response body field "total" equals "1"

  Scenario: Filter issues by priority OR-within
    Given an issue in "ENG" with title "Low one" with priority "low" exists
    And an issue in "ENG" with title "High one" with priority "high" exists
    And an issue in "ENG" with title "Urgent one" with priority "urgent" exists
    When I GET "/api/projects/ENG/issues?priority=high,urgent"
    Then the response status is 200
    And the response body field "total" equals "2"

  Scenario: Full-text search by description
    Given an issue in "ENG" with title "Auth bug" with description "login fails on logout" exists
    And an issue in "ENG" with title "Build red" with description "ci pipeline broke" exists
    When I GET "/api/projects/ENG/issues?q=login"
    Then the response status is 200
    And the response body field "total" equals "1"

  Scenario: Pagination clamps and pages
    Given 3 issues in "ENG" exist
    When I GET "/api/projects/ENG/issues?size=2&page=0"
    Then the response status is 200
    And the response body field "total" equals "3"
    And the response body field "size" equals "2"
