Feature: Timeline

  Background:
    Given the first admin "admin@example.test" / "hunter22!" / "Admin" has been created
    And I am logged in as "admin@example.test" / "hunter22!"
    And a project "ENG" / "Engineering" exists
    And an issue in "ENG" with title "First issue" exists

  Scenario: Timeline returns the synthetic issue_created entry
    When I list the timeline for issue "ENG-1"
    Then the timeline has 1 entries
    And the timeline entry at index 0 has type "activity"
    And the timeline entry at index 0 has action "issue_created"

  Scenario: Timeline interleaves comments and activity sorted by createdAt asc
    Given a comment "first thoughts" exists on issue "ENG-1"
    When I PATCH "/api/projects/ENG/issues/1" with body:
      """
      {"status":"in_progress"}
      """
    Then the response status is 200
    When I list the timeline for issue "ENG-1"
    Then the timeline has 3 entries
    And the timeline entry at index 0 has type "activity"
    And the timeline entry at index 0 has action "issue_created"
    And the timeline entry at index 1 has type "comment"
    And the timeline entry at index 1 has bodyMd "first thoughts"
    And the timeline entry at index 2 has type "activity"
    And the timeline entry at index 2 has action "status_changed"
